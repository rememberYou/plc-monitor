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

package be.heh.plcmonitor.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.text.Html;
import android.transition.Fade;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import be.heh.plcmonitor.ApplicationComponent;
import be.heh.plcmonitor.DaggerApplicationComponent;
import be.heh.plcmonitor.R;
import be.heh.plcmonitor.database.DatabaseHelper;
import be.heh.plcmonitor.database.DatabaseModule;
import be.heh.plcmonitor.model.User;

import com.afollestad.materialdialogs.MaterialDialog;

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
    public static final int INIT = 1;

    @Inject
    DatabaseHelper databaseHelper;

    ApplicationComponent applicationComponent;

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
    private TextView mEmailView;
    private TextView mNameView;
    private MaterialDialog mAboutDialog;

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
        setContentView(R.layout.activity_main);

        applicationComponent = DaggerApplicationComponent.builder()
                .databaseModule(new DatabaseModule(this))
                .build();

        applicationComponent.inject(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerLayout = navigationView.getHeaderView(0);
        mEmailView = headerLayout.findViewById(R.id.tv_email);
        mNameView = headerLayout.findViewById(R.id.tv_name);

        MaterialDialog.Builder mTermsUseBuilder = new MaterialDialog.Builder(this)
                .title(R.string.prompt_about)
                .content(Html.fromHtml(getString(R.string.dialog_about)))
                .positiveText(R.string.action_back);

        mAboutDialog = mTermsUseBuilder.build();

        sp = getSharedPreferences("login", MODE_PRIVATE);
        final boolean logged = sp.getBoolean("logged", false);

        if (logged) {
            user = new User(sp.getString("firstName", ""),
                            sp.getString("lastName", ""),
                            sp.getString("email", ""),
                            sp.getString("password", ""));

            mNameView.setText(getString(R.string.prompt_name,
                                user.getFirstName().toUpperCase(),
                                user.getLastName().toUpperCase()));
            mEmailView.setText(user.getEmail());

            displayMessage(getString(R.string.message_welcome_back, user.getFirstName()));
        } else {
            startActivityForResult(new Intent(MainActivity.this,
                    LoginActivity.class), INIT);
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
        if (requestCode == INIT && resultCode == RESULT_OK) {
            user = data.getParcelableExtra("user");
            mNameView.setText(getString(R.string.prompt_name,
                    user.getFirstName(), user.getLastName()));
            mEmailView.setText(user.getEmail());

            displayMessage(getString(R.string.message_welcome, user.getFirstName()));
        }
    }

    /**
     * Closes the navigation menu if it is opened, when the Activity has
     * detected the user's press of the back key.
     */
    @Override
    public void onBackPressed() {
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
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_connect) {

        } else if (id == R.id.nav_overview) {

        } else if (id == R.id.nav_station_info) {

        } else if (id == R.id.nav_trend_view) {

        } else if (id == R.id.about) {
            mAboutDialog.show();
        } else if (id == R.id.nav_settings) {
            
        } else if (id == R.id.nav_sign_off) {
            signOff();
            startActivity(new Intent(MainActivity.this,
                    LoginActivity.class));
            displayMessage(getString(R.string.message_disconnect));
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
        sp.edit().clear().apply();
    }
}