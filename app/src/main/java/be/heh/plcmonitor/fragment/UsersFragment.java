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

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import be.heh.plcmonitor.ApplicationComponent;
import be.heh.plcmonitor.DaggerApplicationComponent;
import be.heh.plcmonitor.R;
import be.heh.plcmonitor.adapter.UsersAdapter;
import be.heh.plcmonitor.dao.UserDaoImpl;
import be.heh.plcmonitor.database.DatabaseModule;
import be.heh.plcmonitor.helper.Message;
import be.heh.plcmonitor.model.User;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static android.support.design.widget.Snackbar.LENGTH_LONG;

/**
 * Represents the overview screen to all registered users.
 *
 * @author Terencio Agozzino
 */
public class UsersFragment extends Fragment {

    /**
     * Useful for debug to identify which class has logged.
     */
    private static final String TAG = UsersFragment.class.getSimpleName();

    /**
     * Request codes.
     */
    public static final int USER_CREATED = 1;
    public static final int USER_EDITED = 2;

    /**
     * Injections.
     */
    @Inject
    UserDaoImpl userDaoImpl;

    /**
     * Retrieves and hold the contents of the user from his past session.
     */
    private SharedPreferences sp;

    /**
     * Registered users.
     */
    private List<User> mUsers;

    /**
     * Users Adapter.
     */
    private UsersAdapter mUsersAdapter;

    /**
     * UI references.
     */
    private NavigationView mNavigationView;
    private RecyclerView mUsersView;
    private TextView mEmailView;
    private TextView mNameView;
    private View mCoordinatorLayoutView;

    /**
     * Called when the overview fragment is instantiate.
     *
     * @param inflater the LayoutInflater object that can be used to inflate
     *                 any views in the fragment
     * @param container the parent view that the fragment's UI should be attached to
     * @param savedInstanceState saved state given so that the fragment can be
     *                           re-constructed
     * @return the View for the fragment's UI, or null
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users,
                container, false);

        ApplicationComponent applicationComponent =
                DaggerApplicationComponent.builder()
                        .databaseModule(new DatabaseModule(getActivity()
                                .getApplication()))
                        .build();
        applicationComponent.inject(this);

        mNavigationView = getActivity().findViewById(R.id.nav_view);
        View headerLayout = mNavigationView.getHeaderView(0);

        mEmailView = headerLayout.findViewById(R.id.tv_email);
        mNameView = headerLayout.findViewById(R.id.tv_name);

        FloatingActionButton mBtnAdd = view.findViewById(R.id.fab);
        mBtnAdd.setOnClickListener(v -> {
            AddUserFragment addUserFragment = new AddUserFragment();
            addUserFragment.setTargetFragment(UsersFragment.this, USER_CREATED);

            // In order to avoid making a new expensive query to the database,
            // when adding the user, this fragment is just hidden during this
            // time.
            getFragmentManager().beginTransaction()
                    .hide(this)
                    .add(R.id.snackbarPosition, addUserFragment, "AddUserFragment")
                    .addToBackStack(null)
                    .commit();
        });

        sp = getActivity().getSharedPreferences("login", MODE_PRIVATE);

        mUsers = new ArrayList<>();

        mCoordinatorLayoutView = view.findViewById(R.id.cl_users);

        mUsersView = view.findViewById(R.id.rv_users);
        mUsersView.addOnScrollListener(new RecyclerView.OnScrollListener()  {

            /**
             * Callback method to be invoked when the RecyclerView has been
             * scrolled.
             *
             * @param recyclerView the RecycleView
             * @param dx delta variation on the x-axis
             * @param dy delta variation on the y-axis
             */
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) {
                    // Scroll Down
                    if (mBtnAdd.isShown()) {
                        mBtnAdd.hide();
                    }
                } else if (dy < 0) {
                    // Scroll Up
                    if (!mBtnAdd.isShown()) {
                        mBtnAdd.show();
                    }
                }
            }
        });

        ProgressDialog progressBar = new ProgressDialog(getActivity());
        progressBar.setMessage("Retrieves list of users...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressBar.setProgress(0);

        // Probably not the best way...
        int total = userDaoImpl.getAll().size();
        progressBar.setMax(total);

        Observable.fromIterable(userDaoImpl.getAll())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<User>() {

                    /**
                     * Provides the UsersFragment with the means of
                     * cancelling (disposing) the connection (channel)
                     * with the Observable in both synchronous (from within
                     * Observer.onNext(Object)) and asynchronous manner.
                     *
                     * @param disposable the disposable resource.
                     */
                    @Override
                    public void onSubscribe(Disposable disposable) {
                        progressBar.show();
                    }

                    /**
                     * Provides UsersFragment with a new user to observe.
                     *
                     * @param user the retrieved user
                     */
                    @Override
                    public void onNext(User user) {
                        mUsers.add(user);
                        progressBar.setProgress(mUsers.size() / total);
                    }

                    /**
                     * Notifies UsersFragment that the Observable has
                     * experienced an error condition when retrieving the list
                     * of users.
                     *
                     * @param e the exception, not null
                     */
                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Unable to retrieves users", e);
                        progressBar.hide();
                    }

                    /**
                     * Notifies UsersFragment that the Observable has finished
                     * retrieving the list of users.
                     */
                    @Override
                    public void onComplete() {
                        mUsersView.setLayoutManager(new LinearLayoutManager(getActivity()));
                        mUsersAdapter = new UsersAdapter(getActivity(), mUsers,
                                mCoordinatorLayoutView);
                        mUsersView.setAdapter(mUsersAdapter);
                        progressBar.hide();
                    }
                });

        return view;
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
     * Specifies if an account has been created.
     *
     * @param requestCode the integer request code originally supplied to
     *                    startActivityForResult(), allowing to identify who
     *                    this result came from
     * @param resultCode the integer result code returned by AddUserFragment
     * @param data an Intent, which can return user data to the UsersFragment
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == USER_CREATED && resultCode == RESULT_OK) {
            User user = data.getParcelableExtra("user");
            mUsersAdapter.addUser(user);

            // After adding the user, make visible the fragment and
            // display a message.
            getFragmentManager().beginTransaction().show(this).commit();
            Message.display(mCoordinatorLayoutView,
                    Html.fromHtml(getString(R.string.message_successful_user_creation)),
                    LENGTH_LONG);
        } else if (requestCode == USER_EDITED && resultCode == RESULT_OK) {
            int position = data.getIntExtra("user_position", 0);

            User updatedUser = data.getParcelableExtra("user");

            if (updatedUser.equals(userDaoImpl.getUserByEmail(
                    sp.getString("email", "")))) {

                updateFirstName(updatedUser, updatedUser.getFirstName());
                updateLastName(updatedUser, updatedUser.getLastName());
                updateEmail(updatedUser, updatedUser.getEmail());
            }

            mUsersAdapter.getUsers().set(position, updatedUser);
            mUsersAdapter.notifyItemChanged(position);
        }
    }
}