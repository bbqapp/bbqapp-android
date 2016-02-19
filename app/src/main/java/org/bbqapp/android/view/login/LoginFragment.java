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
import android.widget.TextView;

import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.SignInButton;

import org.bbqapp.android.R;
import org.bbqapp.android.auth.AuthCancel;
import org.bbqapp.android.auth.AuthData;
import org.bbqapp.android.auth.AuthError;
import org.bbqapp.android.auth.AuthEvent;
import org.bbqapp.android.auth.AuthInit;
import org.bbqapp.android.auth.Facebook;
import org.bbqapp.android.auth.GooglePlus;
import org.bbqapp.android.view.BaseFragment;
import org.bbqapp.android.view.LoginManager;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.halfbit.tinybus.Subscribe;
import de.halfbit.tinybus.TinyBus;

/**
 * Fragment for user login operations
 */
public class LoginFragment extends BaseFragment implements View.OnClickListener {

    @Inject
    TinyBus bus;

    @Inject
    LoginManager loginManager;

    @Inject
    Activity activity;

    @Bind(R.id.login_buttons)
    LinearLayout loginButtons;

    @Bind(R.id.login_info)
    LinearLayout loginInfo;

    @Bind(R.id.login_name)
    TextView nameText;

    private final Object authEventsLock = new Object();

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

        bus.register(this);

        loginButtons.setVisibility(View.GONE);
        loginInfo.setVisibility(View.GONE);

        initOrUpdate();
    }

    @Override
    public void onPause() {
        super.onPause();

        bus.unregister(this);

        loginButtons.removeAllViews();
    }

    @Override
    public void onStop() {
        super.onStop();

        getProgressbar().setIndeterminate(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    private void initOrUpdate() {
        synchronized (authEventsLock) {
            AuthData authData = loginManager.getLastAuthData();
            boolean loggedIn = authData != null && authData.isLoggedIn();
            boolean initialized = loginManager.isInitialized();
            boolean busy = loginManager.isBusy();

            getProgressbar().setIndeterminate(busy);

            if (loggedIn) {
                displayAuthData(authData);
            } else if (!busy && !initialized) {
                loginManager.init();
            } else if (initialized) {
                displayLoginButtons();
            }
        }
    }

    private void displayAuthData(AuthData authData) {
        loginButtons.setVisibility(View.GONE);
        loginInfo.setVisibility(View.VISIBLE);

        nameText.setText(authData.getDisplayName());
    }

    private void displayLoginButtons() {
        loginButtons.setVisibility(View.VISIBLE);
        loginInfo.setVisibility(View.GONE);

        loginButtons.removeAllViews();
        View button;
        for (String serviceId : loginManager.getAuthServiceIds()) {
            button = createButton(serviceId);
            if (button != null) {
                loginButtons.addView(button);
            }
        }
    }

    @OnClick(R.id.logout_button)
    protected void logout() {
        getProgressbar().setIndeterminate(true);
        loginManager.logout();
    }

    @Subscribe
    public void onAuthEvent(AuthEvent authEvent) {
        initOrUpdate();
    }

    @Subscribe
    public void onAuthError(AuthError authError) {
        onAuthEvent(authError);
    }

    @Subscribe
    public void onAuthCancel(AuthCancel authCancel) {
        onAuthEvent(authCancel);
    }

    @Subscribe
    public void onAuthData(AuthData authData) {
        onAuthEvent(authData);
    }

    @Subscribe
    public void onAuthInit(AuthInit authInit) {
        onAuthEvent(authInit);
    }

    private View createButton(String serviceId) {
        View button;
        switch (serviceId) {
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
            button.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            button.setOnClickListener(this);
        }

        return button;
    }

    @Override
    public void onClick(View v) {
        getProgressbar().setIndeterminate(true);
        if (v instanceof SignInButton) {
            loginManager.login(GooglePlus.ID);
        } else if (v instanceof LoginButton) {
            loginManager.login(Facebook.ID);
        }
    }
}
