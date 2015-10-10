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

package org.bbqapp.android.auth;

import android.preference.PreferenceManager;

/**
 * Interface for all auth service like g+, facebook, ...
 */
public interface AuthService extends PreferenceManager.OnActivityResultListener {

    /**
     * Returns id of service provider
     * @return
     */
    String getId();

    /**
     * Initialize sdk but without login
     */
    void init();

    /**
     * Initialize sdk with login
     * @param callback callback to call
     */
    void init(AuthCallback callback);

    /**
     * Performs login
     * @param callback callback to call
     */
    void login(AuthCallback callback);

    /**
     * Performs logout
     * @param callback callback to call
     */
    void logout(AuthCallback callback);

    /**
     * Determines if some operation currently in progress
     * @return {@code true} if service is currently busy {@code false} otherwise
     */
    boolean isBusy();
}
