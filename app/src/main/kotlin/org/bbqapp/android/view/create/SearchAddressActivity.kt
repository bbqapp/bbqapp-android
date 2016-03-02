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

package org.bbqapp.android.view.create

import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import com.jakewharton.rxbinding.support.v7.widget.navigationClicks
import com.jakewharton.rxbinding.widget.itemClicks
import com.jakewharton.rxbinding.widget.textChanges
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.activity_search_address.*
import org.bbqapp.android.R
import org.bbqapp.android.extension.observeOnMainThread
import org.bbqapp.android.service.resolve
import org.bbqapp.android.view.BaseActivity
import rx.internal.operators.OperatorSwitch
import java.util.concurrent.TimeUnit

class SearchAddressActivity : BaseActivity() {

    lateinit var adapter: LocationListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_search_address)

        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.navigationClicks().bindToLifecycle(this).subscribe { onBackPressed() }

        intent?.getStringExtra("search")?.let {
            search.setText(it)
        }

        adapter = LocationListAdapter()
        suggestions.adapter = adapter;
        suggestions.itemClicks().bindToLifecycle(this).subscribe {
            finish(adapter.getItem(it) as Address)
        }
    }

    override fun onResume() {
        super.onResume()

        search.textChanges()
                .debounce(250, TimeUnit.MILLISECONDS)
                .map { Geocoder(this).resolve(it.toString(), 10) }
                .lift(OperatorSwitch.instance())
                .observeOnMainThread()
                .doOnError { Toast.makeText(this, R.string.resolve_error, Toast.LENGTH_LONG).show() }
                .retry()
                .bindToLifecycle(this)
                .subscribe { adapter.set(it) }
    }

    private fun finish(address: Address) {
        val intent = Intent()
        intent.putExtra("address", address)
        setResult(RESULT_OK, intent)
        finish()
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, SearchAddressActivity::class.java);
        }

        fun createIntent(context: Context, search: String): Intent {
            val intent = createIntent(context)
            intent.putExtra("search", search)
            return intent
        }
    }
}