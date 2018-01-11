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

package be.heh.plcmonitor.preference;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import be.heh.plcmonitor.ApplicationComponent;
import be.heh.plcmonitor.DaggerApplicationComponent;
import be.heh.plcmonitor.R;
import be.heh.plcmonitor.activity.MainActivity;
import be.heh.plcmonitor.dao.UserDaoImpl;
import be.heh.plcmonitor.database.DatabaseModule;
import be.heh.plcmonitor.helper.Message;
import be.heh.plcmonitor.model.User;
import be.heh.plcmonitor.util.Validator;

import com.afollestad.materialdialogs.MaterialDialog;

import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;
import static android.support.design.widget.Snackbar.LENGTH_LONG;

/**
 * Represents the general and account settings screen of the user.
 *
 * @author Terencio Agozzino
 */
public class SettingsPreferenceFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * Useful for debug to identify which class has logged.
     */
    private static final String TAG = SettingsPreferenceFragment.class.getSimpleName();

    /**
     * Injections.
     */
    @Inject
    UserDaoImpl userDaoImpl;

    /**
     * The user who needs to have the information edited.
     */
    private User mUser;

    /**
     * UI references.
     */
    private EditTextPreference mPasswordPreference;
    private NavigationView mNavigationView;
    private TextView mEmailView;
    private TextView mNameView;

    /**
     * Retrieves and hold the contents of the user from his past session.
     */
    private SharedPreferences sp;

    /**
     * Called during onCreate(Bundle) to supply the preferences for this
     * fragment.
     *
     * @param savedInstanceState saved state given so that the fragment can be
     *                           re-constructed
     * @param rootKey fragment should be rooted at the PreferenceScreen with
     *                this key
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);

        ApplicationComponent applicationComponent =
                DaggerApplicationComponent.builder()
                        .databaseModule(new DatabaseModule(getActivity()
                                .getApplication()))
                        .build();
        applicationComponent.inject(this);

        sp = getActivity().getSharedPreferences("login", MODE_PRIVATE);
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
                        mUser = user;
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

        mNavigationView = getActivity().findViewById(R.id.nav_view);
        View headerLayout = mNavigationView.getHeaderView(0);

        mEmailView = headerLayout.findViewById(R.id.tv_email);
        mNameView = headerLayout.findViewById(R.id.tv_name);

        EditTextPreference mFirstNamePreference =
                (EditTextPreference) findPreference("firstName");
        mFirstNamePreference.setSummary(sp.getString("firstName", ""));
        mFirstNamePreference.setText(sp.getString("firstName", ""));

        EditTextPreference mNamePreference =
                (EditTextPreference) findPreference("lastName");
        mNamePreference.setSummary(sp.getString("lastName", ""));
        mNamePreference.setText(sp.getString("lastName", ""));

        EditTextPreference mEmailPreference =
                (EditTextPreference) findPreference("email");
        mEmailPreference.setSummary(sp.getString("email", ""));
        mEmailPreference.setText(sp.getString("email", ""));

        mPasswordPreference =
                (EditTextPreference) findPreference("password");
        mPasswordPreference.setSummary(hidePassword(sp.getString("password", ""), '*'));
        mPasswordPreference.setText("");

        Preference button = findPreference("delete_account");
        button.setOnPreferenceClickListener(preference -> {
                deletePreventionDialog(mUser).show();

            return true;
        });
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
     * Updates a user in the database.
     *
     * @param user the user to update
     * @return the new Completable instance
     */
    public Completable updateUser(User user) {
        return Completable.create(emitter -> {
            userDaoImpl.update(user);
            emitter.onComplete();
        });
    }

    /**
     * Verifies whether a user can be deleted.
     *
     * @param user the user to delete
     * @return true if the user can be deleted; otherwise false
     */
    private boolean canDeleteUser(User user) {
        return !user.isAdmin() || isAnotherAdminInDatabase(user);
    }

    /**
     * Constructs a prevention dialog box when deleting a user.
     *
     * @param user the user to delete
     * @return a new prevention dialog box
     */
    private MaterialDialog.Builder deletePreventionDialog(User user) {
        return new MaterialDialog.Builder(getActivity())
                .title("Deletion Confirmation")
                .content(R.string.dialog_user_delete)
                .negativeText("Cancel")
                .positiveText("Agree")
                .onPositive((dialog, which) -> deleteUser(user));
    }

    /**
     * Deletes the user when possible by returning a message to indicate that
     * the user has been deleted.
     *
     * @param user the user to delete
     */
    private void deleteUser(User user) {
        if (canDeleteUser(user)) {
            userDaoImpl.delete(user);

            mNavigationView.setCheckedItem(R.id.nav_sign_off);
            ((MainActivity) getActivity()).onNavigationItemSelected(
                    mNavigationView.getMenu().findItem(R.id.nav_sign_off));

            if (getView() != null) {
                Message.display(getView(),
                        Html.fromHtml(getString(R.string.message_successful_user_delete)),
                        LENGTH_LONG);
            }
        } else if (getView() != null) {
            Message.display(getView(),
                    Html.fromHtml(getString(R.string.message_failure_delete)),
                    LENGTH_LONG);
        }
    }

    /**
     * Hides user's password.
     *
     * @param password the user's password
     * @return The password of the user hidden by symbols
     */
    private String hidePassword(String password, char symbol) {
        return StringUtils.repeat(symbol, password.length());
    }

    /**
     * Verifies if there is another administrator in the database.
     *
     * @param admin the user with privileges
     * @return true if there is another administrator in the database; false
     *         otherwise
     */
    private boolean isAnotherAdminInDatabase(User admin) {
        for (User user : userDaoImpl.getAll()) {
            if (user.isAdmin() && !user.equals(admin)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Registers a callback to be invoked when a change happens to a
     * preference when the fragment will start interacting with the user.
     */
    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager()
                .getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Unregisters a previous callback when the system is about to start
     * resuming the SettingsPreferenceFragment.
     */
    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager()
                .getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Called when a shared preference is changed, added, or removed. This may
     * be called even if a preference is set to its existing value.
     *
     * @param sharedPreferences the SharedPreferences that received the change
     * @param key the key of the preference that was changed, added, or removed
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        Preference preference = findPreference(key);

        if (preference instanceof CheckBoxPreference) {
            updateLoginRemember((CheckBoxPreference) preference);
        }

        if (preference instanceof EditTextPreference) {
            String value = ((EditTextPreference) preference).getText();

            boolean update = false;
            String message = "";
            String tmp = "";

            switch (key) {
                case "firstName":
                    tmp = mUser.getFirstName();

                    if (Validator.isValidName(value)) {
                        updateFirstName(mUser, value);
                        update = true;
                    } else {
                        message = "Invalid first name";
                    }
                    break;
                case "lastName":
                    tmp = mUser.getLastName();

                    if (Validator.isValidName(value)) {
                        updateLastName(mUser, value);
                        update = true;
                    } else {
                        message = "Invalid last name";
                    }
                    break;
                case "email":
                    tmp = mUser.getEmail();

                    if (Validator.isValidEmail(value) && !isRegisteredEmail(value)) {
                        updateEmail(mUser, value);
                        update = true;
                    } else {
                        message = "Invalid/Existing email address";
                    }
                    break;
                case "password":
                    if (Validator.isValidPassword(value)) {
                        updatePassword(mUser, value);
                        update = true;
                    } else {
                        message = "Invalid password";
                    }
                    break;
                default:
                    Log.e(TAG, key);
                    break;
            }

            if (update) {
                updateSummary((EditTextPreference) preference);

                if (key.equals("password")) {
                    mPasswordPreference.setSummary(hidePassword(value, '*'));
                }

                updateUser(mUser)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableCompletableObserver() {

                            /**
                             * Called once updating user information completes normally.
                             */
                            @Override
                            public void onComplete() {
                                if (getView() != null) {
                                    Message.display(getView(),
                                            Html.fromHtml(getString(
                                                    R.string.message_successful_user_update)),
                                            LENGTH_LONG);
                                }
                            }

                            /**
                             * Called once if updating user information 'throws' an exception.
                             *
                             * @param e the exception, not null
                             */
                            @Override
                            public void onError(@NonNull Throwable e) {
                                Log.e(TAG, "Unable to update the user information", e);
                            }
                        });

            } else if (getView() != null) {
                ((EditTextPreference) preference).setText(tmp);

                Message.display(getView(),
                        Html.fromHtml(getString(R.string.message_failure_settings, message)),
                        LENGTH_LONG);
            }
        }
    }

    /**
     * Updates the email address of the logged-in user.
     *
     * @param user the logged-in user
     * @param email the modified email address
     */
    private void updateEmail(User user, String email) {
        user.setEmail(email);
        mEmailView.setText(email);
        sp.edit().putString("email", email).apply();
    }

    /**
     * Updates the first name of the logged-in user.
     *
     * @param user the logged-in user
     * @param firstName the modified first name
     */
    private void updateFirstName(User user, String firstName) {
        user.setFirstName(firstName);
        mNameView.setText(getString(R.string.prompt_user_name,
                firstName.toUpperCase(),
                user.getLastName().toUpperCase()));
        sp.edit().putString("firstName", firstName).apply();
    }

    /**
     * Updates the last name of the logged-in user.
     *
     * @param user the logged-in user
     * @param lastName the modified last name
     */
    private void updateLastName(User user, String lastName) {
        user.setLastName(lastName);
        mNameView.setText(getString(R.string.prompt_user_name,
                user.getFirstName().toUpperCase(),
                lastName.toUpperCase()));
        sp.edit().putString("lastName", lastName).apply();
    }

    /**
     * Updates user-related login settings.
     *
     * @param preference basic Preference UI
     */
    private void updateLoginRemember(CheckBoxPreference preference) {
        if (preference.isChecked()) {
            sp.edit().putBoolean("logged", true).apply();
        } else {
            sp.edit().putBoolean("logged", false).apply();
        }
    }

    /**
     * Updates the password of the logged-in user.
     *
     * @param user the logged-in user
     * @param password the modified password
     */
    private void updatePassword(User user, String password) {
        user.setPassword(password);
        sp.edit().putString("password", password).apply();
    }

    /**
     * Updates the summary of a EditTextPreference UI.
     *
     * @param preference basic Preference UI
     */
    private void updateSummary(EditTextPreference preference) {
        preference.setSummary(preference.getText());
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
}