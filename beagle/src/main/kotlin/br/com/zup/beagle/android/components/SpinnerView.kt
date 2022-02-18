package br.com.zup.beagle.android.components

import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.Spinner
import br.com.zup.beagle.R
import br.com.zup.beagle.android.action.Action
import br.com.zup.beagle.android.context.ContextData
import br.com.zup.beagle.android.utils.handleEvent
import br.com.zup.beagle.android.widget.RootView
import br.com.zup.beagle.android.widget.WidgetView
import br.com.zup.beagle.annotation.RegisterWidget

@RegisterWidget("beagleSpinner")
class SpinnerView (
    private val entries: List<String>,
    val onSelected: List<Action>? = null,
) : WidgetView() {

    override fun buildView(rootView: RootView): View {
        return SpinnerComponent(rootView.getContext()).apply {
            setItems(entries)
            beagleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    onSelected?.let {
                        this@SpinnerView.handleEvent(
                            rootView,
                            beagleSpinner,
                            it,
                            ContextData(
                                id = "onSelected",
                                value = entries[position]
                            )
                        )
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }
        }
    }

    class SpinnerComponent(context: Context) : FrameLayout(context) {
        val beagleSpinner: Spinner

        init {
            View.inflate(context, R.layout.beagle_spinner_view, this)
            beagleSpinner = findViewById(R.id.beagleSpinner)
        }

        fun setItems(entries: List<String>) {
            beagleSpinner.adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, entries)
        }

    }
}