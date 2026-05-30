#!/usr/bin/env python3
"""
Patches libyoga.so inside beagle-yoga AAR for Android 16KB page size compatibility.

The script inserts padding between ELF LOAD segments so:
    p_offset % 16384 == p_vaddr % 16384   (required for 16KB mmap)

Only 64-bit ELFs (arm64-v8a, x86_64) are patched; 32-bit ones are copied unchanged.

Usage:
    python3 scripts/patch_yoga_16kb.py

Outputs:
    beagle/libs/beagle-yoga-1.19.0-16k.aar
"""

import struct
import os
import shutil
import zipfile
import tempfile

NEW_ALIGN = 0x4000  # 16 KB
PT_LOAD = 1         # ELF segment type


# ---- ELF helpers -----------------------------------------------------------

def _u(fmt, buf, off):
    return struct.unpack_from(fmt, buf, off)[0]


def _p(fmt, buf, off, val):
    struct.pack_into(fmt, buf, off, val)


def read_elf64_hdr(data):
    e = '<' if data[5] == 1 else '>'
    return {
        'endian': e,
        'e_phoff':     _u(e + 'Q', data, 32),
        'e_shoff':     _u(e + 'Q', data, 40),
        'e_phentsize': _u(e + 'H', data, 54),
        'e_phnum':     _u(e + 'H', data, 56),
        'e_shentsize': _u(e + 'H', data, 58),
        'e_shnum':     _u(e + 'H', data, 60),
    }


def read_phdrs(data, hdr):
    e, result = hdr['endian'], []
    for i in range(hdr['e_phnum']):
        base = hdr['e_phoff'] + i * hdr['e_phentsize']
        result.append({
            'entry':  base,
            'p_type': _u(e + 'I', data, base),
            'p_off':  _u(e + 'Q', data, base + 8),
            'p_vaddr':_u(e + 'Q', data, base + 16),
            'p_align':_u(e + 'Q', data, base + 48),
        })
    return result


# ---- Core patcher ----------------------------------------------------------

