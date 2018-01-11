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
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.transition.Fade;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import be.heh.plcmonitor.ApplicationComponent;
import be.heh.plcmonitor.DaggerApplicationComponent;
import be.heh.plcmonitor.R;
import be.heh.plcmonitor.dao.UserDaoImpl;
import be.heh.plcmonitor.database.DatabaseModule;
import be.heh.plcmonitor.fragment.ConnectionFragment;
import be.heh.plcmonitor.fragment.PlcsFragment;
import be.heh.plcmonitor.fragment.UsersFragment;
import be.heh.plcmonitor.preference.ControlLevelPreference;
import be.heh.plcmonitor.preference.PillsPreferenceFragment;
import be.heh.plcmonitor.preference.SettingsPreferenceFragment;
import be.heh.plcmonitor.model.User;

import com.afollestad.materialdialogs.MaterialDialog;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Main screen of the application.
 *
 * @author Terencio Agozzino
 */
public class MainActivity extends Activity
        implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * Request codes.
     */
    public static final int LOGIN = 1;

    /**
     * Injections.
     */
    @Inject
    UserDaoImpl userDaoImpl;

    /**
     * Logged user.
     */
    private User user;

    /**
     * Retrieves and hold the contents of the user from his past session.
     */
    private SharedPreferences sp;

    /**
     * UI references.
     */
    private MaterialDialog mAboutDialog;
    private MenuItem mItemUsers;
    private NavigationView mNavigationView;
    private TextView mEmailView;
    private TextView mNameView;
    private TextView mProfileView;

    /**
     * Says whether or not a welcoming message needs to be sent out.
     */
    private boolean isNeedWelcome;

    /**
     * Welcoming message.
     */
    private String msgWelcome;

    private List<Fragment> fragments;

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
        setContentView(R.layout.activity_main);

        SQLiteDatabase.loadLibs(this);

        ApplicationComponent applicationComponent =
                DaggerApplicationComponent.builder()
                        .databaseModule(new DatabaseModule(this))
                        .build();
        applicationComponent.inject(this);

        Toolbar toolbar = findViewById(R.id.toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        View headerLayout = mNavigationView.getHeaderView(0);
        mEmailView = headerLayout.findViewById(R.id.tv_email);
        mNameView = headerLayout.findViewById(R.id.tv_name);
        mProfileView = headerLayout.findViewById(R.id.tv_profile);

        MaterialDialog.Builder mTermsUseBuilder = new MaterialDialog.Builder(this)
                .title(R.string.prompt_about)
                .content(Html.fromHtml(getString(R.string.dialog_about)))
                .positiveText(R.string.action_back);

        mAboutDialog = mTermsUseBuilder.build();

        sp = getSharedPreferences("login", MODE_PRIVATE);
        final boolean logged = sp.getBoolean("logged", false);

        fragments = new ArrayList<>();

        if (logged) {
            user = new User(
                    sp.getString("firstName", ""),
                    sp.getString("lastName", ""),
                    sp.getString("email", ""),
                    sp.getString("password", ""),
                    sp.getInt("permission", 0));

            mNameView.setText(getString(R.string.prompt_user_name,
                    user.getFirstName().toUpperCase(),
                    user.getLastName().toUpperCase()));
            mEmailView.setText(user.getEmail());

            if (user.isAdmin()) {
                mProfileView.setText(getString(R.string.prompt_profile_admin));
            } else {
                mProfileView.setText(getString(R.string.prompt_profile_user));
            }

            if (user.isAdmin()) {
                Menu menu = mNavigationView.getMenu();
                mItemUsers = menu.add(R.id.generalGroup, Menu.NONE, Menu.NONE, "Users");
                mItemUsers.setIcon(R.drawable.ic_person_black_24dp);
                mItemUsers.setCheckable(true);
            }

            isNeedWelcome = true;
            msgWelcome = getString(R.string.message_welcome_back, user.getFirstName());

            mNavigationView.setCheckedItem(R.id.nav_plc);
            onNavigationItemSelected(mNavigationView.getMenu().findItem(R.id.nav_plc));
        } else {
            startActivityForResult(new Intent(this, LoginActivity.class), LOGIN);
        }
    }

    /**
     * Specifies if an account has been logged.
     *
     * @param requestCode the integer request code originally supplied to
     *                    startActivityForResult(), allowing to identify who
     *                    this result came from
     * @param resultCode the integer result code returned by LoginActivity
     *                   through its setResult()
     * @param data an Intent, which can return user data to the MainActivity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOGIN && resultCode == RESULT_OK) {
            user = new User(
                    sp.getString("firstName", ""),
                    sp.getString("lastName", ""),
                    sp.getString("email", ""),
                    sp.getString("password", ""),
                    sp.getInt("permission", 0));

            mNameView.setText(getString(R.string.prompt_user_name,
                    user.getFirstName(), user.getLastName()));
            mEmailView.setText(user.getEmail());

            isNeedWelcome = true;
            msgWelcome = getString(R.string.message_welcome, user.getFirstName());

            if (user.isAdmin()) {
                Menu menu = mNavigationView.getMenu();
                mItemUsers = menu.add(R.id.generalGroup, Menu.NONE, Menu.NONE, "Users");
                mItemUsers.setIcon(R.drawable.ic_person_black_24dp);
                mItemUsers.setCheckable(true);
                mProfileView.setText(getString(R.string.prompt_profile_admin));
            } else {
                mProfileView.setText(getString(R.string.prompt_profile_user));
            }

            mNavigationView.setCheckedItem(R.id.nav_plc);
            onNavigationItemSelected(mNavigationView.getMenu().getItem(1));
        }
    }

    /**
     * Closes the navigation menu if it is opened, when the Activity has
     * detected the user's press of the back key.
     */
    @Override
    public void onBackPressed() {
        if (ControlLevelPreference.isRunning.get()) {
            ControlLevelPreference.readThread.interrupt();
            ControlLevelPreference.plcConnection.close();
            ControlLevelPreference.isRunning.set(false);
        } else if (PillsPreferenceFragment.isRunning.get()) {
            PillsPreferenceFragment.readThread.interrupt();
            PillsPreferenceFragment.plcConnection.close();
            PillsPreferenceFragment.isRunning.set(false);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Called when an item in the navigation menu is selected.
     *
     * @param item the selected item
     * @return true to display the item as the selected item; false otherwise
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;
        Class fragmentClass;

        removeAddFragmentsForm();

        if (id == R.id.nav_connect) {
            hideFragments(fragments);

            fragmentClass = ConnectionFragment.class;

            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }

            fragments.add(fragment);

            getFragmentManager().beginTransaction()
                    .add(R.id.snackbarPosition, fragment, "ConnectionFragment")
                    .show(fragment)
                    .commit();
        } else if (id == R.id.nav_plc) {
            Fragment plcFragment = getFragmentManager()
                    .findFragmentByTag("PlcsFragment");

            hideFragments(fragments);

            if (plcFragment == null) {
                fragmentClass = PlcsFragment.class;

                try {
                    fragment = (Fragment) fragmentClass.newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (isNeedWelcome) {
                    Bundle bundle = new Bundle();
                    bundle.putString("message", msgWelcome);
                    fragment.setArguments(bundle);
                    isNeedWelcome = false;
                }

                fragments.add(fragment);

                getFragmentManager().beginTransaction()
                        .add(R.id.snackbarPosition, fragment, "PlcsFragment")
                        .show(fragment)
                        .commit();
            } else {
                getFragmentManager().beginTransaction()
                        .show(plcFragment)
                        .commit();
            }

        } else if (id == Menu.NONE) {
            Fragment usersFragment = getFragmentManager()
                    .findFragmentByTag("UsersFragment");

            hideFragments(fragments);

            if (usersFragment == null) {
                fragmentClass = UsersFragment.class;

                try {
                    fragment = (Fragment) fragmentClass.newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                fragments.add(fragment);

                getFragmentManager().beginTransaction()
                        .add(R.id.snackbarPosition, fragment, "UsersFragment")
                        .show(fragment)
                        .commit();
            } else {
                getFragmentManager().beginTransaction()
                        .show(usersFragment)
                        .commit();
            }

        } else if (id == R.id.about) {
            mAboutDialog.show();
        } else if (id == R.id.nav_settings) {
            Fragment settingsFragment = getFragmentManager()
                    .findFragmentByTag("SettingsFragment");

            hideFragments(fragments);

            if (settingsFragment == null) {
                fragmentClass = SettingsPreferenceFragment.class;

                try {
                    fragment = (Fragment) fragmentClass.newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                fragments.add(fragment);

                getFragmentManager().beginTransaction()
                        .add(R.id.snackbarPosition, fragment, "SettingsFragment")
                        .show(fragment)
                        .commit();
            } else {
                getFragmentManager().beginTransaction()
                        .show(settingsFragment)
                        .commit();
            }

        } else if (id == R.id.nav_sign_off) {
            signOff();
            Intent intent = new Intent(MainActivity.this,
                    LoginActivity.class);
            intent.putExtra("signOff", true);
            startActivityForResult(intent, LOGIN);
            removeFragments(fragments);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Gets the list of fragments.
     *
     * @return the list of fragments
     */
    public List<Fragment> getFragments() { return fragments; }

    /**
     * Hides fragments in the stack.
     *
     * @param fragments the list of fragments to hide
     */
    private void hideFragments(List<Fragment> fragments) {
        for (Fragment fragment : fragments) {
            if (fragment.getTag().equals("ConnectionFragment")  ||
                    fragment.getTag().equals("EditPlcFragment") ||
                    fragment.getTag().equals("EditUserFragment")) {
                getFragmentManager().beginTransaction()
                        .remove(fragment).commit();
                fragments.remove(fragment);
            } else {
                getFragmentManager().beginTransaction()
                        .hide(fragment).commit();
            }
        }
    }

    /**
     * Removes fragments in the stack.
     *
     * @param fragments the list of fragments to remove
     */
    private void removeFragments(List<Fragment> fragments) {
        for (Fragment fragment : fragments) {
            if (fragment != null) {
                getFragmentManager().beginTransaction()
                        .remove(fragment).commit();
            }
        }

        fragments.clear();
    }

    /**
     * Silly method to avoid that "AddUserFragment" and "AddPlcFragment" are
     * still visible if the user changes fragment with the navigation view.
     */
    public void removeAddFragmentsForm() {
        Fragment addUserFragment = getFragmentManager()
                .findFragmentByTag("AddUserFragment");

        Fragment addPlcFragment = getFragmentManager()
                .findFragmentByTag("AddPlcFragment");

        if (addUserFragment != null) {
            getFragmentManager().beginTransaction()
                    .remove(addUserFragment).commit();
        }

        if (addPlcFragment != null) {
            getFragmentManager().beginTransaction()
                    .remove(addPlcFragment).commit();
        }
    }

    /**
     * Set up animations of window fades for entrances and exits of the
     * Activity.
     */
    private void setupWindowAnimations() {
        getWindow().setEnterTransition(new Fade().setDuration(1000));
    }

    /**
     * Disconnects the user from his/her account by schedules the data
     * to be deleted asynchronously.
     */
    public void signOff() {
        if (user.isAdmin()) {
            mNavigationView.getMenu().removeItem(mItemUsers.getItemId());
        }
        sp.edit().clear().apply();
    }
}