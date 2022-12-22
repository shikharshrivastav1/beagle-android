package br.com.zup.beagle.android.components

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.widget.FrameLayout
import android.widget.TextClock
import br.com.zup.beagle.R
import br.com.zup.beagle.android.widget.RootView
import br.com.zup.beagle.android.widget.WidgetView
import br.com.zup.beagle.android.annotation.RegisterWidget

@RegisterWidget("beagleTextClock")
class BeagleTextClock(
    private val textSize: Float?,
    private val clock12HrFormat: String?,
    private val clock24HrFormat: String?,
    private val textStyle: TextStyle?
) : WidgetView() {

    override fun buildView(rootView: RootView): View {
        return BeagleTextClockComponent(rootView.getContext()).apply {
            textSize?.let { setTextSize(it) }
            clock12HrFormat?.let { setClock12HrFormat(it) }
            clock24HrFormat?.let { setClock24HrFormat(it) }
            textStyle?.let { setTextStyle(it) }
        }
    }

    class BeagleTextClockComponent(context: Context) : FrameLayout(context) {
        private val clockBeagleComp: TextClock

        init {
            View.inflate(context, R.layout.beagle_custom_clock, this)
            clockBeagleComp = findViewById(R.id.clockBeagleComp)
        }

        fun setClock12HrFormat(format12hr: String) {
            clockBeagleComp.format12Hour = format12hr
        }

        fun setClock24HrFormat(format24hr: String) {
            clockBeagleComp.format24Hour = format24hr
        }

        fun setTextSize(textSize: Float) {
            clockBeagleComp.textSize = textSize
        }

        fun setTextStyle(textStyle: TextStyle) {
            var typeFace: Typeface? = null
            when (textStyle) {
                TextStyle.BOLD -> {
                    clockBeagleComp.setTypeface(clockBeagleComp.typeface, Typeface.BOLD)
                }
                TextStyle.ITALIC -> {
                    clockBeagleComp.setTypeface(clockBeagleComp.typeface, Typeface.ITALIC)
                }
                TextStyle.BOLD_ITALIC -> {
                    clockBeagleComp.setTypeface(clockBeagleComp.typeface,  Typeface.BOLD_ITALIC)
                }
            }
        }
    }
}