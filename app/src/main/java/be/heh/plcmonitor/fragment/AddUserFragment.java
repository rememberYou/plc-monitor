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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import be.heh.plcmonitor.ApplicationComponent;
import be.heh.plcmonitor.DaggerApplicationComponent;
import be.heh.plcmonitor.R;
import be.heh.plcmonitor.dao.DataBlockDaoImpl;
import be.heh.plcmonitor.dao.PlcDaoImpl;
import be.heh.plcmonitor.dao.PlcUserDaoImpl;
import be.heh.plcmonitor.dao.UserDaoImpl;
import be.heh.plcmonitor.database.DatabaseModule;
import be.heh.plcmonitor.model.DataBlock;
import be.heh.plcmonitor.model.Plc;
import be.heh.plcmonitor.model.PlcUser;
import be.heh.plcmonitor.model.User;
import be.heh.plcmonitor.util.Validator;

import com.afollestad.materialdialogs.MaterialDialog;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Represents the screen for adding a user.
 *
 * @author Terencio Agozzino
 */
public class AddUserFragment extends Fragment {

    /**
     * Useful for debug to identify which class has logged.
     */
    private static final String TAG = AddUserFragment.class.getSimpleName();

    /**
     * Injections.
     */
    @Inject
    UserDaoImpl userDaoImpl;
    @Inject
    PlcDaoImpl plcDaoImpl;
    @Inject
    DataBlockDaoImpl dataBlockDaoImpl;
    @Inject
    PlcUserDaoImpl plcUserDaoImpl;

    /**
     * UI references.
     */
    private AutoCompleteTextView mEmailView;
    private CheckBox mAdminView;
    private EditText mFirstNameView;
    private EditText mLastNameView;
    private EditText mPasswordView;
    private EditText mPasswordConfirmView;

    /**
     * Keeps the user created in memory to easily update the UsersAdapter.
     */
    private User mUser;

    /**
     * Preventive message for the privilege checkbox.
     */
    private boolean msgPreventive;

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
        View view = inflater.inflate(R.layout.fragment_add_user,
                container, false);

        ApplicationComponent applicationComponent =
                DaggerApplicationComponent.builder()
                        .databaseModule(new DatabaseModule(
                                getActivity().getApplication()))
                .build();
        applicationComponent.inject(this);

        msgPreventive = true;

        mEmailView = view.findViewById(R.id.actv_add_user_email);

