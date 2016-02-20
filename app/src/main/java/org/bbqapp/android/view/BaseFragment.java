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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ProgressBar;

import com.squareup.leakcanary.RefWatcher;

import org.bbqapp.android.Injector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import dagger.ObjectGraph;

/**
 * Base fragment for all fragments in application
 */
public abstract class BaseFragment extends Fragment implements Injector {

    private boolean active = false;

    private ObjectGraph objectGraph;

    @Inject
    RefWatcher refWatcher;

    @Override
    public void inject(Object o) {
        objectGraph.inject(o);
    }

    @Override
    public ObjectGraph getObjectGraph() {
        return objectGraph;
    }

    private void prepareAndInject(Context context) {
        Injector injector = (Injector) context;
        if (objectGraph == null && injector != null && injector.getObjectGraph() != null) {
            objectGraph = injector.getObjectGraph().plus(getModules().toArray());
            inject(this);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        prepareAndInject(getActivity());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        active = true;
    }

    @Override
    public void onPause() {
        super.onPause();

        active = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // dagger
        objectGraph = null;

        refWatcher.watch(this);
    }

    public ProgressBar getProgressbar() {
        return ((MainActivity) getActivity()).getProgressBar();
    }

    /**
     * Returns extensible list of modules of this fragment
     *
     * @return list of modules to add
     */
    protected List<Object> getModules() {

        return new ArrayList<>(Arrays.<Object>asList(new FragmentModule(this)));
    }

    public boolean isActive() {
        return active;
    }
}
