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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v14.preference.PreferenceFragment;
import android.util.Log;
import android.widget.TextView;

import javax.inject.Inject;

import be.heh.plcmonitor.ApplicationComponent;
import be.heh.plcmonitor.DaggerApplicationComponent;
import be.heh.plcmonitor.R;
import be.heh.plcmonitor.dao.UserDaoImpl;
import be.heh.plcmonitor.database.DatabaseModule;
import be.heh.plcmonitor.model.User;
import be.heh.plcmonitor.utils.Validator;

import static android.content.Context.MODE_PRIVATE;

/**
 * Represents the general and account settings screen of the user.
 *
 * @author Terencio Agozzino
 */
public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

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
     * UI references.
     */
    EditTextPreference mFirstNamePreference;
    EditTextPreference mNamePreference;
    EditTextPreference mEmailPreference;
    EditTextPreference mPasswordPreference;
    TextView mEmailView;
    TextView mNameView;

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

        applicationComponent = DaggerApplicationComponent.builder()
                .databaseModule(new DatabaseModule(getActivity().getApplication()))
                .build();
        applicationComponent.inject(this);

        sp = getActivity().getSharedPreferences("login", MODE_PRIVATE);

        mFirstNamePreference = (EditTextPreference) findPreference("firstName");
        mFirstNamePreference.setSummary(sp.getString("firstName", ""));
        mFirstNamePreference.setText(sp.getString("firstName", ""));

        mNamePreference = (EditTextPreference) findPreference("lastName");
        mNamePreference.setSummary(sp.getString("lastName", ""));
        mNamePreference.setText(sp.getString("lastName", ""));

        mEmailPreference = (EditTextPreference) findPreference("email");
        mEmailPreference.setSummary(sp.getString("email", ""));
        mEmailPreference.setText(sp.getString("email", ""));

        mPasswordPreference = (EditTextPreference) findPreference("password");
        mPasswordPreference.setSummary(sp.getString("password", ""));
        mPasswordPreference.setText(sp.getString("password", ""));

        mEmailView = getActivity().findViewById(R.id.tv_email);
        mNameView = getActivity().findViewById(R.id.tv_name);
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
     * resuming the SettingsFragment.
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
            User user = userDaoImpl.get(sp.getInt("id", 0));

            switch (key) {
                case "firstName":
                    if (Validator.isValidName(value)) {
                        updateFirstName(user, value);
                    }
                    break;
                case "lastName":
                    if (Validator.isValidName(value)) {
                        updateLastName(user, value);
                    }
                    break;
                case "email":
                    if (Validator.isValidEmail(value)) {
                        updateEmail(user, value);
                    }
                    break;
                case "password":
                    if (Validator.isValidPassword(value)) {
                        updatePassword(user, value);
                    }
                    break;
                default:
                    Log.i("Settings", key);
                    break;
            }

            updateSummary((EditTextPreference) preference);
            userDaoImpl.update(user);
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
}