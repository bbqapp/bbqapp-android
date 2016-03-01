/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 bbqapp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.bbqapp.android.view

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import butterknife.ButterKnife
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.android.appKodein
import com.squareup.leakcanary.RefWatcher
import com.trello.rxlifecycle.components.support.RxAppCompatActivity
import dagger.ObjectGraph
import org.bbqapp.android.Injector
import timber.log.Timber

open class BaseActivity : RxAppCompatActivity(), Injector {
    private val injector = KodeinInjector()
    private var objectGraph: ObjectGraph? = null

    private val refWatcher: RefWatcher by injector.instance()

    private fun prepareAndInject(context: Context) {
        val injector = context as? Injector
        if (objectGraph == null && injector != null && injector.objectGraph != null) {
            objectGraph = injector.objectGraph.plus(*Array(modules.size, { modules[it] }))
            inject(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // dagger
        prepareAndInject(applicationContext)

        // kodein
        injector.inject(appKodein())
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)

        // butterknife
        ButterKnife.bind(this)
    }

    override fun setContentView(view: View) {
        super.setContentView(view)

        // butterknife
        ButterKnife.bind(this)
    }

    override fun setContentView(view: View, params: ViewGroup.LayoutParams) {
        super.setContentView(view, params)

        // butterknife
        ButterKnife.bind(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        // dagger
        objectGraph = null

        // butterknife
        ButterKnife.unbind(this)

        refWatcher.watch(this)
    }

    @Deprecated(message = "Use Kodein instead")
    override fun getObjectGraph(): ObjectGraph? {
        return objectGraph
    }

    @Deprecated(message = "Use Kodein instead")
    override fun inject(o: Any) {
        try {
            objectGraph?.inject(o)
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "Could not inject $localClassName.");
        }
    }

    @Deprecated(message = "Use Kodein instead")
    protected val modules: List<Any>
        get() = listOf(ActivityModule(this))
}