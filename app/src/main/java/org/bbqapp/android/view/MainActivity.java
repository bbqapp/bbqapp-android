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

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.widget.ListView;
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

import dagger.ObjectGraph;
import de.halfbit.tinybus.Subscribe;
import de.halfbit.tinybus.TinyBus;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    private DrawerLayout menu;
    private Toolbar toolbar;
    private ActionBarDrawerToggle menuToggle;
    private MenuAdapter menuListAdapter;

    private ObjectGraph objectGraph;

    @Inject
    TinyBus bus;

    @Inject
    LoginManager loginManager;

    private enum View {
        LOGIN, MAP, LIST, SEARCH, CREATE, SETTINGS, CONTACT, NOTICE, FOOTER;
    }

    private Map<MenuAdapter.Entry, View> menuEntries = new HashMap<>();

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

        toolbar = (Toolbar) findViewById(R.id.toolbar);
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
        loginHeader = menuListAdapter.addHeader(getCaption(View.LOGIN));
        menuEntries.put(loginHeader, View.LOGIN);
        menuEntries.put(menuListAdapter.add(getCaption(View.MAP)), View.MAP);
        menuEntries.put(menuListAdapter.add(getCaption(View.LIST)), View.LIST);
        menuEntries.put(menuListAdapter.add(getCaption(View.SEARCH)), View.SEARCH);
        menuEntries.put(menuListAdapter.add(getCaption(View.CREATE)), View.CREATE);
        menuListAdapter.add();
        menuEntries.put(menuListAdapter.add(getCaption(View.SETTINGS)), View.SETTINGS);
        menuEntries.put(menuListAdapter.add(getCaption(View.CONTACT)), View.CONTACT);
        menuEntries.put(menuListAdapter.add(getCaption(View.NOTICE)), View.NOTICE);
        menuEntries.put(menuListAdapter.addFooter(BuildConfig.VERSION_NAME), View.FOOTER);
        menuListAdapter.setOnEntryClickListener(new MenuAdapter.OnEntryClickListener() {
            @Override
            public void onEntryClick(ListView listView, MenuAdapter.Entry entry, int position, long id) {
                navigateTo(menuEntries.get(entry));
            }
        });


        Fragment fragment = getFragmentManager().findFragmentById(R.id.content_frame);
        if (fragment == null) {
            navigateTo(View.MAP, false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        bus.register(this);

        loginManager.login();
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
    }

    protected String getCaption(View view) {
        int id = getResources().getIdentifier("menu_" + view.toString().toLowerCase(), "string", getPackageName());
        return id > 0 ? (String) getResources().getText(id) : view.name();
    }

    public void navigateTo(View view) {
        navigateTo(view, true);
    }

    public void navigateTo(View view, boolean addToBackStack) {
        menu.closeDrawers();

        // inflate new view
        Fragment fragment = findFragmentByView(view);
        if (fragment == null) {
            switch (view) {
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

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment, view.name());
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();
    }

    /**
     * Finds a fragment by view
     *
     * @param view fragment of view to find
     * @return found fragment or {@code null} otherwise
     */
    private Fragment findFragmentByView(View view) {
        return getFragmentManager().findFragmentByTag(view.name());
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (menuToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // propagate results to child fragments
        for (View view : View.values()) {
            Fragment fragment = findFragmentByView(view);
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
        } else {
            Toast.makeText(this, "Logged out.", Toast.LENGTH_LONG).show();

            loginHeader.setString(getCaption(View.LOGIN));
        }
    }

    @Subscribe
    public void onAuthError(AuthError authError) {
        Toast.makeText(this, "Error occurred during authorization.", Toast.LENGTH_LONG).show();
    }

    @Subscribe
    public void onAuthCancel(AuthCancel authCancel) {
        Toast.makeText(this, "login canceled.", Toast.LENGTH_LONG).show();
    }
}
