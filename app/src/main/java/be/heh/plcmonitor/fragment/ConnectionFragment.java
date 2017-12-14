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

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import be.heh.plcmonitor.R;
import be.heh.plcmonitor.utils.Validator;
import be.heh.plcmonitor.widget.Progress;

import static org.apache.commons.lang3.StringUtils.isNumeric;

/**
 * Represents the connection screen to a PLC.
 *
 * @author Terencio Agozzino
 */
public class ConnectionFragment extends Fragment {

    /**
     * UI references.
     */
    private TextView mPlcIpView;
    private TextView mPlcRackView;
    private TextView mPlcSlotView;
    private View mProgressView;
    private View mConnectionFormView;

    /**
     * Called when the connection fragment is instantiate.
     *
     * @param inflater the LayoutInflater object that can be used to inflate
     *                 any views in the fragment
     * @param container the parent view that the fragment's UI should be attached to
     * @param savedInstanceState saved state given so that the fragment can be
     *                           re-constructed
     * @return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_connection, container, false);
    }

    /**
     * Called immediately after onCreateView(LayoutInflater, ViewGroup, Bundle)
     * has returned, but before any saved state has been restored in to the
     * view.
     *
     * @param view the View returned by onCreateView(LayoutInflater, ViewGroup,
     *             Bundle).
     * @param savedInstanceState saved state given so that the fragment can be
     *                           re-constructed
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        mPlcIpView = view.findViewById(R.id.txtConnectIp);
        mPlcRackView = view.findViewById(R.id.txtConnectRack);
        mPlcSlotView = view.findViewById(R.id.txtConnectSlot);

        Button mConnectionButton = view.findViewById(R.id.btn_connect);
        mConnectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptConnect();
            }
        });

        mConnectionFormView = view.findViewById(R.id.scroll_connection);
        mProgressView = view.findViewById(R.id.pg_connection);
    }

    /**
     * Attempts to connect to the PLC specified by the connection form. If
     * there are form errors (invalid IP, missing fields, etc.), the errors
     * are presented and no actual connection attempt is made.
     */
    private void attemptConnect() {
        // Reset errors.
        mPlcIpView.setError(null);
        mPlcRackView.setError(null);
        mPlcSlotView.setError(null);

        // Store values at the time of the connection attempt.
        String ip = mPlcIpView.getText().toString();
        String rack = mPlcRackView.getText().toString();
        String slot = mPlcSlotView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid IP.
        if (TextUtils.isEmpty(ip)) {
            mPlcIpView.setError(getString(R.string.error_field_required));
            focusView = mPlcIpView;
            cancel = true;
        } else if (!Validator.isValidIp(ip)) {
            mPlcIpView.setError(getString(R.string.error_invalid_ip));
            focusView = mPlcIpView;
            cancel = true;
        }

        // Check for a valid rack.
        if (TextUtils.isEmpty(rack)) {
            mPlcRackView.setError(getString(R.string.error_field_required));
            focusView = mPlcRackView;
            cancel = true;
        } else if (!isNumeric(rack)) {
            mPlcRackView.setError(getString(R.string.error_invalid_rack));
            focusView = mPlcRackView;
            cancel = true;
        }

        // Check for a valid slot.
        if (TextUtils.isEmpty(slot)) {
            mPlcSlotView.setError(getString(R.string.error_field_required));
            focusView = mPlcSlotView;
            cancel = true;
        } else if (!isNumeric(slot)) {
            mPlcSlotView.setError(getString(R.string.error_invalid_slot));
            focusView = mPlcSlotView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt connect and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the PLC connection attempt.
            Progress.show(mConnectionFormView, mProgressView,
                    getResources().getInteger(android.R.integer.config_shortAnimTime),
                    true);
        }
    }
}