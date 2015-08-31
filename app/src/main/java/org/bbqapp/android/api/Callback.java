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

package org.bbqapp.android.api;

import org.bbqapp.android.api.exception.ApiException;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Callback for async API requests
 */
public abstract class Callback<T> implements retrofit.Callback<T> {
    @Override
    public final void success(T t, Response response) {
        onSuccess(t);
    }

    @Override
    public final void failure(RetrofitError error) {
        onFailure(ApiErrorHandler.convert(error));
    }

    /**
     * Success API request
     *
     * @param t received object
     */
    public abstract void onSuccess(T t);

    /**
     * Unsuccessful API request
     *
     * @param cause request exception
     */
    public abstract void onFailure(ApiException cause);
}
