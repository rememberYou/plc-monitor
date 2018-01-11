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
import android.os.Bundle;
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

import SimaticS7.S7;
import SimaticS7.S7Client;
import be.heh.plcmonitor.ApplicationComponent;
import be.heh.plcmonitor.DaggerApplicationComponent;
import be.heh.plcmonitor.R;
import be.heh.plcmonitor.activity.MainActivity;
import be.heh.plcmonitor.dao.PlcDaoImpl;
import be.heh.plcmonitor.database.DatabaseModule;
import be.heh.plcmonitor.fragment.EditPlcFragment;
import be.heh.plcmonitor.fragment.PlcsFragment;
import be.heh.plcmonitor.helper.Message;
import be.heh.plcmonitor.model.Plc;
import be.heh.plcmonitor.preference.ControlLevelPreference;
import be.heh.plcmonitor.preference.PillsPreferenceFragment;
import be.heh.plcmonitor.s7.PlcConnection;
import be.heh.plcmonitor.util.Connectivity;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import javax.inject.Inject;

import static android.support.design.widget.Snackbar.LENGTH_LONG;

/**
 * Adapter for binding PLC data.
 * This class takes a PlcViewHolder which gives us access to the PLC views.
 *
 * @author Terencio Agozzino
 */
public class PlcsAdapter extends
        RecyclerView.Adapter<PlcsAdapter.PlcViewHolder> {

    /**
     * Useful for debug to identify which class has logged.
     */
    private static final String TAG = PlcsAdapter.class.getSimpleName();

    /**
     * Context of the object.
     */
    private Context mContext;

    /**
     * Manage the connection with PLC.
     */
    private Connectivity mConnectivity;


    /**
     * List containing the PLCs to display.
     */
    private List<Plc> mPlcs;

    /**
     * Injections.
     */
    @Inject
    PlcDaoImpl plcDaoImpl;

    private PlcConnection plcConnection;

    /**
     * Preventive message for the deletion of a user.
     */
    private boolean msgPreventive;

    /**
     * UI references.
     */
    private View mCoordinatorLayoutView;

    /**
     * Main constructor of the UsersAdapter class.
     *
     * @param mContext the context of the object
     * @param mPlcs the list of users
     * @param mCoordinatorLayoutView the layout to display messages
     */
    public PlcsAdapter(Context mContext, List<Plc> mPlcs,
                        View mCoordinatorLayoutView) {
        this.mContext = mContext;
        this.mPlcs = mPlcs;

        ApplicationComponent applicationComponent =
                DaggerApplicationComponent.builder()
                        .databaseModule(new DatabaseModule(mContext))
                        .build();
        applicationComponent.inject(this);

        mConnectivity = new Connectivity(mContext);
        this.mCoordinatorLayoutView = mCoordinatorLayoutView;

        msgPreventive = true;
    }

    /**
     * Describes a PLC item view and metadata about its place within the
     * RecyclerView. Used to cache the views within the item layout for fast
     * access.
     */
    public class PlcViewHolder extends RecyclerView.ViewHolder {

        /**
         * UI references to render the rows in each view.
         */
        private ImageButton mImageButtonView;
        private TextView mPlcItemPositionView;
        private TextView mPlcItemIpView;
        private TextView mPlcItemNameView;
        private TextView mPlcItemRackView;
        private TextView mPlcItemSlotView;

        /**
         * Main constructor of the PLC view holder that accepts the entire item
         * row.
         *
         * @param itemView the PLC item view
         */
        public PlcViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can
            // be used to access the context from any PlcViewHolder instance.
            super(itemView);

            mImageButtonView = itemView.findViewById(R.id.ib_plc);
            mPlcItemPositionView = itemView.findViewById(R.id.tv_plc_item_position);
            mPlcItemNameView = itemView.findViewById(R.id.tv_plc_item_name);
            mPlcItemIpView = itemView.findViewById(R.id.tv_plc_item_ip);
            mPlcItemRackView = itemView.findViewById(R.id.tv_plc_item_rack);
            mPlcItemSlotView = itemView.findViewById(R.id.tv_plc_item_slot);
        }
    }

    /**
     * Called when RecyclerView needs a new RecyclerView.ViewHolder of the
     * given type to represent an item. Usually involves inflating a layout
     * from XML and returning the holder.
     *
     * @param parent the ViewGroup into which the new View will be added after
     *               it is bound to an adapter position.
     * @param viewType the view type of the new View
     * @return a new ViewHolder that holds a View of the given view type
     */
    @Override
    public PlcViewHolder onCreateViewHolder(ViewGroup parent,
                                            int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plc, parent, false);

        return new PlcViewHolder(view);
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
    public void onBindViewHolder(PlcViewHolder holder, int position) {
        int id = mPlcs.get(position).getId();
        holder.mPlcItemPositionView.setText(String.valueOf(position + 1));

        holder.mPlcItemNameView.setText(mPlcs.get(position).getName());

        holder.mPlcItemIpView.setText(mContext.getResources().getString(
                R.string.prompt_adapter_ip, mPlcs.get(position).getIp()));

        holder.mPlcItemRackView.setText(mContext.getResources().getString(
                R.string.prompt_adapter_rack, mPlcs.get(position).getRack()));

        holder.mPlcItemSlotView.setText(mContext.getResources().getString(
                R.string.prompt_adapter_slot, mPlcs.get(position).getSlot()));

        holder.mImageButtonView.setOnClickListener(view -> {
            try {
                PopupMenu popUp = new PopupMenu(mContext, view);
                MenuInflater inflater = popUp.getMenuInflater();
                inflater.inflate(R.menu.plc_item_actions, popUp.getMenu());

                popUp.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.plc_connect:
                            String plcName = mPlcs.get(position).getName();

                            if (plcName.equals("Conditioning Pills")) {
                                if (mConnectivity.isConnected()) {
                                        PillsPreferenceFragment pillsFragment =
                                                new PillsPreferenceFragment();
                                        ((MainActivity) mContext).getFragments()
                                                .add(pillsFragment);

                                        Fragment fragment = ((Activity) mContext).getFragmentManager()
                                                .findFragmentByTag("PlcsFragment");

                                        ((Activity) mContext).getFragmentManager().beginTransaction()
                                                .add(R.id.snackbarPosition, pillsFragment,
                                                        "PillsPreferenceFragment")
                                                .hide(fragment)
                                                .addToBackStack(null)
                                                .commit();
                                } else {
                                    Message.display(mCoordinatorLayoutView,
                                            Html.fromHtml(mContext.getResources()
                                                    .getString(R.string.message_missing_network)),
                                            LENGTH_LONG);
                                }
                            } else if (plcName.equals("Control Level")) {
                                if (mConnectivity.isConnected()) {
                                        ControlLevelPreference regulationFragment =
                                                new ControlLevelPreference();
                                        ((MainActivity) mContext).getFragments()
                                                .add(regulationFragment);

                                        Fragment fragment = ((Activity) mContext).getFragmentManager()
                                                .findFragmentByTag("PlcsFragment");

                                        ((Activity) mContext).getFragmentManager().beginTransaction()
                                                .add(R.id.snackbarPosition, regulationFragment,
                                                        "ControlLevelPreference")
                                                .hide(fragment)
                                                .addToBackStack(null)
                                                .commit();
                                } else {
                                    Message.display(mCoordinatorLayoutView,
                                            Html.fromHtml(mContext.getResources()
                                                    .getString(R.string.message_missing_network)),
                                            LENGTH_LONG);
                                }
                            } else {
                                Message.display(mCoordinatorLayoutView,
                                        mContext.getResources()
                                                .getString((R.string.message_no_connection)),
                                        LENGTH_LONG);
                            }
                            break;

                        case R.id.plc_edit:
                            switchEditFragment(id, position);
                            break;
                        case R.id.plc_delete:
                            Plc plc = plcDaoImpl.get(id);

                            if (msgPreventive) {
                                deletePreventionDialog(plc, position).show();
                            } else {
                                deletePlc(plc, position);
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
     * Gets the total number of PLCs in the data set held by the adapter.
     *
     * @return The total number of PLCs in this adapter
     */
    @Override
    public int getItemCount() { return mPlcs.size(); }

    /**
     * Adds a PLC to the list and notify any registered observers that the
     * list has been changed.
     *
     * @param plc the PLC to add to the list
     */
    public void addPlc(Plc plc) {
        mPlcs.add(plc);
        notifyDataSetChanged();
    }

    /**
     * Deletes the PLC when possible by returning a message to indicate that
     * the PLC has been deleted.
     *
     * @param plc the PLC to delete
     * @param position the PLC's position in the list
     */
    private void deletePlc(Plc plc, int position) {
        plcDaoImpl.delete(plc);
        removeAt(position);
    }

    /**
     * Constructs a prevention dialog box when deleting a PLC.
     *
     * @param plc the PLC to delete
     * @param position the PLC's position in the list
     * @return a new prevention dialog box
     */
    private MaterialDialog.Builder deletePreventionDialog(Plc plc, int position) {
        return new MaterialDialog.Builder(mContext)
                .title("Prevention Message")
                .content(R.string.dialog_plc_delete)
                .negativeText("Cancel")
                .positiveText("Agree")
                .onPositive((dialog, which) -> deletePlc(plc, position));
    }

    /**
     * Deletes an item from the list from a given position and notify any
     * registered observers that the item previously located at position has
     * been removed from the data set.
     *
     * @param position the given position of the PLC to be deleted
     */
    public void removeAt(int position) {
        mPlcs.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mPlcs.size());
    }

    /**
     * Replaces the old PLC list with a new one and notify any
     * registered observers that the list has been changed.
     *
     * @param plcs the new PLC list
     */
    public void setPlcs(List<Plc> plcs) {
        mPlcs.clear();
        mPlcs.addAll(plcs);
        notifyDataSetChanged();
    }

    /**
     * Gets the list of registered PLCs.
     *
     * @return the list of registered PLCs
     */
    public List<Plc> getPlcs() {
        return mPlcs;
    }

    /**
     * Replaces the existing fragment that has been added to a container with
     * EditFragment by sending it the identifier of the selected user.
     *
     * @param id the identifier of the user
     */
    private void switchEditFragment(int id, int position) {
        EditPlcFragment editPlcFragment = new EditPlcFragment();
        Fragment fragment = ((Activity) mContext).getFragmentManager()
                .findFragmentByTag("PlcsFragment");
        editPlcFragment.setTargetFragment(fragment, PlcsFragment.PLC_EDITED);

        Bundle bundle = new Bundle();
        bundle.putInt("id", id);
        bundle.putInt("position", position);
        editPlcFragment.setArguments(bundle);

        ((MainActivity) mContext).getFragments().add(editPlcFragment);
        ((Activity) mContext).getFragmentManager().beginTransaction()
                .add(R.id.snackbarPosition, editPlcFragment, "EditPlcFragment")
                .hide(fragment)
                .addToBackStack(null)
                .commit();
    }
}