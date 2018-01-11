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

package be.heh.plcmonitor.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
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
import be.heh.plcmonitor.database.DatabaseHelper;
import be.heh.plcmonitor.database.DatabaseModule;
import be.heh.plcmonitor.helper.Progress;
import be.heh.plcmonitor.model.DataBlock;
import be.heh.plcmonitor.model.Plc;
import be.heh.plcmonitor.model.PlcUser;
import be.heh.plcmonitor.model.User;
import be.heh.plcmonitor.util.Validator;

import com.afollestad.materialdialogs.MaterialDialog;
import com.j256.ormlite.cipher.android.apptools.OrmLiteBaseActivity;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * A register screen that offers sign up via first name, last name,
 * email address, and password.
 *
 * @author Terencio Agozzino
 */
public class RegisterActivity extends OrmLiteBaseActivity<DatabaseHelper> {

    /**
     * Useful for debug to identify which class has logged.
     */
    private static final String TAG = RegisterActivity.class.getSimpleName();

    /**
     * Injections.
     */
    @Inject
    DataBlockDaoImpl dataBlockDaoImpl;
    @Inject
    UserDaoImpl userDaoImpl;
    @Inject
    PlcDaoImpl plcDaoImpl;
    @Inject
    PlcUserDaoImpl plcUserDaoImpl;

    /**
     * UI references.
     */
    private AutoCompleteTextView mEmailView;
    private CheckBox mTermsPrivacyView;
    private EditText mFirstNameView;
    private EditText mLastNameView;
    private EditText mPasswordView;
    private EditText mPasswordConfirmView;
    private MaterialDialog mPrivacyPolicyDialog;
    private MaterialDialog mTermsUseDialog;
    private View mProgressView;
    private View mRegisterFormView;

    /**
     * Called when the activity is starting.
     *
     * @param savedInstanceState if the activity is being re-initialized after
     *                           previously being shut down then this Bundle
     *                           contains the data it most recently supplied in
     *                           onSaveInstanceState(Bundle).
     *                           Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ApplicationComponent applicationComponent =
                DaggerApplicationComponent.builder()
                        .databaseModule(new DatabaseModule(this))
                        .build();
        applicationComponent.inject(this);

        mEmailView = findViewById(R.id.actv_register_email);

        mFirstNameView = findViewById(R.id.et_register_firstname);
        mFirstNameView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptRegister();
                return true;
            }
            return false;
        });

        mLastNameView = findViewById(R.id.et_register_lastname);
        mLastNameView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptRegister();
                return true;
            }
            return false;
        });

        mPasswordView = findViewById(R.id.et_register_password);
        mPasswordView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptRegister();
                return true;
            }
            return false;
        });

        mPasswordConfirmView = findViewById(R.id.et_register_password_confirm);
        mPasswordConfirmView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptRegister();
                return true;
            }
            return false;
        });

        mTermsPrivacyView = findViewById(R.id.chk_terms_privacy);

        Button mRegisterButton = findViewById(R.id.btn_register);

        mRegisterButton.setOnClickListener(view -> attemptRegister());

        mRegisterFormView = findViewById(R.id.scroll_register);

        mProgressView = findViewById(R.id.pg_register);

        TextView mLoginView = findViewById(R.id.tv_login);
        mLoginView.setOnClickListener(view -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });

        MaterialDialog.Builder mPrivacyPolicyBuilder = new MaterialDialog.Builder(this)
                .title(R.string.prompt_privacy_policy)
                .content(Html.fromHtml(getString(R.string.dialog_privacy_policy)))
                .positiveText(R.string.action_back);

        mPrivacyPolicyDialog = mPrivacyPolicyBuilder.build();

        TextView mPrivacyPolicyView = findViewById(R.id.tv_privacy_policy);
        mPrivacyPolicyView.setOnClickListener(view -> mPrivacyPolicyDialog.show());

        MaterialDialog.Builder mTermsUseBuilder = new MaterialDialog.Builder(this)
                .title(R.string.prompt_terms_use)
                .content(Html.fromHtml(getString(R.string.dialog_terms_use)))
                .positiveText(R.string.action_back);

        mTermsUseDialog = mTermsUseBuilder.build();

        TextView mTermsUseView = findViewById(R.id.tv_terms_use);
        mTermsUseView.setOnClickListener(view -> mTermsUseDialog.show());
    }

    /**
     * If not override, it will starts a LoginActivity when the user pressed
     * the back button.
     */
    @Override
    public void onBackPressed() { finish(); }

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
        String firstName = mFirstNameView.getText().toString().trim();
        String lastName = mLastNameView.getText().toString().trim();
        String email = mEmailView.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();
        String passwordConfirm = mPasswordConfirmView.getText().toString().trim();

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

