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
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.transition.Fade;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import be.heh.plcmonitor.DaggerApplicationComponent;
import be.heh.plcmonitor.R;
import be.heh.plcmonitor.dao.UserDaoImpl;
import be.heh.plcmonitor.database.DatabaseHelper;
import be.heh.plcmonitor.database.DatabaseModule;
import be.heh.plcmonitor.ApplicationComponent;
import be.heh.plcmonitor.model.User;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

import javax.inject.Inject;

/**
 * A login screen that offers login via email/password.
 *
 * @author Terencio Agozzino
 */
public class LoginActivity extends OrmLiteBaseActivity<DatabaseHelper> {

    /**
     * Request codes.
     */
    public static final int REGISTER = 2;
    public static final int SIGN_OFF = 3;

    @Inject
    DatabaseHelper databaseHelper;

    @Inject
    UserDaoImpl userDaoImpl;

    ApplicationComponent applicationComponent;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    /**
     * Building a bundle to manage transitions between the activities.
     */
    private ActivityOptions options;

    /**
     * UI references.
     */
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

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
        setContentView(R.layout.activity_login);

        applicationComponent = DaggerApplicationComponent.builder()
                .databaseModule(new DatabaseModule(this))
                .build();

        applicationComponent.inject(this);

        options = ActivityOptions.makeSceneTransitionAnimation(LoginActivity.this);

        mEmailView = findViewById(R.id.actv_login_email);

        mPasswordView = findViewById(R.id.et_login_password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mSignInButton = findViewById(R.id.btn_sign_in);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.scroll_login);
        mProgressView = findViewById(R.id.pg_login);

        TextView mRegisterView = findViewById(R.id.tv_register);
        mRegisterView.setOnClickListener(new OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v) {
                mEmailView.setText("");
                mPasswordView.setText("");
                startActivityForResult(new Intent(LoginActivity.this,
                        RegisterActivity.class), REGISTER, options.toBundle());
            }
        });

        for (User user : userDaoImpl.getAll()) {
            Log.i("TEST", user.toString());
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
            displayMessage(getString(R.string.prompt_successful_creation));
        } else if (requestCode == SIGN_OFF && resultCode == RESULT_OK) {
            displayMessage(getString(R.string.message_disconnect));
        }
    }

    /**
     * Attempts to sign in the account specified by the login form. If there
     * are form errors (invalid email, missing fields, etc.), the errors are
     * presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isValidEmail(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        } else if (!isRegisteredEmail(email)) {
            mEmailView.setError(getString(R.string.error_incorrect_email));
            focusView = mEmailView;
            cancel = true;
        }

        // Check for a valid password.
        if (!TextUtils.isEmpty(password) && !isValidPassword(password)) {
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
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Display a message with a Snackbar.
     *
     * @param message the message to display
     */
    public void displayMessage(String message) {
        View coordinatorLayoutView = findViewById(R.id.snackbarPosition);

        Snackbar mSnackbar = Snackbar.make(coordinatorLayoutView, message,
                Snackbar.LENGTH_LONG);

        View mView = mSnackbar.getView();
        TextView mTextView = mView.findViewById(android.support.design.R.id.snackbar_text);
        mTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        mSnackbar.show();
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
     * Specifies the validation of a connection.
     *
     * @param email the email address of the user
     * @param password the password of the user
     * @return true if the connection is a valid one; false otherwise
     */
    private boolean isValidConnection(String email, String password) {
        return userDaoImpl.getUserByEmail(email).getPassword().equals(password);
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
     * @return true if the password is a valid one; false otherwise
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
     * Shows the progress UI and hides the login form.
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

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
     * Represents an asynchronous login task used to authenticate the user.
     */
    @SuppressLint("StaticFieldLeak")
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String email;
        private final String password;

        /**
         * Main constructor of the user login task.
         *
         * @param email the email address of the user
         * @param password the password of the user
         */
        UserLoginTask(String email, String password) {
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

            return isValidConnection(email, password);
        }

        /**
         * Runs on the UI thread after doInBackground(Params...).
         *
         * @param success the result of the operation computed by
         *                doInBackground(Params...).
         */
        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                User user = userDaoImpl.getUserByEmail(email);

                SharedPreferences sp = getSharedPreferences("login", MODE_PRIVATE);
                sp.edit().putString("firstName", user.getFirstName()).apply();
                sp.edit().putString("lastName", user.getLastName()).apply();
                sp.edit().putString("email", user.getEmail()).apply();
                sp.edit().putString("password", user.getPassword()).apply();
                sp.edit().putBoolean("logged", true).apply();

                // If use finish(), it provide some issues when the user
                // register himself with RegisterActivity and came back to
                // LoginActivity because it will switch back to
                // RegisterActivity instead of MainActivity.
                setResult(Activity.RESULT_OK);
                startActivityForResult(new Intent(LoginActivity.this,
                        MainActivity.class), REGISTER, options.toBundle());
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        /**
         * Runs on the UI thread after cancel(boolean) is invoked and
         * doInBackground(Params...) has finished.
         */
        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}