package br.com.zup.beagle.android.components

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.LinearLayout
import br.com.zup.beagle.R
import br.com.zup.beagle.android.widget.RootView
import br.com.zup.beagle.android.widget.WidgetView
import br.com.zup.beagle.annotation.RegisterWidget

@RegisterWidget("divider")
data class Divider(
    val styleId: String? = null,
    val dividerColor: String = "#000000"
) : WidgetView() {

    override fun buildView(rootView: RootView): View {
        return DividerComponent(context = rootView.getContext()).apply {
            setDividerColor(dividerColor)
        }
    }

    class DividerComponent(context: Context) : LinearLayout(context) {

        init {
            View.inflate(context,  R.layout.beagle_divider_horizontal, this)
        }

        fun setDividerColor(colorStr: String) {
            try {
                 val color = Color.parseColor(colorStr)
                findViewById<View>(R.id.divider).setBackgroundColor(color)
            }
            catch (e: Exception) {

            }
        }
    }

}