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

package org.bbqapp.android.api

import android.net.Uri
import com.squareup.picasso.Picasso
import com.squareup.picasso.Request

class PicassoPictureRequestTransformer(private val apiUrl: String) : Picasso.RequestTransformer {

    override fun transformRequest(request: Request?): Request? {
        val host = request?.uri?.host
        val scheme = request?.uri?.scheme
        if ((scheme == null || scheme.isEmpty()) && (host == null || host.isEmpty())) {
            var originalUri = request?.uri?.toString()
            if (originalUri != null) {
                if (originalUri.startsWith("/") && apiUrl.endsWith("/")) {
                    originalUri = originalUri.substring(1)
                }

                return request?.buildUpon()?.setUri(Uri.parse(apiUrl + originalUri))?.build()
            }
        }
        return request
    }
}
