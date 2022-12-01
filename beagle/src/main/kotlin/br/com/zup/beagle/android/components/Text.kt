/*
 * Copyright 2020, 2022 ZUP IT SERVICOS EM TECNOLOGIA E INOVACAO SA
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

package br.com.zup.beagle.android.components

import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.TextView
import br.com.zup.beagle.android.context.Bind
import br.com.zup.beagle.android.utils.StyleManager
import br.com.zup.beagle.android.utils.observeBindChanges
import br.com.zup.beagle.android.utils.toAndroidColor
import br.com.zup.beagle.android.view.ViewFactory
import br.com.zup.beagle.android.widget.RootView
import br.com.zup.beagle.android.widget.WidgetView
import br.com.zup.beagle.android.annotation.RegisterWidget
import br.com.zup.beagle.android.widget.core.BeagleJson
import br.com.zup.beagle.android.widget.core.TextAlignment

@BeagleJson
enum class TextStyle {
    BOLD, ITALIC, BOLD_ITALIC
}

/**
 * A text widget will define a text view natively using the server driven information received through Beagle.
 *
 * @param text defines the text view content. This attribute must be declared and it cannot be null.
 * @param styleId
 *              will reference a style in your local styles file to be applied on this text view.
 *              This attribute can be set as null.
 * @param textColor defines the text color natively.
 * @param alignment defines the text content alignment inside the text view.
 *
 */
@RegisterWidget("text")
data class Text(
    val text: Bind<String>,
    val styleId: String? = null,
    val textSize: Float? = null,
    val textTypeFace: TextStyle? = null,
    val textColor: Bind<String>? = null,
    val alignment: Bind<TextAlignment>? = null,
) : WidgetView() {

    @Transient
    private val styleManager: StyleManager = StyleManager()

    override fun buildView(rootView: RootView): View {
        styleManager.init(rootView.getBeagleConfigurator().designSystem)
        val textStyle = styleManager.getTextStyle(styleId)

        val textView = if (textStyle == 0) ViewFactory.makeTextView(rootView.getContext())
        else ViewFactory.makeTextView(rootView.getContext(), textStyle)
        textSize?.let { textView.textSize = it }
        textTypeFace?.let {
            when (it) {
                TextStyle.BOLD -> {
                    textView.setTypeface(textView.typeface, Typeface.BOLD)
                }
                TextStyle.ITALIC -> {
                    textView.setTypeface(textView.typeface, Typeface.ITALIC)
                }
                TextStyle.BOLD_ITALIC -> {
                    textView.setTypeface(textView.typeface, Typeface.BOLD_ITALIC)
                }
            }
        }
        textView.setTextWidget(this, rootView)
        return textView
    }

    private fun TextView.setTextWidget(text: Text, rootView: RootView) {
        observeBindChanges(rootView, this, text.text) {
            it?.let { this.text = it }
        }

        text.textColor?.let {
            observeBindChanges(rootView, this, it) { value ->
                value?.let { color -> this.setTextColor(color) }
            }
        }

        text.alignment?.let {
            observeBindChanges(rootView, this, it) { value ->
                value?.let { alignment -> this.setAlignment(alignment) }
            }
        }
    }

    private fun TextView.setAlignment(alignment: TextAlignment?) {
        gravity = when (alignment) {
            TextAlignment.CENTER -> Gravity.CENTER
            TextAlignment.RIGHT -> Gravity.END
            else -> Gravity.START
        }
    }

    private fun TextView.setTextColor(color: String?) {
        color?.toAndroidColor()?.let { androidColor -> setTextColor(androidColor) }
    }
}
