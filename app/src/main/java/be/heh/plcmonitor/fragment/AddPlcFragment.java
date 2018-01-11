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

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import be.heh.plcmonitor.ApplicationComponent;
import be.heh.plcmonitor.DaggerApplicationComponent;
import be.heh.plcmonitor.R;
import be.heh.plcmonitor.dao.PlcDaoImpl;
import be.heh.plcmonitor.dao.PlcUserDaoImpl;
import be.heh.plcmonitor.dao.UserDaoImpl;
import be.heh.plcmonitor.database.DatabaseModule;
import be.heh.plcmonitor.model.Plc;
import be.heh.plcmonitor.model.PlcUser;
import be.heh.plcmonitor.model.User;
import be.heh.plcmonitor.util.Validator;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;
import static org.apache.commons.lang3.StringUtils.isNumeric;

/**
 * Represents the screen for adding a PLC.
 *
 * @author Terencio Agozzino
 */
public class AddPlcFragment extends Fragment {

    /**
     * Useful for debug to identify which class has logged.
     */
    private static final String TAG = AddPlcFragment.class.getSimpleName();

    /**
     * Injections.
     */
    @Inject
    PlcDaoImpl plcDaoImpl;
    @Inject
    PlcUserDaoImpl plcUserDaoImpl;
    @Inject
    UserDaoImpl userDaoImpl;

    /**
     * UI references.
     */
    private TextView mPlcNameView;
    private TextView mPlcIpView;
    private TextView mPlcRackView;
    private TextView mPlcSlotView;

    /**
     * Keeps the current user in memory to easily add the PLC to his
     * PLC list.
     */
    private User mUser;

    /**
     * Keeps the PLC created in memory to easily update the PlcAdapter.
     */
    private Plc mPlc;

    /**
     * Called when the connection fragment is instantiate.
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
        View view = inflater.inflate(R.layout.fragment_add_plc, container,
                false);

        ApplicationComponent applicationComponent =
                DaggerApplicationComponent.builder()
                        .databaseModule(new DatabaseModule(
                                getActivity().getApplication()))
                        .build();
        applicationComponent.inject(this);

        // Retrieves the logged-in user.
        SharedPreferences sp = getActivity().getSharedPreferences("login",
                MODE_PRIVATE);
        mUser = userDaoImpl.get(sp.getInt("id", 0));

        mPlcNameView = view.findViewById(R.id.txtPlcName);
        mPlcNameView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptAddPlc();
                return true;
            }
            return false;
        });

        mPlcIpView = view.findViewById(R.id.txtPlcIp);
        mPlcIpView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptAddPlc();
                return true;
            }
            return false;
        });

        mPlcRackView = view.findViewById(R.id.txtPlcRack);
        mPlcRackView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptAddPlc();
                return true;
            }
            return false;
        });

        mPlcSlotView = view.findViewById(R.id.txtPlcSlot);
        mPlcSlotView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptAddPlc();
                return true;
            }
            return false;
        });

        Button mAddButton = view.findViewById(R.id.btn_add);
        mAddButton.setOnClickListener(v -> attemptAddPlc());

        return view;
    }

    /**
     * Attempts to add a PLC for the account specified by the add form. If there
     * are form errors (invalid name, missing fields, etc.), the errors are
     * presented and no actual add attempt is made.
     */
    private void attemptAddPlc() {
        // Reset errors.
        mPlcNameView.setError(null);
        mPlcIpView.setError(null);
        mPlcRackView.setError(null);
        mPlcSlotView.setError(null);

        // Store values at the time of the adding PLC attempt.
        String plcName = mPlcNameView.getText().toString();
        String plcIp = mPlcIpView.getText().toString();
        String plcRack = mPlcRackView.getText().toString();
        String plcSlot = mPlcSlotView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid PLC name.
        if (TextUtils.isEmpty(plcName)) {
            mPlcNameView.setError(getString(R.string.error_field_required));
            focusView = mPlcNameView;
            cancel = true;
        } else if (!Validator.isValidName(plcName)) {
            mPlcNameView.setError(getString(R.string.error_invalid_first_name));
            focusView = mPlcNameView;
            cancel = true;
        }

        // Check for a valid IP.
        if (TextUtils.isEmpty(plcIp)) {
            mPlcIpView.setError(getString(R.string.error_field_required));
            focusView = mPlcIpView;
            cancel = true;
        } else if (!Validator.isValidIp(plcIp)) {
            mPlcIpView.setError(getString(R.string.error_invalid_ip));
            focusView = mPlcIpView;
            cancel = true;
        }

        // Check for a valid rack.
        if (TextUtils.isEmpty(plcRack)) {
            mPlcRackView.setError(getString(R.string.error_field_required));
            focusView = mPlcRackView;
            cancel = true;
        } else if (!isNumeric(plcRack)) {
            mPlcRackView.setError(getString(R.string.error_invalid_rack));
            focusView = mPlcRackView;
            cancel = true;
        }

        // Check for a valid slot.
        if (TextUtils.isEmpty(plcSlot)) {
            mPlcSlotView.setError(getString(R.string.error_field_required));
            focusView = mPlcSlotView;
            cancel = true;
        } else if (!isNumeric(plcSlot)) {
            mPlcSlotView.setError(getString(R.string.error_invalid_slot));
            focusView = mPlcSlotView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt register and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            createPlc(plcName, plcIp, Integer.parseInt(plcRack), Integer.parseInt(plcSlot))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableCompletableObserver() {

                        /**
                         * Called once the PLC registration completes normally.
                         */
                        @Override
                        public void onComplete() {
                            getTargetFragment().onActivityResult(
                                    getTargetRequestCode(),
                                    Activity.RESULT_OK,
                                    new Intent()
                                            .putExtra("plc", mPlc));
                            getFragmentManager().popBackStack();
                        }

                        /**
                         * Called once if PLC registration 'throws' an exception.
                         *
                         * @param e the exception, not null
                         */
                        @Override
                        public void onError(@NonNull Throwable e) {
                            Log.e(TAG, "Unable to register the PLC", e);
                        }
                    });
        }
    }

    /**
     * Creates a PLC in the database.
     *
     * @param name the name of the PLC
     * @param ip the IP address of the PLC
     * @param rack the rack of the PLC
     * @param slot the slot of the PLC
     * @return the new Completable instance
     */
    public Completable createPlc(String name, String ip, int rack, int slot) {
        return Completable.create(emitter -> {
            mPlc = new Plc(name, ip, rack, slot, null);
            plcDaoImpl.create(mPlc);
            plcUserDaoImpl.create(new PlcUser(mPlc, mUser));

            emitter.onComplete();
        });
    }
}