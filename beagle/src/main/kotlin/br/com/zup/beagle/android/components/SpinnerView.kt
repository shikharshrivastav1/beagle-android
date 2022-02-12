package br.com.zup.beagle.android.components

import android.content.Context
import android.view.View
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.Spinner
import br.com.zup.beagle.R
import br.com.zup.beagle.android.widget.RootView
import br.com.zup.beagle.android.widget.WidgetView
import br.com.zup.beagle.annotation.RegisterWidget

@RegisterWidget("beagleSpinner")
class SpinnerView (
    private val entries: List<String>,
) : WidgetView() {

    override fun buildView(rootView: RootView): View {
        return SpinnerComponent(rootView.getContext()).apply {
            setItems(entries)
        }
    }

    class SpinnerComponent(context: Context) : FrameLayout(context) {
        private val beagleSpinner: Spinner

        init {
            View.inflate(context, R.layout.beagle_spinner_view, this)
            beagleSpinner = findViewById(R.id.beagleSpinner)
        }

        fun setItems(entries: List<String>) {
            beagleSpinner.adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, entries)
        }

    }
}