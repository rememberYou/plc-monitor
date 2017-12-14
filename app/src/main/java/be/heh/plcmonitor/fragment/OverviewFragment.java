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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import be.heh.plcmonitor.R;
import be.heh.plcmonitor.adapter.PlcsAdapter;
import be.heh.plcmonitor.model.Plc;
import be.heh.plcmonitor.utils.Validator;
import be.heh.plcmonitor.widget.Message;

import java.util.ArrayList;

import static android.support.design.widget.Snackbar.LENGTH_LONG;
import static org.apache.commons.lang3.StringUtils.isNumeric;

/**
 * Represents the overview screen to all saved PLC.
 *
 * @author Terencio Agozzino
 */
public class OverviewFragment extends Fragment {

    /**
     * Saved PLCs by the user.
     */
    private ArrayList<Plc> plcs;

    /**
     * UI references.
     */
    private EditText mPlcName;
    private EditText mPlcIp;
    private EditText mPlcRack;
    private EditText mPlcSlot;
    private MaterialDialog mPlcDialog;
    private RecyclerView mPlcsView;
    private View coordinatorLayoutView;

    /**
     * Called when the overview fragment is instantiate.
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
        // TODO: to be deleted.
        initializeData();

        View view = inflater.inflate(R.layout.fragment_overview,
                container, false);

        mPlcsView = view.findViewById(R.id.rv_plcs);

        coordinatorLayoutView = view.findViewById(R.id.snackbarPosition);

        mPlcsView.setAdapter(new PlcsAdapter(getActivity(), plcs));
        mPlcsView.setLayoutManager(new LinearLayoutManager(getActivity()));

        View mView = inflater.inflate(R.layout.dialog_add_plc, null);
        mPlcName = mView.findViewById(R.id.txtPlcName);
        mPlcName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptAddPlc();
                    return true;
                }
                return false;
            }
        });

        mPlcIp = mView.findViewById(R.id.txtPlcIp);
        mPlcIp.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptAddPlc();
                    return true;
                }
                return false;
            }
        });

        mPlcRack = mView.findViewById(R.id.txtPlcRack);
        mPlcRack.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptAddPlc();
                    return true;
                }
                return false;
            }
        });

        mPlcSlot = mView.findViewById(R.id.txtPlcSlot);
        mPlcSlot.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptAddPlc();
                    return true;
                }
                return false;
            }
        });

        return view;
    }

    // TODO: to be completed.
    private void buildAlertDialog() {
       /* View view = getLayoutInflater().inflate(R.layout.dialog_add_plc, null);
        Button mBtnAdd = view.findViewById(R.id.btn_add);
        mBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptAddPlc();
            }
        }); */

        // final AlertDialog alertDialog = new AlertDialog.Builder(this)
        //       .setView(R.layout.dialog_add_plc)

