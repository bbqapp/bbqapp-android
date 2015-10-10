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

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;

import java.io.IOException;

/**
 * Google Plus auth service
 */
public class GooglePlus extends AbstractAuthService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient
        .OnConnectionFailedListener {
    private GoogleApiClient googleApiClient;
    private Activity activity;

    public static final int PLUS_RESOLUTION_REQUEST_CODE = 154;

    private boolean operationLogin;

    public GooglePlus(Activity activity) {
        this.activity = activity;
    }

    @Override
    public String getId() {
        return "plus";
    }

    @Override
    public void init() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(activity)
                    .addApi(Plus.API)
                    .addScope(Plus.SCOPE_PLUS_LOGIN)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
    }

    @Override
    public void login(AuthCallback callback) {
        setCallback(callback);
        operationLogin = true;
        googleApiClient.connect();
    }

    @Override
    public void logout(AuthCallback callback) {
        setCallback(callback);
        operationLogin = false;
        googleApiClient.connect();
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLUS_RESOLUTION_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                googleApiClient.connect();
            } else {
                googleApiClient.disconnect();
                onCancelled();
            }

            return true;
        }
        return false;
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (operationLogin) {
            final String displayName = Plus.PeopleApi.getCurrentPerson(googleApiClient).getDisplayName();
            final String accountName = Plus.AccountApi.getAccountName(googleApiClient);
            new AsyncTask<Void, Void, Void>() {
                AuthData authData;
                Exception cause;

                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        String token = GoogleAuthUtil.getToken(activity, accountName, "oauth2:" + Plus.SCOPE_PLUS_LOGIN);
                        authData = new AuthData(getId(), token, displayName);
                    } catch (IOException | GoogleAuthException e) {
                        cause = e;
                    }
                    googleApiClient.disconnect();
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);

                    if (cause != null) {
                        onError(cause);
                    } else {
                        onSuccess(authData);
                    }
                }
            }.execute();
        } else {
            Plus.AccountApi.clearDefaultAccount(googleApiClient);
            Plus.AccountApi.revokeAccessAndDisconnect(googleApiClient).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    googleApiClient.disconnect();
                    if (status.isSuccess()) {
                        onSuccess(null);
                    } else if (status.isCanceled()) {
                        onCancelled();
                    } else {
                        onError(new Exception(status.getStatusMessage()));
                    }
                }
            });
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.disconnect();
        onError(new Exception("No connection"));
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(activity, PLUS_RESOLUTION_REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                googleApiClient.connect();
            }
        } else {
            googleApiClient.disconnect();
            onError(new Exception(connectionResult.getErrorMessage()));
            //GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), activity, 0).show();
        }
    }
}
