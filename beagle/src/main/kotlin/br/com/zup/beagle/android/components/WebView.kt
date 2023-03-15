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

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import android.view.View
import android.webkit.HttpAuthHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import br.com.zup.beagle.android.action.Action
import br.com.zup.beagle.android.annotation.RegisterWidget
import br.com.zup.beagle.android.context.Bind
import br.com.zup.beagle.android.context.expressionOrConstant
import br.com.zup.beagle.android.utils.handleEvent
import br.com.zup.beagle.android.utils.observeBindChanges
import br.com.zup.beagle.android.view.BeagleActivity
import br.com.zup.beagle.android.view.ServerDrivenState
import br.com.zup.beagle.android.view.ViewFactory
import br.com.zup.beagle.android.widget.RootView
import br.com.zup.beagle.android.widget.WidgetView

/**
 * A WebView widget will define a WebView natively using the server driven information received through Beagle.
 *
 * @param url
 *              define the initial page that the WebView will load when presented .
 *              This attribute must be declared and it cannot be null.
 *
 */
@RegisterWidget("webView")
data class WebView(
    val url: Bind<String>,
    val basicAuthUsername: String? = null,
    val basicAuthPassword: String? = null,
    val onPageStarted: List<Action>? = null,
    val onPageFinished: List<Action>? = null,
    val onReceivedError: List<Action>? = null,
) : WidgetView() {

    constructor(url: String) : this(expressionOrConstant(url))

    @SuppressLint("SetJavaScriptEnabled")
    override fun buildView(rootView: RootView): View {
        val webView = ViewFactory.makeWebView(rootView.getContext())
        webView.webViewClient = BeagleWebViewClient(rootView.getContext(), basicAuthUsername, basicAuthPassword, this, rootView)
        webView.clearCache(true)
        webView.settings.run {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            allowContentAccess = true
            javaScriptCanOpenWindowsAutomatically = true
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
        }
        observeBindChanges(rootView, webView, url) {
            it?.let { webView.loadUrl(it) }
        }
        return webView
    }

    class BeagleWebViewClient(val context: Context, val basicAuthUsername: String? = null, val basicAuthPassword: String? = null,
                              val beagleWebView: br.com.zup.beagle.android.components.WebView? = null, val rootView: RootView) : WebViewClient() {

        override fun onPageFinished(view: WebView?, url: String?) {
            notify(loading = false)
            view?.requestLayout()
            this.beagleWebView?.run {
                view?.let { it1 ->
                    this.onPageFinished?.let { actions ->
                        try {
                            handleEvent(
                                rootView,
                                it1,
                                actions
                            )
                        }
                        catch (e: IllegalStateException) {

                        }
                    }
                }
            }
        }

        override fun onReceivedHttpAuthRequest(view: WebView?, handler: HttpAuthHandler?, host: String?, realm: String?) {
            basicAuthUsername?.let {
                handler?.proceed(it, basicAuthPassword)
                return
            }
            handler?.cancel()
        }

        override fun onPageStarted(
            view: WebView?,
            url: String?,
            favicon: Bitmap?,
        ) {
            notify(loading = true)
            Log.v("WebViewBeagle", "onPageStarted")
            this.beagleWebView?.run {
                view?.let { it1 ->
                    this.onPageStarted?.let { actions ->
                        try {
                            handleEvent(
                                rootView,
                                it1,
                                actions
                            )
                        }
                        catch (e: IllegalStateException) {

                        }
                    }
                }
            }
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?,
        ) {
            val throwable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Error("$error")
            } else {
                Error("received error")
            }
            notify(state = ServerDrivenState.WebViewError(throwable) { view?.reload() })
            this.beagleWebView?.run {
                view?.let { it1 ->
                    this.onReceivedError?.let { actions ->
                        try {
                            handleEvent(
                                rootView,
                                it1,
                                actions
                            )
                        }
                        catch (e: Exception) {}
                    }
                }
            }
        }

        private fun notify(loading: Boolean) {
            if (loading) {
                notify(state = ServerDrivenState.Started)
            } else {
                notify(state = ServerDrivenState.Finished)
            }
        }

        private fun notify(state: ServerDrivenState) {
            (context as? BeagleActivity)?.onServerDrivenContainerStateChanged(state)
        }
    }

}