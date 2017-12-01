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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.transition.Fade;
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
     * Request codes.
     */
    public static final int REGISTER = 2;

    @Inject
    UserDaoImpl userDaoImpl;

    ApplicationComponent applicationComponent;

    /**
     * Keep track of the register task to ensure we can cancel it if requested.
     */
    private UserRegisterTask mRegisterTask = null;

    /**
     * Building a bundle to manage transitions between the activities.
     */
    private ActivityOptions options;

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
        setupWindowAnimations();
        setContentView(R.layout.activity_register);

        applicationComponent = DaggerApplicationComponent.builder()
                .databaseModule(new DatabaseModule(this))
                .build();

        applicationComponent.inject(this);

        options = ActivityOptions.makeSceneTransitionAnimation(RegisterActivity.this);

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
                startActivityForResult(new Intent(RegisterActivity.this,
                        LoginActivity.class), REGISTER, options.toBundle());
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
     * Provides animations of window fades for exits of the Activity when it
     * has detected the user's press of the back key.
     *
     * Without override this method, animation does not occur normally.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(Activity.RESULT_CANCELED);
        startActivityForResult(new Intent(RegisterActivity.this,
                LoginActivity.class), REGISTER, options.toBundle());
    }

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
        } else if (!isValidName(firstName)) {
            mFirstNameView.setError(getString(R.string.error_invalid_firstname));
            focusView = mFirstNameView;
            cancel = true;
        }

        // Check for a valid last name.
        if (TextUtils.isEmpty(lastName)) {
            mLastNameView.setError(getString(R.string.error_field_required));
            focusView = mLastNameView;
            cancel = true;
        } else if (!isValidName(lastName)) {
            mLastNameView.setError(getString(R.string.error_invalid_lastname));
            focusView = mLastNameView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isValidEmail(email)) {
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
        } else if (!isValidPassword(password)) {
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
            mTermsPrivacyView.setButtonTintList(getResources().getColorStateList(R.color.colorRed));
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
     * Specifies the validation of a first name and a last name.
     *
     * @param name the name of the user
     * @return true if the name is a valid one; false otherwise
     */
    private boolean isValidName(String name) {
        return name.matches("^([A-Za-z]+)(\\s[A-Za-z]+)*\\s?$");
    }

    /**
     * Specifies the validation of an email address.
     *
     * @param email the email address of the user
     * @return true if the email is a valid one; false otherwise
     */
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Specifies the validation of a password.
     *
     * @param password the password of the user
     * @return true if the password is a valid one; false otherwise.
     */
    private boolean isValidPassword(String password) {
        return password.length() >= 4;
    }

    /**
     * Set up animations of window fades for entrances and exits of the
     * Activity.
     */
    private void setupWindowAnimations() {
        getWindow().setEnterTransition(new Fade().setDuration(1000));
    }

    /**
     * Shows the progress UI and hides the register form.
     *
     * On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
     * for very easy animations. If available, use these APIs to fade-in
     * the progress spinner.
     *
     * @param show equals 1 if the login form is show; 0 otherwise.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mRegisterFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
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
            showProgress(false);

            setResult(Activity.RESULT_OK);
            startActivityForResult(new Intent(RegisterActivity.this,
                    LoginActivity.class), REGISTER, options.toBundle());
        }

        /**
         * Runs on the UI thread after cancel(boolean) is invoked and
         * doInBackground(Params...) has finished.
         */
        @Override
        protected void onCancelled() {
            mRegisterTask = null;
            showProgress(false);
        }
    }
}