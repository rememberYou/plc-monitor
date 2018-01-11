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
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.TextUtils;
import android.transition.Fade;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import be.heh.plcmonitor.ApplicationComponent;
import be.heh.plcmonitor.DaggerApplicationComponent;
import be.heh.plcmonitor.R;
import be.heh.plcmonitor.dao.UserDaoImpl;
import be.heh.plcmonitor.database.DatabaseHelper;
import be.heh.plcmonitor.database.DatabaseModule;
import be.heh.plcmonitor.helper.Message;
import be.heh.plcmonitor.helper.Progress;
import be.heh.plcmonitor.model.User;
import be.heh.plcmonitor.util.Validator;

import com.j256.ormlite.cipher.android.apptools.OrmLiteBaseActivity;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

import static android.support.design.widget.Snackbar.LENGTH_LONG;

/**
 * A login screen that offers login via email/password.
 *
 * @author Terencio Agozzino
 */
public class LoginActivity extends OrmLiteBaseActivity<DatabaseHelper> {

    /**
     * Useful for debug to identify which class has logged.
     */
    private static final String TAG = LoginActivity.class.getSimpleName();

    /**
     * Request codes.
     */
    public static final int REGISTER = 1;

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
     * Building a bundle to manage transitions between the activities.
     */
    private ActivityOptions options;

    /**
     * Saves the content of the user's session.
     */
    private SharedPreferences sp;

    /**
     * UI references.
     */
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private View mCoordinatorLayoutView;

    /**
     * Called when the activity is starting.
     *
     * @param savedInstanceState if the activity is being re-initialized after
     *                           previously being shut down then this Bundle
     *                           contains the data it most recently supplied in
     *                           onSaveInstanceState(Bundle)
     *                           Note: Otherwise it is null
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupWindowAnimations();
        setContentView(R.layout.activity_login);

        applicationComponent = DaggerApplicationComponent.builder()
                .databaseModule(new DatabaseModule(this))
                .build();
        applicationComponent.inject(this);

        mEmailView = findViewById(R.id.actv_login_email);

        mPasswordView = findViewById(R.id.et_login_password);
        mPasswordView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin();
                return true;
            }
            return false;
        });

        mCoordinatorLayoutView = findViewById(R.id.snackbarPosition);

        Button mSignInButton = findViewById(R.id.btn_sign_in);
        mSignInButton.setOnClickListener(view -> attemptLogin());

        mLoginFormView = findViewById(R.id.scroll_login);
        mProgressView = findViewById(R.id.pg_login);

        TextView mRegisterView = findViewById(R.id.tv_register);
        mRegisterView.setOnClickListener(view -> {
            mEmailView.setText("");
            mPasswordView.setText("");
            options = ActivityOptions.makeSceneTransitionAnimation(LoginActivity.this);

            startActivityForResult(new Intent(LoginActivity.this,
                    RegisterActivity.class), REGISTER, options.toBundle());
        });

        sp = getSharedPreferences("login", MODE_PRIVATE);

        Bundle extras = getIntent().getExtras();

        if (extras != null && extras.getBoolean("signOff")) {
            Message.display(mCoordinatorLayoutView,
                    Html.fromHtml(getString(R.string.message_disconnect)),
                    LENGTH_LONG);
        }
    }

    /**
     * Specifies if an account has been created or logged out.
     *
     * @param requestCode the integer request code originally supplied to
     *                    startActivityForResult(), allowing to identify who
     *                    this result came from
     * @param resultCode the integer result code returned by RegisterActivity
     *                   through its setResult()
     * @param data an Intent, which can return result data to the LoginActivity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REGISTER && resultCode == RESULT_OK) {
            Message.display(mCoordinatorLayoutView,
                    Html.fromHtml(getString(R.string.message_successful_user_creation)),
                    LENGTH_LONG);
        }
    }

    /**
     * Prevents the user from returning to the MainActivity until logged in.
     */
    @Override
    public void onBackPressed() { return; }

