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

package org.bbqapp.android.view.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.SignInButton;

import org.bbqapp.android.R;
import org.bbqapp.android.auth.AuthCallback;
import org.bbqapp.android.auth.AuthData;
import org.bbqapp.android.auth.GooglePlus;
import org.bbqapp.android.view.BaseFragment;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Fragment for user login operations
 */
public class LoginFragment extends BaseFragment {

    private static final String TAG = LoginFragment.class.getName();

    @Bind(R.id.google_login_button)
    SignInButton googleLoginButton;

    @Inject
    GooglePlus googlePlus;

    private AuthData authData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_login, container, false);

        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.menu_login);

        googlePlus.init();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.google_login_button)
    protected void onGoogleLoginButtonClick() {
        AuthCallback authCallback = new AuthCallback() {
            @Override
            public void onSuccess(AuthData authData) {
                LoginFragment.this.authData = authData;
                String msg = authData != null ? authData.getDisplayName() + " is logged in." : "logged out.";
                Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
                onFinish();
            }

            @Override
            public void onCancel() {
                Toast.makeText(getActivity(), "login cancelled", Toast.LENGTH_LONG).show();
                onFinish();
            }

            @Override
            public void onError(Exception cause) {
                Toast.makeText(getActivity(), "Error occurred during authorization", Toast.LENGTH_LONG).show();
                onFinish();
            }

            void onFinish() {
                googleLoginButton.setEnabled(true);
            }
        };

        googleLoginButton.setEnabled(false);

        if (authData == null) {
            googlePlus.login(authCallback);
        } else {
            googlePlus.logout(authCallback);
        }
    }
}
