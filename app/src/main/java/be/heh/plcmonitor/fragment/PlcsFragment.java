/*
 * Copyright 2017 Terencio Agozzino
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package be.heh.plcmonitor.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import be.heh.plcmonitor.ApplicationComponent;
import be.heh.plcmonitor.DaggerApplicationComponent;
import be.heh.plcmonitor.R;
import be.heh.plcmonitor.adapter.PlcsAdapter;
import be.heh.plcmonitor.dao.PlcDaoImpl;
import be.heh.plcmonitor.dao.PlcUserDaoImpl;
import be.heh.plcmonitor.dao.UserDaoImpl;
import be.heh.plcmonitor.database.DatabaseModule;
import be.heh.plcmonitor.helper.Message;
import be.heh.plcmonitor.model.Plc;
import be.heh.plcmonitor.model.User;

import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static android.support.design.widget.Snackbar.LENGTH_LONG;

/**
 * Represents the overview screen to all saved PLC of a user.
 *
 * @author Terencio Agozzino
 */
public class PlcsFragment extends Fragment {

    /**
     * Useful for debug to identify which class has logged.
     */
    private static final String TAG = PlcsFragment.class.getSimpleName();

    /**
     * Request codes.
     */
    public static final int PLC_CREATED = 1;
    public static final int PLC_EDITED = 2;

    /**
     * Injections.
     */
    @Inject
    UserDaoImpl userDaoImpl;
    @Inject
    PlcDaoImpl plcDaoImpl;
    @Inject
    PlcUserDaoImpl plcUserDaoImpl;

    /**
     * PLCs Adapter.
     */
    private PlcsAdapter mPlcsAdapter;

    private ArrayList mPlcs;

    /**
     * UI references.
     */
    private RecyclerView mPlcsView;
    private View mCoordinatorLayoutView;