        //.setPositiveButton("Add PLC", null)
        //.setNegativeButton("Cancel", null)
        //              .create();
        //alertDialog.show();
        mPlcDialog = new MaterialDialog.Builder(getActivity())
                .title("Add PLC")
                .customView(R.layout.dialog_add_plc, true)
                .positiveText("Add")
                .autoDismiss(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        attemptAddPlc();
                    }
                })
                .negativeText("Cancel")
                .show();
    }

    // TODO: to be deleted.
    private void initializeData() {
        plcs = new ArrayList<>();
        plcs.add(new Plc("PLC 1","10.1.1.1", 1, 2));
        plcs.add(new Plc("PLC 2","10.1.1.2", 1, 2));
        plcs.add(new Plc("PLC 3","10.1.1.3", 1, 2));
        plcs.add(new Plc("PLC 4","10.1.1.4", 1, 2));
        plcs.add(new Plc("PLC 1","10.1.1.1", 1, 2));
        plcs.add(new Plc("PLC 2","10.1.1.2", 1, 2));
        plcs.add(new Plc("PLC 3","10.1.1.3", 1, 2));
        plcs.add(new Plc("PLC 4","10.1.1.4", 1, 2));
        plcs.add(new Plc("PLC 1","10.1.1.1", 1, 2));
        plcs.add(new Plc("PLC 2","10.1.1.2", 1, 2));
        plcs.add(new Plc("PLC 3","10.1.1.3", 1, 2));
        plcs.add(new Plc("PLC 4","10.1.1.4", 1, 2));
        plcs.add(new Plc("PLC 1","10.1.1.1", 1, 2));
        plcs.add(new Plc("PLC 2","10.1.1.2", 1, 2));
        plcs.add(new Plc("PLC 3","10.1.1.3", 1, 2));
        plcs.add(new Plc("PLC 4","10.1.1.4", 1, 2));
        plcs.add(new Plc("PLC 1","10.1.1.1", 1, 2));
        plcs.add(new Plc("PLC 2","10.1.1.2", 1, 2));
        plcs.add(new Plc("PLC 3","10.1.1.3", 1, 2));
        plcs.add(new Plc("PLC 4","10.1.1.4", 1, 2));
        plcs.add(new Plc("PLC 1","10.1.1.1", 1, 2));
        plcs.add(new Plc("PLC 2","10.1.1.2", 1, 2));
        plcs.add(new Plc("PLC 3","10.1.1.3", 1, 2));
        plcs.add(new Plc("PLC 4","10.1.1.4", 1, 2));
        plcs.add(new Plc("PLC 1","10.1.1.1", 1, 2));
        plcs.add(new Plc("PLC 2","10.1.1.2", 1, 2));
        plcs.add(new Plc("PLC 3","10.1.1.3", 1, 2));
        plcs.add(new Plc("PLC 4","10.1.1.4", 1, 2));
    }

    /**
     * Attempts to add a PLC for the account specified by the add form. If there
     * are form errors (invalid name, missing fields, etc.), the errors are
     * presented and no actual add attempt is made.
     */
    private void attemptAddPlc() {
        // Reset errors.
        mPlcName.setError(null);
        mPlcIp.setError(null);
        mPlcRack.setError(null);
        mPlcSlot.setError(null);

        // Store values at the time of the adding PLC attempt.
        String plcName = mPlcName.getText().toString();
        String plcIp = mPlcIp.getText().toString();
        String plcRack = mPlcRack.getText().toString();
        String plcSlot = mPlcSlot.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid PLC name.
        if (TextUtils.isEmpty(plcName)) {
            mPlcName.setError(getString(R.string.error_field_required));
            focusView = mPlcName;
            Message.display(coordinatorLayoutView, "Plc Added", LENGTH_LONG);
            cancel = true;
        } else if (!Validator.isValidName(plcName)) {
            mPlcName.setError(getString(R.string.error_invalid_firstname));
            focusView = mPlcName;
            cancel = true;
        }

        // Check for a valid IP.
        if (TextUtils.isEmpty(plcIp)) {
            mPlcIp.setError(getString(R.string.error_field_required));
            focusView = mPlcIp;
            cancel = true;
        } else if (!Validator.isValidIp(plcIp)) {
            mPlcIp.setError(getString(R.string.error_invalid_firstname));
            focusView = mPlcIp;
            cancel = true;
        }

        // Check for a valid rack.
        if (TextUtils.isEmpty(plcRack)) {
            mPlcRack.setError(getString(R.string.error_field_required));
            focusView = mPlcRack;
            cancel = true;
        } else if (!isNumeric(plcRack)) {
            mPlcRack.setError(getString(R.string.error_invalid_firstname));
            focusView = mPlcRack;
            cancel = true;
        }

        // Check for a valid slot.
        if (TextUtils.isEmpty(plcSlot)) {
            mPlcSlot.setError(getString(R.string.error_field_required));
            focusView = mPlcSlot;
            cancel = true;
        } else if (!isNumeric(plcSlot)) {
            mPlcSlot.setError(getString(R.string.error_invalid_firstname));
            focusView = mPlcSlot;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt register and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user register attempt.
            mPlcDialog.dismiss();
        }
    }
}