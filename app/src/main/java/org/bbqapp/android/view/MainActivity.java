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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.bbqapp.android.App;
import org.bbqapp.android.BuildConfig;
import org.bbqapp.android.R;
import org.bbqapp.android.auth.AuthCancel;
import org.bbqapp.android.auth.AuthData;
import org.bbqapp.android.auth.AuthError;
import org.bbqapp.android.view.create.CreateFragment;
import org.bbqapp.android.view.list.ListFragment;
import org.bbqapp.android.view.login.LoginFragment;
import org.bbqapp.android.view.map.MapFragment;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import dagger.ObjectGraph;
import de.halfbit.tinybus.Subscribe;
import de.halfbit.tinybus.TinyBus;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    private DrawerLayout menu;
    private ActionBarDrawerToggle menuToggle;
    private MenuAdapter menuListAdapter;

    private ObjectGraph objectGraph;

    @Bind(R.id.toolbar_progressbar)
    ProgressBar toolbarProgressBar;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Inject
    TinyBus bus;

    @Inject
    LoginManager loginManager;

    private enum FragmentView {
        LOGIN, MAP, LIST, SEARCH, CREATE, SETTINGS, CONTACT, NOTICE, FOOTER
    }

    private Map<MenuAdapter.Entry, FragmentView> menuEntries = new HashMap<>();

    private MenuAdapter.Header loginHeader;

    @Inject
    LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // dagger
        objectGraph = ((App) getApplication()).getObjectGraph();
        objectGraph = objectGraph.plus(new ActivityModule(this));
        objectGraph.inject(this);


        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);


        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        menu = (DrawerLayout) findViewById(R.id.navigation);

        menuToggle = new ActionBarDrawerToggle(this, menu, R.string.open_menu, R.string.close_menu);
        menu.setDrawerListener(menuToggle);
        menuToggle.syncState();

        // list view
        ListView menuList = (ListView) findViewById(R.id.menu_list);
        menuListAdapter = new MenuAdapter(inflater, menuList);
        loginHeader = menuListAdapter.addHeader(getCaption(FragmentView.LOGIN));
        menuEntries.put(loginHeader, FragmentView.LOGIN);
        menuEntries.put(menuListAdapter.add(getCaption(FragmentView.MAP)), FragmentView.MAP);
        menuEntries.put(menuListAdapter.add(getCaption(FragmentView.LIST)), FragmentView.LIST);
        menuEntries.put(menuListAdapter.add(getCaption(FragmentView.SEARCH)), FragmentView.SEARCH);
        menuEntries.put(menuListAdapter.add(getCaption(FragmentView.CREATE)), FragmentView.CREATE);
        menuListAdapter.add();
        menuEntries.put(menuListAdapter.add(getCaption(FragmentView.SETTINGS)), FragmentView.SETTINGS);
        menuEntries.put(menuListAdapter.add(getCaption(FragmentView.CONTACT)), FragmentView.CONTACT);
        menuEntries.put(menuListAdapter.add(getCaption(FragmentView.NOTICE)), FragmentView.NOTICE);
        menuEntries.put(menuListAdapter.addFooter(BuildConfig.VERSION_NAME), FragmentView.FOOTER);
        menuListAdapter.setOnEntryClickListener(new MenuAdapter.OnEntryClickListener() {
            @Override
            public void onEntryClick(ListView listView, MenuAdapter.Entry entry, int position, long id) {
                navigateTo(menuEntries.get(entry));
            }
        });


        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (fragment == null) {
            navigateTo(FragmentView.MAP, false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        bus.register(this);

        onAuthData(loginManager.getLastAuthData());

        try {
            loginManager.login();
        } catch (Exception e) {
            Log.e(TAG, "error occurred during login process", e);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        bus.unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // dagger
        objectGraph = null;

        // butterknife
        ButterKnife.unbind(this);
    }

    @Override
    public void onBackPressed() {
        if (menu.isDrawerOpen(GravityCompat.START)) {
            menu.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public ProgressBar getProgressBar() {
        return toolbarProgressBar;
    }

    protected String getCaption(FragmentView fragmentView) {
        int id = getResources().getIdentifier("menu_" + fragmentView.toString().toLowerCase(), "string", getPackageName());
        return id > 0 ? (String) getResources().getText(id) : fragmentView.name();
    }

    public void navigateTo(FragmentView fragmentView) {
        navigateTo(fragmentView, true);
    }

    public void navigateTo(FragmentView fragmentView, boolean addToBackStack) {
        menu.closeDrawers();

        // inflate new fragmentView
        Fragment fragment = findFragmentByFragmentView(fragmentView);
        if (fragment == null) {
            switch (fragmentView) {
                case LOGIN:
                    fragment = new LoginFragment();
                    break;
                case LIST:
                    fragment = new ListFragment();
                    break;
                case CREATE:
                    fragment = new CreateFragment();
                    break;
                default:
                    fragment = new MapFragment();
            }
        }

        // TODO better handling
        if (getFragmentManager().findFragmentById(R.id.content_frame) != null && getFragmentManager().findFragmentById(R.id.content_frame).getClass().equals(fragment.getClass())) {
            return;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment, fragmentView.name());
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();
    }

    /**
     * Finds a fragment by fragmentView
     *
     * @param fragmentView fragment view to find
     * @return found fragment or {@code null} otherwise
     */
    private Fragment findFragmentByFragmentView(FragmentView fragmentView) {
        return getSupportFragmentManager().findFragmentByTag(fragmentView.name());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return menuToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // propagate results to child fragments
        for (FragmentView fragmentView : FragmentView.values()) {
            Fragment fragment = findFragmentByFragmentView(fragmentView);
            if (fragment != null) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        }

        // propagate results to auth services
        loginManager.onActivityResult(requestCode, resultCode, data);
    }

    public ObjectGraph getObjectGraph() {
        return objectGraph;
    }

    @Subscribe
    public void onAuthData(AuthData authData) {
        if (authData != null && authData.isLoggedIn()) {
            Toast.makeText(this, authData.getDisplayName() + " is logged in.", Toast.LENGTH_LONG).show();

            loginHeader.setString(authData.getDisplayName());
        } else if (authData != null) {
            Toast.makeText(this, "Logged out.", Toast.LENGTH_LONG).show();

            loginHeader.setString(getCaption(FragmentView.LOGIN));
        }
    }

    @Subscribe
    public void onAuthError(AuthError authError) {
        Toast.makeText(this, "Error occurred during authorization.", Toast.LENGTH_LONG).show();
    }

    @Subscribe
    public void onAuthCancel(AuthCancel authCancel) {
        Toast.makeText(this, "Login canceled.", Toast.LENGTH_LONG).show();
    }
}
