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

/**
 * Abstract implementation of {@link AuthService}
 */
abstract class AbstractAuthService implements AuthService {
    private AuthCallback authCallback = null;
    private Object lock = new Object();

    final void onInit(AuthInit authInit) {
        synchronized (lock) {
            unsetCallback().onInit(authInit);
        }
    }

    final void onError(AuthError authError) {
        synchronized (lock) {
            unsetCallback().onError(authError);
        }
    }

    final void onCancelled(AuthCancel cancel) {
        synchronized (lock) {
            unsetCallback().onCancel(cancel);
        }
    }

    final void onSuccess(AuthData authData) {
        synchronized (lock) {
            unsetCallback().onData(authData);
        }
    }

    final private AuthCallback unsetCallback() {
        AuthCallback cb = authCallback;
        authCallback = null;
        return cb;
    }

    protected void setCallback(AuthCallback callback) {
        if (callback == null) {
            throw new NullPointerException("Callback cannot be null");
        }

        synchronized (lock) {
            if (authCallback != null && callback != authCallback) {
                throw new IllegalArgumentException("Auth service is currently busy");
            }

            authCallback = callback;
        }
    }

    @Override
    public boolean isBusy() {
        synchronized (lock) {
            return authCallback != null;
        }
    }

    protected AuthInit createAuthInit() {
        return new AuthInit(getId());
    }

    protected AuthCancel createAuthCancel() {
        return new AuthCancel(getId());
    }

    protected AuthData createAuthData() {
        return new AuthData(getId());
    }

    protected AuthData createAuthData(String token, String displayName, String imageUrl) {
        return new AuthData(getId(), token, displayName, imageUrl);
    }

    protected AuthError createAuthError(String message) {
        return new AuthError(getId(), message);
    }

    protected AuthError createAuthError(Throwable cause) {
        return new AuthError(getId(), cause);
    }

    protected AuthError createAuthError(String message, Throwable cause) {
        return new AuthError(getId(), message, cause);
    }
}
