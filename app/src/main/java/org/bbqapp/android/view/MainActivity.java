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

import com.facebook.FacebookSdk;

import org.bbqapp.android.App;
import org.bbqapp.android.BuildConfig;
import org.bbqapp.android.R;
import org.bbqapp.android.view.create.CreateFragment;
import org.bbqapp.android.view.list.ListFragment;
import org.bbqapp.android.view.login.LoginFragment;
import org.bbqapp.android.view.map.MapFragment;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import dagger.ObjectGraph;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    private DrawerLayout menu;
    private Toolbar toolbar;
    private ActionBarDrawerToggle menuToggle;
    private MenuAdapter menuListAdapter;

    // last fragment
    private Fragment fragment;

    private ObjectGraph objectGraph;

    private enum Views {
        LOGIN, MAP, LIST, SEARCH, CREATE, SETTINGS, CONTACT, NOTICE, FOOTER;
    }

    private Map<MenuAdapter.Entry, Views> menuEntries = new HashMap<>();

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

        initSdks();

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
        menuEntries.put(menuListAdapter.addHeader(getCaption(Views.LOGIN)), Views.LOGIN);
        menuEntries.put(menuListAdapter.add(getCaption(Views.MAP)), Views.MAP);
        menuEntries.put(menuListAdapter.add(getCaption(Views.LIST)), Views.LIST);
        menuEntries.put(menuListAdapter.add(getCaption(Views.SEARCH)), Views.SEARCH);
        menuEntries.put(menuListAdapter.add(getCaption(Views.CREATE)), Views.CREATE);
        menuListAdapter.add();
        menuEntries.put(menuListAdapter.add(getCaption(Views.SETTINGS)), Views.SETTINGS);
        menuEntries.put(menuListAdapter.add(getCaption(Views.CONTACT)), Views.CONTACT);
        menuEntries.put(menuListAdapter.add(getCaption(Views.NOTICE)), Views.NOTICE);
        menuEntries.put(menuListAdapter.addFooter(BuildConfig.VERSION_NAME), Views.FOOTER);
        menuListAdapter.setOnEntryClickListener(new MenuAdapter.OnEntryClickListener() {
            @Override
            public void onEntryClick(ListView listView, MenuAdapter.Entry entry, int position, long id) {
                navigateTo(menuEntries.get(entry));
            }
        });


        Fragment fragment = getFragmentManager().findFragmentById(R.id.content_frame);
        if (fragment == null) {
            navigateTo(Views.MAP, false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // dagger
        objectGraph = null;
    }

    private void initSdks() {
        // init SDKs
        FacebookSdk.sdkInitialize(this);
    }

    protected String getCaption(Views view) {
        int id = getResources().getIdentifier("menu_" + view.toString().toLowerCase(), "string", getPackageName());
        return id > 0 ? (String) getResources().getText(id) : view.name();
    }

    public void navigateTo(Views view) {
        navigateTo(view, true);
    }

    public void navigateTo(Views view, boolean addToBackStack) {
        menu.closeDrawers();

        // inflate new view
        fragment = getFragmentManager().findFragmentByTag(view.name());
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
        // TODO temp G+ login fix
        super.onActivityResult(requestCode, resultCode, data);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    public ObjectGraph getObjectGraph() {
        return objectGraph;
    }
}
