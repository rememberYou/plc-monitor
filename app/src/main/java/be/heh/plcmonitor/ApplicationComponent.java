/*
 * Copyright 2017 the original author or authors.
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

package be.heh.plcmonitor;

import be.heh.plcmonitor.activity.LoginActivity;
import be.heh.plcmonitor.activity.MainActivity;
import be.heh.plcmonitor.activity.RegisterActivity;
import be.heh.plcmonitor.adapter.PlcsAdapter;
import be.heh.plcmonitor.adapter.UsersAdapter;
import be.heh.plcmonitor.fragment.AddPlcFragment;
import be.heh.plcmonitor.fragment.AddUserFragment;
import be.heh.plcmonitor.fragment.EditPlcFragment;
import be.heh.plcmonitor.fragment.EditUserFragment;
import be.heh.plcmonitor.fragment.PlcsFragment;
import be.heh.plcmonitor.fragment.UsersFragment;
import be.heh.plcmonitor.preference.ControlLevelPreference;
import be.heh.plcmonitor.preference.PillsPreferenceFragment;
import be.heh.plcmonitor.preference.SettingsPreferenceFragment;
import be.heh.plcmonitor.database.DatabaseModule;

import dagger.Component;

import javax.inject.Singleton;

/**
 * Interface assigns references in the activities to have access to singletons
 * in the DatabaseModule class.
 *
 * @author Terencio Agozzino
 */
@Singleton
@Component(modules = DatabaseModule.class)
public interface ApplicationComponent {

    /**
     * Allows LoginActivity to request dependencies declared by the
     * DatabaseModule class.
     *
     * @param loginActivity the login screen that offers login via
     *                      email/password
     */
    void inject(LoginActivity loginActivity);

    /**
     * Allows MainActivity to request dependencies declared by the
     * DatabaseModule class.
     *
     * @param mainActivity the main screen of the application
     */
    void inject(MainActivity mainActivity);


    /**
     * Allows RegisterActivity to request dependencies declared by the
     * DatabaseModule class.
     *
     * @param registerActivity the register screen that offers sign up via
     *                         first name, last name, email address, and
     *                         password
     */
    void inject(RegisterActivity registerActivity);

    /**
     * Allows AddPlcFragment to request dependencies declared by the
     * DatabaseModule class.
     *
     * @param addPlcsFragment the fragment to add a new PLC
     */
    void inject(AddPlcFragment addPlcsFragment);

    /**
     * Allows AddUserFragment to request dependencies declared by the
     * DatabaseModule class.
     *
     * @param addUsersFragment the fragment to add a new user
     */
    void inject(AddUserFragment addUsersFragment);

    /**
     * Allows EditPlcFragment to request dependencies declared by the
     * DatabaseModule class.
     *
     * @param editPlcFragment the fragment to edit a PLC
     */
    void inject(EditPlcFragment editPlcFragment);

    /**
     * Allows EditUserFragment to request dependencies declared by the
     * DatabaseModule class.
     *
     * @param editUserFragment the fragment to edit a user
     */
    void inject(EditUserFragment editUserFragment);

    /**
     * Allows PlcsFragment to request dependencies declared by the
     * DatabaseModule class.
     *
     * @param plcFragment the overview fragment of the registered PLCs
     *                    for a user
     */
    void inject(PlcsFragment plcFragment);

    /**
     * Allows UsersFragment to request dependencies declared by the
     * DatabaseModule class.
     *
     * @param usersFragment the overview fragment of the registered users
     */
    void inject(UsersFragment usersFragment);

    /**
     * Allows UsersAdapter to request dependencies declared by the
     * DatabaseModule class.
     *
     * @param plcsAdapter the PLCs adapter
     */
    void inject(PlcsAdapter plcsAdapter);

    /**
     * Allows UsersAdapter to request dependencies declared by the
     * DatabaseModule class.
     *
     * @param usersAdapter the users adapter
     */
    void inject(UsersAdapter usersAdapter);

    /**
     * Allows PillsPreferenceFragment to request dependencies declared by the
     * DatabaseModule class.
     *
     * @param pillsPreferenceFragment the preference fragment of the pills PLC.
     */
    void inject(PillsPreferenceFragment pillsPreferenceFragment);

    /**
     * Allows ControlLevelPreference to request dependencies declared by the
     * DatabaseModule class.
     *
     * @param controlLevelPreference the preference fragment of the
     *                                     regulation PLC.
     */
    void inject(ControlLevelPreference controlLevelPreference);

    /**
     * Allows SettingsPreferenceFragment to request dependencies declared by the
     * DatabaseModule class.
     *
     * @param settingsPreferenceFragment the preference fragment of the user
     *                                   settings.
     */
    void inject(SettingsPreferenceFragment settingsPreferenceFragment);
}