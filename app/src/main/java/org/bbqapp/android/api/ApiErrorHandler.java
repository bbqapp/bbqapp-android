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
import org.bbqapp.android.api.exception.ConnectionException;
import org.bbqapp.android.api.exception.ResponseException;
import org.bbqapp.android.api.model.Error;

import retrofit.ErrorHandler;
import retrofit.RetrofitError;

/**
 * Api exception handler to. Maps occurred exceptions to {@link ApiException}
 */
class ApiErrorHandler implements ErrorHandler {
    @Override
    public Throwable handleError(RetrofitError cause) {
        return convert(cause);
    }

    /**
     * Converts {@link RetrofitError} to {@link ApiException}
     *
     * @param cause exception to convert
     * @return converted exception
     */
    static ApiException convert(RetrofitError cause) {
        if (cause.getResponse() == null) {
            return new ConnectionException(cause);
        } else {
            try {
                Error error = (Error) cause.getBodyAs(Error.class);
                return new ResponseException(error, cause);
            } catch (RuntimeException e) {
                return new ConnectionException(cause);
            }
        }
    }
}