def patch_so_16kb(src, dst):
    """Patch a single .so file for 16KB alignment and write to dst."""
    raw = bytearray(open(src, 'rb').read())

    if raw[:4] != b'\x7fELF':
        shutil.copy2(src, dst)
        print(f'    [COPY] not ELF: {os.path.basename(src)}')
        return True

    if raw[4] != 2:   # 32-bit – skip
        shutil.copy2(src, dst)
        print(f'    [COPY] 32-bit ELF (16KB not required): {os.path.basename(src)}')
        return True

    hdr = read_elf64_hdr(raw)
    e = hdr['endian']
    phdrs = read_phdrs(raw, hdr)

    loads = sorted([p for p in phdrs if p['p_type'] == PT_LOAD],
                   key=lambda p: p['p_off'])

    if len(loads) < 2:
        shutil.copy2(src, dst)
        print(f'    [COPY] <2 LOAD segments: {os.path.basename(src)}')
        return True

    # --- Determine padding needed for each non-first LOAD segment ----------
    patches = []
    cumulative = 0
    for seg in loads:
        if seg['p_off'] == 0:
            continue
        adjusted = seg['p_off'] + cumulative
        target_mod = seg['p_vaddr'] % NEW_ALIGN
        if adjusted % NEW_ALIGN == target_mod and seg['p_align'] >= NEW_ALIGN:
            continue  # already aligned
        # smallest offset >= adjusted that satisfies the congruence
        base = (adjusted // NEW_ALIGN) * NEW_ALIGN
        candidate = base + target_mod
        if candidate < adjusted:
            candidate += NEW_ALIGN
        padding = candidate - adjusted
        patches.append((adjusted, padding))
        cumulative += padding

    if not patches:
        shutil.copy2(src, dst)
        print(f'    [ALREADY OK] {os.path.basename(src)}')
        return True

    # --- Insert padding bytes -----------------------------------------------
    data = bytearray(raw)
    shift = 0
    for insert_at, pad in patches:
        pos = insert_at + shift
        data = data[:pos] + bytes(pad) + data[pos:]
        shift += pad

    total_padding = shift
    first_insert = patches[0][0]
    orig_phdrs = read_phdrs(bytearray(raw), hdr)

    # --- Update program headers (phdr table is in the first LOAD, before insertion) --
    for p in orig_phdrs:
        entry = p['entry']
        if p['p_type'] == PT_LOAD:
            _p(e + 'Q', data, entry + 48, NEW_ALIGN)  # p_align
        if p['p_off'] > 0 and p['p_off'] >= first_insert:
            _p(e + 'Q', data, entry + 8, p['p_off'] + total_padding)  # p_offset

    # --- Update section headers (shdr table was after insertion point) ------
    orig_shoff = hdr['e_shoff']
    if hdr['e_shnum'] > 0 and orig_shoff > 0:
        new_shoff = orig_shoff + total_padding
        _p(e + 'Q', data, 40, new_shoff)   # e_shoff in ELF header
        for i in range(hdr['e_shnum']):
            sb = new_shoff + i * hdr['e_shentsize']
            orig_sh_off = _u(e + 'Q', data, sb + 24)  # sh_offset field (original value)
            if orig_sh_off > 0 and orig_sh_off >= first_insert:
                _p(e + 'Q', data, sb + 24, orig_sh_off + total_padding)

    # --- Verify ------------------------------------------------------------
    final_hdr = read_elf64_hdr(data)
    final_loads = [p for p in read_phdrs(data, final_hdr) if p['p_type'] == PT_LOAD and p['p_off'] > 0]
    ok = all(p['p_off'] % NEW_ALIGN == p['p_vaddr'] % NEW_ALIGN and p['p_align'] >= NEW_ALIGN
             for p in final_loads)

    os.makedirs(os.path.dirname(os.path.abspath(dst)), exist_ok=True)
    open(dst, 'wb').write(data)
    status = '[OK]' if ok else '[WARN] alignment check failed!'
    print(f'    {status} {os.path.basename(dst)} (+{hex(total_padding)} bytes padding)')
    return ok


# ---- AAR patcher -----------------------------------------------------------

def patch_yoga_aar(src_aar, dst_aar):
    work = tempfile.mkdtemp(prefix='yoga16k_')
    try:
        print(f'Unpacking {os.path.basename(src_aar)} ...')
        with zipfile.ZipFile(src_aar, 'r') as z:
            z.extractall(work)

        jni_dir = os.path.join(work, 'jni')
        for arch in sorted(os.listdir(jni_dir)):
            so = os.path.join(jni_dir, arch, 'libyoga.so')
            if not os.path.exists(so):
                continue
            tmp_out = so + '.patched'
            print(f'\n  Patching {arch}/libyoga.so:')
            patch_so_16kb(so, tmp_out)
            os.replace(tmp_out, so)

        os.makedirs(os.path.dirname(os.path.abspath(dst_aar)), exist_ok=True)
        print(f'\nRepacking -> {os.path.basename(dst_aar)} ...')
        with zipfile.ZipFile(dst_aar, 'w') as zout:
            for dirpath, _, files in os.walk(work):
                for fname in files:
                    fpath = os.path.join(dirpath, fname)
                    arcname = os.path.relpath(fpath, work)
                    compress = zipfile.ZIP_STORED if fname.endswith('.so') else zipfile.ZIP_DEFLATED
                    zout.write(fpath, arcname, compress_type=compress)
        print('Done.')
    finally:
        shutil.rmtree(work, ignore_errors=True)


def verify_aar(aar_path):
    print(f'\n=== Verification: {os.path.basename(aar_path)} ===')
    with zipfile.ZipFile(aar_path, 'r') as z:
        for name in sorted(z.namelist()):
            if not name.endswith('.so'):
                continue
            data = bytearray(z.read(name))
            if data[:4] != b'\x7fELF' or data[4] != 2:
                continue
            e = '<' if data[5] == 1 else '>'
            e_phoff = _u(e + 'Q', data, 32)
            e_phentsize = _u(e + 'H', data, 54)
            e_phnum = _u(e + 'H', data, 56)
            for i in range(e_phnum):
                b = e_phoff + i * e_phentsize
                p_type = _u(e + 'I', data, b)
                p_off  = _u(e + 'Q', data, b + 8)
                p_va   = _u(e + 'Q', data, b + 16)
                p_al   = _u(e + 'Q', data, b + 48)
                if p_type == PT_LOAD and p_off > 0:
                    ok = (p_off % NEW_ALIGN == p_va % NEW_ALIGN) and p_al >= NEW_ALIGN
                    print(f'  {"[OK]" if ok else "[FAIL]"} {name}: '
                          f'p_offset={hex(p_off)}, p_vaddr={hex(p_va)}, p_align={hex(p_al)}')


if __name__ == '__main__':
    script_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.dirname(script_dir)

    # Find the cached beagle-yoga 1.19.0 AAR
    gradle_cache = os.path.expanduser('~/.gradle/caches/modules-2/files-2.1')
    src_aar = os.path.join(
        gradle_cache,
        'br.com.zup.beagle', 'beagle-yoga', '1.19.0',
        'dd8eaa0eef3952d661919c047891fe17e5c47c2c',
        'beagle-yoga-1.19.0.aar'
    )

    if not os.path.exists(src_aar):
        # Fallback: search for it
        base = os.path.join(gradle_cache, 'br.com.zup.beagle', 'beagle-yoga', '1.19.0')
        if os.path.isdir(base):
            for root, _, files in os.walk(base):
                for f in files:
                    if f.endswith('.aar'):
                        src_aar = os.path.join(root, f)
                        break
        if not os.path.exists(src_aar):
            print(f'ERROR: Could not find beagle-yoga-1.19.0.aar in Gradle cache.')
            print('Run: ./gradlew dependencies first to download it.')
            exit(1)

    dst_aar = os.path.join(project_root, 'beagle', 'libs', 'beagle-yoga-1.19.0-16k.aar')
    patch_yoga_aar(src_aar, dst_aar)
    verify_aar(dst_aar)

