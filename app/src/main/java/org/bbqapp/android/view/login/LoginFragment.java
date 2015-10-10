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

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.SignInButton;

import org.bbqapp.android.R;
import org.bbqapp.android.auth.AuthInit;
import org.bbqapp.android.auth.Facebook;
import org.bbqapp.android.auth.GooglePlus;
import org.bbqapp.android.view.BaseFragment;
import org.bbqapp.android.view.LoginManager;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.halfbit.tinybus.Subscribe;

/**
 * Fragment for user login operations
 */
public class LoginFragment extends BaseFragment {

    private static final String TAG = LoginFragment.class.getName();

    @Inject
    LoginManager loginManager;

    @Inject
    Activity activity;

    @Bind(R.id.login_buttons)
    LinearLayout loginButtons;

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

        loginManager.init();
    }

    @Override
    public void onPause() {
        super.onPause();

        loginButtons.removeAllViews();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    //@OnClick(R.id.google_login_button)
    protected void onGoogleLoginButtonClick() {
        if (loginManager.getLastAuthData() != null && loginManager.getLastAuthData().isLoggedIn()) {
            loginManager.logout();
        } else {
            loginManager.login(GooglePlus.ID);
        }
    }

    //@OnClick(R.id.facebook_login_button)
    protected void onFacebookLoginButtonClick() {
        if (loginManager.getLastAuthData() != null && loginManager.getLastAuthData().isLoggedIn()) {
            loginManager.logout();
        } else {
            loginManager.login(Facebook.ID);
        }
    }

    @Subscribe
    public void onAuthInit(AuthInit authInit) {
        View button;
        switch (authInit.getAuthServiceId()) {
            case GooglePlus.ID:
                button = new SignInButton(activity);
                break;
            case Facebook.ID:
                button = new LoginButton(activity);
                break;
            default:
                button = null;
        }

        if (button != null) {
            loginButtons.addView(button);
        }
    }
}
