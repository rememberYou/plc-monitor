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

package be.heh.plcmonitor.adapter;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import be.heh.plcmonitor.ApplicationComponent;
import be.heh.plcmonitor.DaggerApplicationComponent;
import be.heh.plcmonitor.R;
import be.heh.plcmonitor.activity.MainActivity;
import be.heh.plcmonitor.dao.UserDaoImpl;
import be.heh.plcmonitor.database.DatabaseModule;
import be.heh.plcmonitor.fragment.EditUserFragment;
import be.heh.plcmonitor.fragment.UsersFragment;
import be.heh.plcmonitor.helper.Message;
import be.heh.plcmonitor.model.User;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import javax.inject.Inject;

import static android.content.Context.MODE_PRIVATE;
import static android.support.design.widget.Snackbar.LENGTH_LONG;

/**
 * Adapter for binding user data.
 * This class takes a PlcViewHolder which gives us access to the user views.
 *
 * @author Terencio Agozzino
 */
public class UsersAdapter extends
        RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    /**
     * Useful for debug to identify which class has logged.
     */
    private static final String TAG = UsersAdapter.class.getSimpleName();

    /**
     * Context of the object.
     */
    private Context mContext;

    /**
     * List containing the users to display.
     */
    private List<User> mUsers;

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
     * Preventive message for the deletion of a user.
     */
    private boolean msgPreventive;

    /**
     * UI references.
     */
    private NavigationView mNavigationView;
    private TextView mEmailView;
    private TextView mNameView;
    private View mCoordinatorLayout;

    /**
     * Main constructor of the UsersAdapter class.
     *
     * @param mContext the context of the object
     * @param mUsers the list of users
     * @param mCoordinatorLayout the layout to display messages
     */
    public UsersAdapter(Context mContext, List<User> mUsers,
                        View mCoordinatorLayout) {
        this.mContext = mContext;
        this.mUsers = mUsers;

        ApplicationComponent applicationComponent =
                DaggerApplicationComponent.builder()
                        .databaseModule(new DatabaseModule(mContext))
                        .build();
        applicationComponent.inject(this);

        msgPreventive = true;

        sp = mContext.getSharedPreferences("login", MODE_PRIVATE);

        this.mCoordinatorLayout = mCoordinatorLayout;
        mNavigationView = ((Activity) mContext).findViewById(R.id.nav_view);

        View headerLayout = mNavigationView.getHeaderView(0);

        mEmailView = headerLayout.findViewById(R.id.tv_email);
        mNameView = headerLayout.findViewById(R.id.tv_name);
    }

    /**
     * Describes a user item view and metadata about its place within the
     * RecyclerView. Used to cache the views within the item layout for fast
     * access.
     */
    public class UserViewHolder extends RecyclerView.ViewHolder {

        /**
         * UI references to render the rows in each view.
         */
        private ImageButton mImageButtonView;
        private TextView mUserItemPositionView;
        private TextView mUserItemNameView;
        private TextView mUserItemEmailView;
        private TextView mUserItemProfileView;

        /**
         * Main constructor of the user view holder that accepts the entire item
         * row.
         *
         * @param itemView the user item view
         */
        public UserViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can
            // be used to access the context from any UserViewHolder instance.
            super(itemView);

            mImageButtonView = itemView.findViewById(R.id.ib_user);
            mUserItemPositionView = itemView.findViewById(R.id.tv_user_item_position);
            mUserItemNameView = itemView.findViewById(R.id.tv_user_item_name);
            mUserItemEmailView = itemView.findViewById(R.id.tv_user_item_email);
            mUserItemProfileView = itemView.findViewById(R.id.tv_user_item_profile);
        }
    }

    /**
     * Called when RecyclerView needs a new RecyclerView.ViewHolder of the
     * given type to represent an item. Usually involves inflating a layout
     * from XML and returning the holder.
     *
     * @param parent the ViewGroup into which the new View will be added after
     *               it is bound to an adapter position
     * @param viewType the view type of the new View
     * @return a new ViewHolder that holds a View of the given view type
     */
    @Override
    public UserViewHolder onCreateViewHolder(final ViewGroup parent,
                                            int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);

        return new UserViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method should update the contents of the itemView to reflect the
     * item at the given position. In other word, it involves populating data
     * into the item through holder.
     *
     * @param holder the PlcViewHolder which should be updated to represent the
     *               contents of the item at the given position in the data set
     * @param position the position of the item within the adapter's data set
     */
    @Override
    public void onBindViewHolder(final UserViewHolder holder, int position) {
        int id = mUsers.get(position).getId();
        holder.mUserItemPositionView.setText(String.valueOf(position + 1));

        holder.mUserItemNameView.setText(
                mContext.getString(R.string.prompt_user_name,
                mUsers.get(position).getFirstName(),
                mUsers.get(position).getLastName()));

        holder.mUserItemEmailView.setText(mUsers.get(position).getEmail());

        if (mUsers.get(position).getPermission() == 0) {
            holder.mUserItemProfileView.setText(mContext.getResources()
                    .getString(R.string.prompt_profile_user));
        } else {
            holder.mUserItemProfileView.setText(mContext.getResources()
                    .getString(R.string.prompt_profile_admin));
            holder.mUserItemProfileView.setTextColor(mContext.getResources()
                    .getColor(R.color.primary));
        }

        holder.mImageButtonView.setOnClickListener(view -> {
            try {
                PopupMenu popUp = new PopupMenu(mContext, view);
                MenuInflater inflater = popUp.getMenuInflater();
                inflater.inflate(R.menu.user_item_actions, popUp.getMenu());

                popUp.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.user_edit:
                            switchEditFragment(id, position);
                            break;
                        case R.id.user_delete:
                            User user = userDaoImpl.get(id);

                            if (msgPreventive) {
                                deletePreventionDialog(user, position).show();
                            } else {
                                deleteUser(user, position);
                            }

                            msgPreventive = false;
                            break;
                        default:
                            break;
                    }
                    return true;
                });
                popUp.show();
            } catch (Exception e) {
                Log.e(TAG, "Unable to manage the popup menu", e);
            }
        });
    }

    /**
     * Gets the total number of users in the data set held by the adapter.
     *
     * @return the total number of users in this adapter
     */
    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    /**
     * Adds a user to the list and notify any registered observers that the
     * list has been changed.
     *
     * @param user the user to add to the list
     */
    public void addUser(User user) {
        mUsers.add(user);
        notifyDataSetChanged();
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
     * @param position the user's position in the list
     * @return a new prevention dialog box
     */
    private MaterialDialog.Builder deletePreventionDialog(User user, int position) {
        return new MaterialDialog.Builder(mContext)
                .title("Prevention Message")
                .content(R.string.dialog_user_delete)
                .negativeText("Cancel")
                .positiveText("Agree")
                .onPositive((dialog, which) -> deleteUser(user, position));
    }

    /**
     * Deletes the user when possible by returning a message to indicate that
     * the user has been deleted.
     *
     * @param user the user to delete
     * @param position the user's position in the list
     */
    private void deleteUser(User user, int position) {
        if (canDeleteUser(user)) {
            if (isDeletedUserCurrentUser(user)) {
                mNavigationView.setCheckedItem(R.id.nav_sign_off);
                ((MainActivity) mContext).onNavigationItemSelected(
                        mNavigationView.getMenu().getItem(6));
            }
            userDaoImpl.delete(user);
            removeAt(position);
        } else {
            Message.display(mCoordinatorLayout,
                    Html.fromHtml(mContext.getString(R.string.message_failure_delete)),
                    LENGTH_LONG);
        }
    }

    /**
     * Gets the list of registered users.
     *
     * @return the list of registered users
     */
    public List<User> getUsers() {
        return mUsers;
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
     * Verifies if the user that will be deleted is the currently logged-in user.
     *
     * @param user the user that will be deleted
     * @return true if both users are the same; otherwise false
     */
    private boolean isDeletedUserCurrentUser(User user) {
        User currentUser = userDaoImpl.getUserByEmail(sp.getString("email", ""));
        return user.equals(currentUser);
    }

    /**
     * Deletes an item from the list from a given position and notify any
     * registered observers that the item previously located at position has
     * been removed from the data set.
     *
     * @param position the given position of the user to be deleted
     */
    private void removeAt(int position) {
        mUsers.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mUsers.size());
    }

    /**
     * Replaces the old user list with a new one and notify any
     * registered observers that the list has been changed.
     *
     * @param users the new user list
     */
    public void setUsers(List<User> users) {
        mUsers.clear();
        mUsers.addAll(users);
        notifyDataSetChanged();
    }

    /**
     * Replaces the existing fragment that has been added to a container with
     * EditFragment by sending it the identifier of the selected user.
     *
     * @param id the identifier of the user
     */
    private void switchEditFragment(int id, int position) {
        EditUserFragment editUserFragment = new EditUserFragment();
        Fragment fragment = ((Activity) mContext).getFragmentManager()
                .findFragmentByTag("UsersFragment");
        editUserFragment.setTargetFragment(fragment, UsersFragment.USER_EDITED);

        Bundle bundle = new Bundle();
        bundle.putInt("id", id);
        bundle.putInt("position", position);
        editUserFragment.setArguments(bundle);

        ((MainActivity) mContext).getFragments().add(editUserFragment);
        ((Activity) mContext).getFragmentManager().beginTransaction()
                .add(R.id.snackbarPosition, editUserFragment, "EditUserFragment")
                .hide(fragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Updates the first name of the logged-in user.
     *
     * @param user the logged-in user
     * @param firstName the modified first name
     */
    private void updateFirstName(User user, String firstName) {
        user.setFirstName(firstName);
        mNameView.setText(mContext.getString(R.string.prompt_user_name,
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
        mNameView.setText(mContext.getString(R.string.prompt_user_name,
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
}