        // Check for Terms of Use and Privacy Policy.
        if (!mTermsPrivacyView.isChecked()) {
            mTermsPrivacyView.setButtonTintList(getResources()
                    .getColorStateList(R.color.accent));
            focusView = mTermsPrivacyView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt register and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user register attempt.
            showProgress(true);

            createUser(firstName, lastName, email, password)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableCompletableObserver() {

                        /**
                         * Called once the user registration completes normally.
                         */
                        @Override
                        public void onComplete() {
                            showProgress(false);
                            setResult(Activity.RESULT_OK);
                            finish();
                            dispose();
                        }

                        /**
                         * Called once if user registration 'throws' an exception.
                         *
                         * @param e the exception, not null
                         */
                        @Override
                        public void onError(@NonNull Throwable e) {
                            Log.e(TAG, "Unable to register the user", e);
                            showProgress(false);
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
            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // If it's the first user registered, automatically give him
            // administrator rights.
            if (isEmptyDatabase()) {
                DataBlock db = new DataBlock(5, 0, 20, new byte[512]);
                Single.just(dataBlockDaoImpl.create(db));

                Plc conditioningPills = new Plc("Conditioning Pills",
                        "192.168.1.127", 0, 2, db);
                Single.just(plcDaoImpl.create(conditioningPills));

                Plc controlLevel = new Plc("Control Level",
                        "192.168.1.127", 0, 2, db);
                Single.just(plcDaoImpl.create(controlLevel));

                User user = new User(firstName, lastName, email, password, 1);
                Single.just(userDaoImpl.create(user));

                Single.just(plcUserDaoImpl.create(new PlcUser(conditioningPills, user)));
                Single.just(plcUserDaoImpl.create(new PlcUser(controlLevel, user)));
            } else {
                DataBlock db = new DataBlock(5, 0, 20, new byte[512]);
                Single.just(dataBlockDaoImpl.create(db));

                Plc conditioningPills = new Plc("Conditioning Pills",
                        "192.168.1.127", 0, 2, db);
                Single.just(plcDaoImpl.create(conditioningPills));

                Plc controlLevel = new Plc("Control Level",
                        "192.168.1.127", 0, 2, db);
                Single.just(plcDaoImpl.create(controlLevel));

                User user = new User(firstName, lastName, email, password);
                Single.just(userDaoImpl.create(user));

                Single.just(plcUserDaoImpl.create(new PlcUser(conditioningPills, user)));
                Single.just(plcUserDaoImpl.create(new PlcUser(controlLevel, user)));
            }

            emitter.onComplete();
        });
    }

    /**
     * Emits either a single user value according to his email address.
     *
     * @return the new Single instance
     */
    public Single<Plc> getPlcByName(String name) {
        return Single.create(emitter -> emitter.onSuccess(plcDaoImpl.getPlcByName(name)));
    }

    /**
     * Specifies if a user is already registered in the database.
     *
     * @return true if a user is already registered; false otherwise
     */
    private boolean isEmptyDatabase() {
        return userDaoImpl.getAll().size() == 0;
    }

    /**
     * Specifies if the email is registered in the database
     *
     * @param email the email address of the user
     * @return true if the email is registered; false otherwise
     */
    private boolean isRegisteredEmail(String email) {
        return userDaoImpl.getUserByEmail(email) != null;
    }

    /**
     * Shows the progress UI and hides the login form.
     *
     * @param show true if the form is to be displayed; false otherwise
     */
    public void showProgress(boolean show) {
        Progress.show(mRegisterFormView, mProgressView,
                getResources().getInteger(android.R.integer.config_shortAnimTime),
                show);
    }
}