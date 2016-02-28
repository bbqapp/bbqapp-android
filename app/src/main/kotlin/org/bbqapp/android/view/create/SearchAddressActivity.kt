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
import kotlinx.android.synthetic.main.activity_search_address.*

import android.os.Bundle
import android.os.Parcelable
import com.jakewharton.rxbinding.support.v7.widget.navigationClicks
import com.jakewharton.rxbinding.widget.itemClicks
import com.jakewharton.rxbinding.widget.textChanges
import com.trello.rxlifecycle.components.support.RxAppCompatActivity
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import org.bbqapp.android.R
import org.bbqapp.android.service.GeocodeService
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class SearchAddressActivity : RxAppCompatActivity() {

    val adapter = LocationListAdapter()

    var resolveSubscription: Subscription? = null

    val geocoder = GeocodeService(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_search_address)

        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.navigationClicks().bindToLifecycle(this).subscribe { onBackPressed() }

        suggestions.adapter = adapter;
        suggestions.itemClicks().bindToLifecycle(this).subscribe {
            val address = adapter.getItem(it)
            val intent = Intent()
            intent.putExtra("address", address)
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()


        search.textChanges()
                .doOnNext { if (it.length <= 1) adapter.clear() }
                .filter { it.length > 1 }
                .debounce(250, TimeUnit.MILLISECONDS)
                .bindToLifecycle(this)
                .subscribe { resolve(it) }
    }

    private fun resolve(text: CharSequence) {
        resolveSubscription?.unsubscribe()

        resolveSubscription = geocoder.resolve(text, 10)
                .buffer(10)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .bindToLifecycle(this)
                .subscribe({ adapter.setLocations(it as List<Parcelable>) })
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, SearchAddressActivity::class.java);
        }
    }
}