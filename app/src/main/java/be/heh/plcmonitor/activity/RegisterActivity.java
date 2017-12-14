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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.Html;
import android.text.TextUtils;
import android.transition.Fade;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import be.heh.plcmonitor.DaggerApplicationComponent;
import be.heh.plcmonitor.R;
import be.heh.plcmonitor.dao.UserDaoImpl;
import be.heh.plcmonitor.database.DatabaseHelper;
import be.heh.plcmonitor.ApplicationComponent;
import be.heh.plcmonitor.database.DatabaseModule;
import be.heh.plcmonitor.model.User;
import be.heh.plcmonitor.utils.Validator;
import be.heh.plcmonitor.widget.Progress;

import com.afollestad.materialdialogs.MaterialDialog;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

import javax.inject.Inject;

/**
 * A register screen that offers sign up via first name, last name,
 * email address, and password.
 *
 * @author Terencio Agozzino
 */
public class RegisterActivity extends OrmLiteBaseActivity<DatabaseHelper> {

    /**
     * Components.
     */
    ApplicationComponent applicationComponent;

    /**
     * Injections.
     */
    @Inject
    UserDaoImpl userDaoImpl;

    /**
     * Keep track of the register task to ensure we can cancel it if requested.
     */
    private UserRegisterTask mRegisterTask = null;

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

        applicationComponent = DaggerApplicationComponent.builder()
                .databaseModule(new DatabaseModule(this))
                .build();
        applicationComponent.inject(this);

        mEmailView = findViewById(R.id.actv_register_email);

        mFirstNameView = findViewById(R.id.et_register_firstname);
        mFirstNameView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });

        mLastNameView = findViewById(R.id.et_register_lastname);
        mLastNameView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });

        mPasswordView = findViewById(R.id.et_register_password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });

        mPasswordConfirmView = findViewById(R.id.et_register_password_confirm);
        mPasswordConfirmView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });

        mTermsPrivacyView = findViewById(R.id.chk_terms_privacy);

        Button mRegisterButton = findViewById(R.id.btn_register);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        mRegisterFormView = findViewById(R.id.scroll_register);
        mProgressView = findViewById(R.id.pg_register);

        TextView mLoginView = findViewById(R.id.tv_login);

        mLoginView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });

        MaterialDialog.Builder mPrivacyPolicyBuilder = new MaterialDialog.Builder(this)
                .title(R.string.prompt_privacy_policy)
                .content(Html.fromHtml(getString(R.string.dialog_privacy_policy)))
                .positiveText(R.string.action_back);

        mPrivacyPolicyDialog = mPrivacyPolicyBuilder.build();

        TextView mPrivacyPolicyView = findViewById(R.id.tv_privacy_policy);
        mPrivacyPolicyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPrivacyPolicyDialog.show();
            }
        });

        MaterialDialog.Builder mTermsUseBuilder = new MaterialDialog.Builder(this)
                .title(R.string.prompt_terms_use)
                .content(Html.fromHtml(getString(R.string.dialog_terms_use)))
                .positiveText(R.string.action_back);

        mTermsUseDialog = mTermsUseBuilder.build();

        TextView mTermsUseView = findViewById(R.id.tv_terms_use);
        mTermsUseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTermsUseDialog.show();
            }
        });
    }

    /**
     * If not override, it will starts a LoginActivity when the user pressed
     * the back button.
     */
    @Override
    public void onBackPressed() { finish();  }

    /**
     * Attempts to register the account specified by the register form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual register attempt is made.
     */
    private void attemptRegister() {
        if (mRegisterTask != null) {
            return;
        }

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
            mFirstNameView.setError(getString(R.string.error_invalid_firstname));
            focusView = mFirstNameView;
            cancel = true;
        }

        // Check for a valid last name.
        if (TextUtils.isEmpty(lastName)) {
            mLastNameView.setError(getString(R.string.error_field_required));
            focusView = mLastNameView;
            cancel = true;
        } else if (!Validator.isValidName(lastName)) {
            mLastNameView.setError(getString(R.string.error_invalid_lastname));
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
            Progress.show(mRegisterFormView, mProgressView,
                    getResources().getInteger(android.R.integer.config_shortAnimTime),
                    true);
            mRegisterTask = new UserRegisterTask(firstName, lastName, email, password);
            mRegisterTask.execute((Void) null);
        }
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
     * Represents an asynchronous register task used to register the user.
     */
    @SuppressLint("StaticFieldLeak")
    public class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {

        private final String firstName;
        private final String lastName;
        private final String email;
        private final String password;

        /**
         * Main constructor of the user register task.
         *
         * @param firstName the first name of the user
         * @param lastName the last name of the user
         * @param email the email address of the user
         * @param password the password of the user
         */
        UserRegisterTask(String firstName, String lastName,
                         String email, String password) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.password = password;
        }

        /**
         * Performs a computation on a background thread.
         *
         * @param params the parameters of the task
         * @return the result, defined by the subclass of this task
         */
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }
            userDaoImpl.create(new User(firstName, lastName, email, password));

            return true;
        }

        /**
         * Runs on the UI thread after doInBackground(Params...).
         *
         * @param success the result of the operation computed by
         *                doInBackground(Params...).
         */
        @Override
        protected void onPostExecute(final Boolean success) {
            mRegisterTask = null;
            Progress.show(mRegisterFormView, mProgressView,
                    getResources().getInteger(android.R.integer.config_shortAnimTime),
                    false);

            setResult(Activity.RESULT_OK);
            finish();
        }

        /**
         * Runs on the UI thread after cancel(boolean) is invoked and
         * doInBackground(Params...) has finished.
         */
        @Override
        protected void onCancelled() {
            mRegisterTask = null;
            Progress.show(mRegisterFormView, mProgressView,
                    getResources().getInteger(android.R.integer.config_shortAnimTime),
                    false);
        }
    }
}