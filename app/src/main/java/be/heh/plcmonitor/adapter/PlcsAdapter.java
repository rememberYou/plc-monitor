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

package be.heh.plcmonitor.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import be.heh.plcmonitor.R;
import be.heh.plcmonitor.model.Plc;

import java.util.List;

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
     * List containing the PLCs to display.
     */
    private List<Plc> mPlcs;

    /**
     * Main constructor of the PlcsAdapter class.
     *
     * @param mContext the context of the object
     * @param mPlcs the list of PLCs
     */
    public PlcsAdapter(Context mContext, List<Plc> mPlcs) {
        this.mContext = mContext;
        this.mPlcs = mPlcs;
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
        private TextView mPlcItemIdView;
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

            //mPlcItemIdView = itemView.findViewById(R.id.tv_plc_item_id);
            mPlcItemNameView = itemView.findViewById(R.id.tv_plc_item_name);
            mPlcItemIpView = itemView.findViewById(R.id.tv_plc_item_ip);
            mPlcItemRackView = itemView.findViewById(R.id.tv_plc_item_rack);
            mPlcItemSlotView = itemView.findViewById(R.id.tv_plc_item_slot);
        }
    }

    // Usually involves inflating a layout from XML and returning the holder

    /**
     * Called when RecyclerView needs a new RecyclerView.ViewHolder of the
     * given type to represent an item. Usually involves inflating a layout
     * from XML and returning the holder.
     *
     * @param parent the ViewGroup into which the new View will be added after
     *               it is bound to an adapter position.
     * @param viewType the view type of the new View.
     * @return a new ViewHolder that holds a View of the given view type.
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
     *               contents of the item at the given position in the data set.
     * @param position the position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(PlcViewHolder holder, int position) {
       // holder.mPlcItemIdView.setText("ID: " + String.valueOf(mPlcs.get(position).getId()));
        holder.mPlcItemNameView.setText(mPlcs.get(position).getName());
        holder.mPlcItemIpView.setText("IP: " + mPlcs.get(position).getIp());
        holder.mPlcItemRackView.setText("Rack: " + String.valueOf(mPlcs.get(position).getRack()));
        holder.mPlcItemSlotView.setText("Slot: " + String.valueOf(mPlcs.get(position).getSlot()));
    }

    /**
     * Gets the context object in the RecyclerView.
     *
     * @return the context object in the RecyclerView
     */
    private Context getContext() { return mContext; }

    /**
     * Gets the total number of PLCs in the data set held by the adapter.
     *
     * @return The total number of PLCs in this adapter.
     */
    @Override
    public int getItemCount() { return mPlcs.size(); }
}