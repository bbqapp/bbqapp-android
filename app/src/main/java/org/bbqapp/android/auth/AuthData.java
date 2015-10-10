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
 * Holder of authentication information
 */
public class AuthData {
    private String authServiceId;
    private String token;
    private String displayName;
    private String imageUrl;

    AuthData(String authServiceId) {
        if (authServiceId == null) {
            throw new NullPointerException("Auth service id cannot be null");
        }
        this.authServiceId = authServiceId;
    }

    AuthData(String authServiceId, String token, String displayName, String imageUrl) {
        this(authServiceId);
        this.token = token;
        this.displayName = displayName;
        this.imageUrl = imageUrl;
    }

    public String getAuthServiceId() {
        return authServiceId;
    }

    public String getToken() {
        return token;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    @Override
    public final String toString() {
        return super.toString();
    }
}
