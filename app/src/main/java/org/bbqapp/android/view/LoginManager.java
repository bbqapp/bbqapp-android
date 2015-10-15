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

package org.bbqapp.android.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.bbqapp.android.auth.AuthCallback;
import org.bbqapp.android.auth.AuthCancel;
import org.bbqapp.android.auth.AuthData;
import org.bbqapp.android.auth.AuthError;
import org.bbqapp.android.auth.AuthInit;
import org.bbqapp.android.auth.AuthService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.halfbit.tinybus.Produce;
import de.halfbit.tinybus.TinyBus;

/**
 * Manages the login
 */
public class LoginManager implements AuthCallback, PreferenceManager.OnActivityResultListener {
    private static final String PREFERENCES_NAME = "auth";
    private static final String PREFERENCES_SERVICE_ID_NAME = "authServiceId";

    private AuthData authData;
    private TinyBus bus;
    private Map<String, AuthService> services = new ConcurrentHashMap<>();

    private SharedPreferences preferences;

    // used to sync event dispatching and busy state
    private final Object lock = new Object();

    LoginManager(Context context, TinyBus bus, AuthService... services) {
        this.bus = bus;
        this.preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);

        for (AuthService service : services) {
            this.services.put(service.getId(), service);
        }
    }

    public boolean login() {
        if (authData != null && authData.isLoggedIn()) {
            return false;
        }

        String serviceId = preferences.getString(PREFERENCES_SERVICE_ID_NAME, null);
        if (serviceId != null) {
            login(serviceId);
        }

        return serviceId != null;
    }

    @Produce
    public AuthData getLastAuthData() {
        return authData;
    }

    public void login(String authServiceId) {
        if (authData != null && authData.isLoggedIn() && authData.getAuthServiceId().equals(authServiceId)) {
            throw new IllegalArgumentException("You must log out before you can log in");
        }

        final AuthService service = services.get(authServiceId);
        service.init(new AuthCallback() {
            @Override
            public void onData(AuthData authData) {
                LoginManager.this.onData(authData);
            }

            @Override
            public void onCancel(AuthCancel authCancel) {
                LoginManager.this.onCancel(authCancel);
            }

            @Override
            public void onError(AuthError authError) {
                LoginManager.this.onError(authError);
            }

            @Override
            public void onInit(AuthInit authInit) {
                service.login(this);
            }
        });
    }

    public void logout() {
        if (authData != null && authData.isLoggedIn()) {
            AuthService service = services.get(authData.getAuthServiceId());
            service.logout(this);
        }
    }

    @Override
    public void onData(AuthData authData) {
        synchronized (lock) {
            this.authData = authData;

            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(PREFERENCES_SERVICE_ID_NAME);
            if (authData.isLoggedIn()) {
                editor.putString(PREFERENCES_SERVICE_ID_NAME, authData.getAuthServiceId());
            }
            editor.apply();

            bus.post(authData);
        }
    }

    @Override
    public void onCancel(AuthCancel authCancel) {
        synchronized (lock) {
            bus.post(authCancel);
        }
    }

    @Override
    public void onError(AuthError authError) {
        synchronized (lock) {
            bus.post(authError);
        }

    }

    @Override
    public void onInit(AuthInit authInit) {
        synchronized (lock) {
            bus.post(authInit);
        }

    }

    public boolean isBusy() {
        synchronized (lock) {
            for (AuthService service : services.values()) {
                if (service.isBusy()) {
                    return true;
                }
            }

            return false;
        }
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        for (AuthService service : services.values()) {
            if (service.onActivityResult(requestCode, resultCode, data)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Initializes all auth services
     */
    public void init() {
        for (AuthService service : services.values()) {
            service.init(this);
        }
    }
}