    /**
     * Called when the overview fragment is instantiate.
     *
     * @param inflater the LayoutInflater object that can be used to inflate
     *                 any views in the fragment
     * @param container the parent view that the fragment's UI should be attached to
     * @param savedInstanceState saved state given so that the fragment can be
     *                           re-constructed
     * @return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overview,
                container, false);

        ApplicationComponent applicationComponent =
                DaggerApplicationComponent.builder()
                        .databaseModule(new DatabaseModule(getActivity()
                                .getApplication()))
                        .build();
        applicationComponent.inject(this);

        FloatingActionButton mBtnAdd = view.findViewById(R.id.fab);
        mBtnAdd.setOnClickListener(v -> {
            AddPlcFragment addPlcFragment = new AddPlcFragment();
            addPlcFragment.setTargetFragment(PlcsFragment.this, PLC_CREATED);

            // In order to avoid making a new expensive query to the database,
            // when adding the PLC, this fragment is just hidden during this
            // time.
            getFragmentManager().beginTransaction()
                    .hide(this)
                    .add(R.id.snackbarPosition, addPlcFragment, "AddPlcFragment")
                    .addToBackStack(null)
                    .commit();
        });

        mPlcsView = view.findViewById(R.id.rv_plcs);
        mPlcsView.addOnScrollListener(new RecyclerView.OnScrollListener()  {

            /**
             * Callback method to be invoked when the RecyclerView has been
             * scrolled.
             *
             * @param recyclerView the RecycleView
             * @param dx delta variation on the x-axis
             * @param dy delta variation on the y-axis
             */
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) {
                    // Scroll Down
                    if (mBtnAdd.isShown()) {
                        mBtnAdd.hide();
                    }
                } else if (dy < 0) {
                    // Scroll Up
                    if (!mBtnAdd.isShown()) {
                        mBtnAdd.show();
                    }
                }
            }
        });

        mCoordinatorLayoutView = view.findViewById(R.id.cl_overview);

        SharedPreferences sp = getActivity().getSharedPreferences("login", MODE_PRIVATE);

        ProgressDialog progressBar = new ProgressDialog(getActivity());
        progressBar.setMessage("Retrieves list of users...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressBar.setProgress(0);

        // Probably not the best way...
        int total = plcDaoImpl.getAll().size();
        progressBar.setMax(total);

        mPlcs = new ArrayList();

        mPlcsView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mPlcsAdapter = new PlcsAdapter(getActivity(), mPlcs, mCoordinatorLayoutView);
        mPlcsView.setAdapter(mPlcsAdapter);

       getUser(sp.getInt("id", 0))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<User>() {

                    /**
                     * Notifies the SingleObserver with a single user
                     * value and that the Single has finished sending
                     * push-based notifications.
                     */
                    @Override
                    public void onSuccess(User user) {

                        Observable.fromIterable(plcDaoImpl.getPlcsByUser(user))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Observer<Plc>() {

                                    /**
                                     * Provides the PlcsFragment with the means of
                                     * cancelling (disposing) the connection (channel)
                                     * with the Observable in both synchronous (from within
                                     * Observer.onNext(Object)) and asynchronous manner.
                                     *
                                     * @param disposable the disposable resource.
                                     */
                                    @Override
                                    public void onSubscribe(Disposable disposable) {
                                        progressBar.show();
                                    }

                                    /**
                                     * Provides PlcsFragment with a new PLC to observe.
                                     *
                                     * @param plc the retrieved PLC
                                     */
                                    @Override
                                    public void onNext(Plc plc) {
                                        mPlcsAdapter.addPlc(plc);
                                        progressBar.setProgress(mPlcsAdapter.getPlcs().size() / total);
                                    }

                                    /**
                                     * Notifies PlcsFragment that the Observable has
                                     * experienced an error condition when retrieving the list
                                     * of users.
                                     *
                                     * @param e the exception, not null
                                     */
                                    @Override
                                    public void onError(Throwable e) {
                                        Log.e(TAG, "Unable to retrieves PLCs", e);
                                        progressBar.hide();
                                    }

                                    /**
                                     * Notifies PlcsFragment that the Observable has finished
                                     * retrieving the list of plcs.
                                     */
                                    @Override
                                    public void onComplete() {
                                        progressBar.hide();
                                    }
                                });

                    }

                    /**
                     * Called once if login registration 'throws' an exception.
                     *
                     * @param e the exception, not null
                     */
                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e(TAG, "Unable to get the user", e);
                    }
                });

        if (getArguments() != null) {
            Message.display(mCoordinatorLayoutView,
                    Html.fromHtml(getArguments().getString("message")),
                    LENGTH_LONG);
        }

        return view;
    }


    /**
     * Emits either a single user value for the for user editing, or an error.
     *
     * @return the new Single instance
     */
    public Single<User> getUser(int id) {
        return Single.create(emitter -> emitter.onSuccess(userDaoImpl.get(id)));
    }

    /**
     * Specifies if a PLC has been created.
     *
     * @param requestCode the integer request code originally supplied to
     *                    startActivityForResult(), allowing to identify who
     *                    this result came from
     * @param resultCode the integer result code returned by AddPlcFragment
     * @param data an Intent, which can return PLC data to the PlcsFragment.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLC_CREATED && resultCode == RESULT_OK) {
            Plc plc = data.getParcelableExtra("plc");
            mPlcsAdapter.addPlc(plc);

            // After adding the PLC, make visible the fragment and
            // display a message.
            getFragmentManager().beginTransaction().show(this).commit();
            Message.display(mCoordinatorLayoutView,
                    Html.fromHtml(getString(R.string.message_successful_plc_creation)),
                    LENGTH_LONG);
        } else if (requestCode == PLC_EDITED && resultCode == RESULT_OK) {
            int position = data.getIntExtra("plc_position", 0);

            Plc updatedPlc = data.getParcelableExtra("plc");
            mPlcsAdapter.getPlcs().set(position, updatedPlc);
            mPlcsAdapter.notifyItemChanged(position);

            Message.display(mCoordinatorLayoutView,
                    Html.fromHtml(getString(R.string.message_successful_plc_edited)),
                    LENGTH_LONG);
        }
    }
}