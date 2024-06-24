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

package br.com.zup.beagle.android.action

import android.content.Context
import android.view.View
import br.com.zup.beagle.android.analytics.ActionAnalyticsConfig
import br.com.zup.beagle.android.widget.RootView
import br.com.zup.beagle.android.widget.core.BeagleJson

@BeagleJson(name = "keyValueStore")
data class KeyValueStoreAction(
    val key: String,
    val value: String,
    val prefName: String,
    val sync: Boolean? = false,
    override var analytics: ActionAnalyticsConfig? = null,
) : AnalyticsAction {
    override fun execute(rootView: RootView, origin: View) {
        val sharedPrefs = rootView.getContext().getSharedPreferences(prefName, Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        // Store the value based on its type (you'll need to handle different types here)
        editor.putString(key, value)
        if (sync == true) {
            editor.commit();
        } else {
            editor.apply() // Asynchronous save
        }
    }
}