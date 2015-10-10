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

import android.content.Context;
import android.content.Intent;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;

/**
 *
 */
public class Facebook extends AbstractAuthService {
    public static final String ID = "facebook";

    private Context context;
    private CallbackManager callbackManager;

    public Facebook(Context context) {
        this.context = context;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void init(AuthCallback callback) {
        setCallback(callback);
        FacebookSdk.sdkInitialize(context, new FacebookSdk.InitializeCallback() {

            @Override
            public void onInitialized() {
                onInit(createAuthInit());
            }
        });
        callbackManager = CallbackManager.Factory.create();
    }

    @Override
    public boolean isInitialized() {
        return FacebookSdk.isInitialized();
    }

    @Override
    public void login(AuthCallback callback) {
        setCallback(callback);

        // TODO implementation
        onError(createAuthError("not implemented"));
    }

    @Override
    public void logout(AuthCallback callback) {
        setCallback(callback);

        // TODO implementation
        onError(createAuthError("not implemented"));
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