        mFirstNameView = view.findViewById(R.id.et_add_user_firstname);
        mFirstNameView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptRegister();
                return true;
            }
            return false;
        });

        mLastNameView = view.findViewById(R.id.et_add_user_lastname);
        mLastNameView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptRegister();
                return true;
            }
            return false;
        });

        mPasswordView = view.findViewById(R.id.et_add_user_password);
        mPasswordView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptRegister();
                return true;
            }
            return false;
        });

        mPasswordConfirmView = view.findViewById(R.id.et_add_user_password_confirm);
        mPasswordConfirmView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptRegister();
                return true;
            }
            return false;
        });

        mAdminView = view.findViewById(R.id.chk_admin);
        mAdminView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && msgPreventive) {
                adminPreventionDialog().show();

                msgPreventive = false;
            }
        });

        TextView mAdminTextView = view.findViewById(R.id.tv_admin);
        mAdminTextView.setOnClickListener(new View.OnClickListener() {

            /**
             * Called when a view has been clicked.
             *
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                if (mAdminView.isChecked()) {
                    mAdminView.setChecked(false);
                } else {
                    mAdminView.setChecked(true);
                }
            }
        });

        Button mRegisterButton = view.findViewById(R.id.btn_add);
        mRegisterButton.setOnClickListener(v -> attemptRegister());

        return view;
    }

    /**
     * Constructs a prevention dialog box for the privilege checkbox.
     *
     * @return a new prevention dialog box
     */
    private MaterialDialog.Builder adminPreventionDialog() {
        return new MaterialDialog.Builder(getActivity())
                .title("Prevention Message")
                .content(R.string.dialog_user_admin)
                .negativeText("Cancel")
                .positiveText("Agree")
                .onNegative((dialog, which) -> mAdminView.setChecked(false));
    }

    /**
     * Attempts to register the account specified by the register form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual register attempt is made.
     */
    private void attemptRegister() {
        // Reset errors.
        mFirstNameView.setError(null);
        mLastNameView.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mPasswordConfirmView.setError(null);

        // Store values at the time of the register attempt.
        String firstName = mFirstNameView.getText().toString();
        String lastName = mLastNameView.getText().toString();
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String passwordConfirm = mPasswordConfirmView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid first name.
        if (TextUtils.isEmpty(firstName)) {
            mFirstNameView.setError(getString(R.string.error_field_required));
            focusView = mFirstNameView;
            cancel = true;
        } else if (!Validator.isValidName(firstName)) {
            mFirstNameView.setError(getString(R.string.error_invalid_first_name));
            focusView = mFirstNameView;
            cancel = true;
        }

        // Check for a valid last name.
        if (TextUtils.isEmpty(lastName)) {
            mLastNameView.setError(getString(R.string.error_field_required));
            focusView = mLastNameView;
            cancel = true;
        } else if (!Validator.isValidName(lastName)) {
            mLastNameView.setError(getString(R.string.error_invalid_last_name));
            focusView = mLastNameView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!Validator.isValidEmail(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        } else if (isRegisteredEmail(email)) {
            mEmailView.setError(getString(R.string.error_unique_email));
            focusView = mEmailView;
            cancel = true;
        }

        // Check for a valid password.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!Validator.isValidPassword(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid password.
        if (TextUtils.isEmpty(passwordConfirm)) {
            mPasswordConfirmView.setError(getString(R.string.error_field_required));
            focusView = mPasswordConfirmView;
            cancel = true;
        } else if (!passwordConfirm.equals(password)) {
            mPasswordConfirmView.setError(getString(R.string.error_incorrect_password_confirm));
            focusView = mPasswordConfirmView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt register and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            createUser(firstName, lastName, email, password)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableCompletableObserver() {

                        /**
                         * Called once the user registration completes normally.
                         */
                        @Override
                        public void onComplete() {
                            getTargetFragment().onActivityResult(
                                    getTargetRequestCode(),
                                    Activity.RESULT_OK,
                                    new Intent()
                                            .putExtra("user", mUser));
                            getFragmentManager().popBackStack();
                        }

                        /**
                         * Called once if user registration 'throws' an exception.
                         *
                         * @param e the exception, not null
                         */
                        @Override
                        public void onError(@NonNull Throwable e) {
                            Log.e(TAG, "Unable to register the user", e);
                        }
                    });
        }
    }

    /**
     * Creates a user in the database by giving him/her administrator rights if
     * the user is the first one registered in the database.
     *
     * @param firstName the first name of the user
     * @param lastName the last name of the user
     * @param email the email address of the user
     * @param password password the password of the user
     * @return the new Completable instance
     */
    public Completable createUser(String firstName, String lastName,
                                  String email, String password) {
        return Completable.create(emitter -> {
            DataBlock db = new DataBlock(5, 0, 20, new byte[512]);
            Single.just(dataBlockDaoImpl.create(db));

            Plc conditioningPills = new Plc("Conditioning Pills",
                    "192.168.1.127", 0, 2, db);
            Single.just(plcDaoImpl.create(conditioningPills));

            Plc controlLevel = new Plc("Control Level",
                    "192.168.1.127", 0, 2, db);
            Single.just(plcDaoImpl.create(controlLevel));

            if (mAdminView.isChecked()) {
                mUser = new User(firstName, lastName, email, password, 1);
            } else {
                mUser = new User(firstName, lastName, email, password, 1);
            }
            userDaoImpl.create(mUser);


            Single.just(plcUserDaoImpl.create(new PlcUser(conditioningPills, mUser)));
            Single.just(plcUserDaoImpl.create(new PlcUser(controlLevel, mUser)));

            emitter.onComplete();
        });
    }

    /**
     * Specifies if the email is registered in the database.
     *
     * @param email the email address of the user
     * @return true if the email is registered; false otherwise
     */
    private boolean isRegisteredEmail(String email) {
        return userDaoImpl.getUserByEmail(email) != null;
    }
}