    /**
     * Attempts to sign in the account specified by the login form. If there
     * are form errors (invalid email, missing fields, etc.), the errors are
     * presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!Validator.isValidEmail(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        } else if (!isRegisteredEmail(email)) {
            mEmailView.setError(getString(R.string.error_incorrect_email));
            focusView = mEmailView;
            cancel = true;
        }

        // Check for a valid password.
        if (!TextUtils.isEmpty(password) && !Validator.isValidPassword(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);

            login(email, password)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableSingleObserver<Boolean>() {

                        /**
                         * Notifies the SingleObserver with a single boolean
                         * value and that the Single has finished sending
                         * push-based notifications.
                         *
                         * @param success true if the user logged in; otherwise
                         *                false
                         */
                        @Override
                        public void onSuccess(Boolean success) {
                            showProgress(false);

                            if (success) {

                                getUserByEmail(email)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribeWith(new DisposableSingleObserver<User>() {

                                            /**
                                             * Notifies the SingleObserver with
                                             * a single user value and that the
                                             * Single has finished sending
                                             * push-based notifications.
                                             */
                                            @Override
                                            public void onSuccess(User user) {
                                                buildUserSharedPreferences(user);
                                            }

                                            /**
                                             * Called once if login registration
                                             * 'throws' an exception.
                                             *
                                             * @param e the exception, not null
                                             */
                                            @Override
                                            public void onError(@NonNull Throwable e) {
                                                Log.e(TAG, "Unable to build " +
                                                        "SharedPreferences for the user", e);
                                            }

                                        });

                                setResult(Activity.RESULT_OK);
                                finish();
                                dispose();
                            } else {
                                mPasswordView.setError(getString(R.string.error_incorrect_password));
                                mPasswordView.requestFocus();
                            }
                        }

                        /**
                         * Called once if login registration 'throws' an exception.
                         *
                         * @param e the exception, not null
                         */
                        @Override
                        public void onError(@NonNull Throwable e) {
                            Log.e(TAG, "Unable to login the user", e);
                            showProgress(false);
                        }
                    });
        }
    }

    /**
     * Commits the user preferences to the SharedPreferences object it is editing
     *
     * @param user the user to commits preferences to the SharedPreferences
     */
    public void buildUserSharedPreferences(User user) {
        sp = getSharedPreferences("login", MODE_PRIVATE);
        sp.edit().putInt("id", user.getId()).apply();
        sp.edit().putString("firstName", user.getFirstName()).apply();
        sp.edit().putString("lastName", user.getLastName()).apply();
        sp.edit().putString("email", user.getEmail()).apply();
        sp.edit().putString("password", user.getPassword()).apply();
        sp.edit().putInt("permission", user.getPermission()).apply();
        sp.edit().putBoolean("logged", true).apply();
    }

    /**
     * Specifies if the email is registered in the database
     *
     * @param email the email address of the user
     * @return true if the email is registered; false otherwise
     */
    private Boolean isRegisteredEmail(String email) {
        return userDaoImpl.getUserByEmail(email) != null;
    }

    /**
     * Emits either a single user value according to his email address.
     *
     * @return the new Single instance
     */
    public Single<User> getUserByEmail(String email) {
        return Single.create(emitter -> emitter.onSuccess(userDaoImpl.getUserByEmail(email)));
    }

    /**
     * Emits either a single successful value for the login of the user, or
     * an error.
     *
     * @param email the email address of the user
     * @param password password the password of the user
     * @return the new Single instance
     */
    public Single<Boolean> login(String email, String password) {
        return Single.create(emitter -> {
            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            emitter.onSuccess(Validator.isValidConnection(
                    userDaoImpl, email, password));
        });
    }

    /**
     * Set up animations of window fades for entrances and exits of the
     * Activity.
     */
    private void setupWindowAnimations() {
        getWindow().setEnterTransition(new Fade().setDuration(1000));
    }

    /**
     * Shows the progress UI and hides the login form.
     *
     * @param show true if the form is to be displayed; false otherwise
     */
    public void showProgress(boolean show) {
        Progress.show(mLoginFormView, mProgressView,
                getResources().getInteger(android.R.integer.config_shortAnimTime),
                show);
    }
}