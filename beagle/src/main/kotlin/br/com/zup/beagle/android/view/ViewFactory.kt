/*
 * Copyright 2020 ZUP IT SERVICOS EM TECNOLOGIA E INOVACAO SA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.zup.beagle.android.view

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.View
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import br.com.zup.beagle.R
import br.com.zup.beagle.android.components.BeagleRecyclerView
import br.com.zup.beagle.android.components.utils.RoundedImageView
import br.com.zup.beagle.android.view.custom.BeagleFlexView
import br.com.zup.beagle.android.view.custom.BeaglePageIndicatorView
import br.com.zup.beagle.android.view.custom.BeaglePageView
import br.com.zup.beagle.android.view.custom.BeagleTabLayout
import br.com.zup.beagle.android.view.custom.BeagleView
import br.com.zup.beagle.android.widget.RootView
import br.com.zup.beagle.core.CornerRadius
import br.com.zup.beagle.core.Style

internal object ViewFactory {

    fun makeView(context: Context) = View(context)

    fun makeBeagleView(rootView: RootView) = BeagleView(rootView)

    fun makeBeagleFlexView(rootView: RootView) = BeagleFlexView(rootView)

    fun makeBeagleFlexView(rootView: RootView, style: Style) = BeagleFlexView(rootView, style)

    fun makeBeagleFlexView(
        rootView: RootView,
        style: Style,
        styleId: Int,
    ) = BeagleFlexView(
        rootView,
        style,
        styleId = styleId,
    )

    fun makeScrollView(context: Context) = ScrollView(context).apply {
        isFillViewport = true
    }

    fun makeHorizontalScrollView(context: Context) = HorizontalScrollView(context).apply {
        isFillViewport = true
    }

    fun makeButton(context: Context, id: Int) = AppCompatButton(ContextThemeWrapper(context, id), null, 0)

    fun makeButton(context: Context) = AppCompatButton(context)

    fun makeTextView(context: Context) = AppCompatTextView(context)

    fun makeTextView(context: Context, id: Int) = AppCompatTextView(ContextThemeWrapper(context, id), null, 0)

    fun makeInputText(context: Context, id: Int) = AppCompatEditText(ContextThemeWrapper(context, id), null, 0)

    fun makeInputText(context: Context) = AppCompatEditText(context)

    fun makeAlertDialogBuilder(context: Context) = AlertDialog.Builder(context)

    fun makeFrameLayoutParams(width: Int, height: Int) = FrameLayout.LayoutParams(width, height)

    fun makeViewPager(context: Context) = BeaglePageView(context)

    fun makePageIndicator(context: Context) = BeaglePageIndicatorView(context)

    fun makeTabLayout(context: Context, id: Int) = BeagleTabLayout(ContextThemeWrapper(context, id), null, 0)

    //we use the context.applicationContext to prevent a crash on android 21
    fun makeWebView(context: Context) = WebView(context.applicationContext)

    fun makeImageView(context: Context, cornerRadius: CornerRadius) = RoundedImageView(context, cornerRadius)

    fun makeRecyclerView(context: Context) = RecyclerView(context)

    fun makeBeagleRecyclerView(context: Context) = BeagleRecyclerView(context)

    fun makeBeagleRecyclerViewScrollIndicatorHorizontal(context: Context) =
        BeagleRecyclerView(ContextThemeWrapper(context, R.style.Beagle_Widget_ScrollIndicatorHorizontal))

    fun makeBeagleRecyclerViewScrollIndicatorVertical(context: Context) =
        BeagleRecyclerView(ContextThemeWrapper(context, R.style.Beagle_Widget_ScrollIndicatorVertical))

    fun makeSwipeRefreshLayout(context: Context) = SwipeRefreshLayout(context)
}
