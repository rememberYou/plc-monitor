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
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import be.heh.plcmonitor.ApplicationComponent;
import be.heh.plcmonitor.DaggerApplicationComponent;
import be.heh.plcmonitor.R;
import be.heh.plcmonitor.dao.DataBlockDaoImpl;
import be.heh.plcmonitor.dao.PlcDaoImpl;
import be.heh.plcmonitor.database.DatabaseModule;
import be.heh.plcmonitor.model.DataBlock;
import be.heh.plcmonitor.model.Plc;
import be.heh.plcmonitor.util.Validator;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

import static org.apache.commons.lang3.StringUtils.isNumeric;

/**
 * An editing screen that allows you to edit a PLC according to its name,
 * IP address, rack and slot.
 *
 * @author Terencio Agozzino
 */
public class EditPlcFragment extends Fragment {

    /**
     * Useful for debug to identify which class has logged.
     */
    private static final String TAG = EditPlcFragment.class.getSimpleName();

    /**
     * Injections.
     */
    @Inject
    PlcDaoImpl plcDaoImpl;
    @Inject
    DataBlockDaoImpl dataBlockImpl;

    /**
     * The PLC to edit.
     */
    private Plc mPlc;

    /**
     * The Data block to edit.
     */
    private DataBlock mDataBlock;

    /**
     * Keeps the position of the edited PLC in memory to easily update the
     * PlcAdapter.
     */
    private int mPosition;

    /**
     * UI references.
     */
    private TextView mPlcNameView;
    private TextView mPlcIpView;
    private TextView mPlcRackView;
    private TextView mPlcSlotView;

    private TextView mDbAmountView;
    private TextView mDbNumberView;
    private TextView mDbOffsetView;

    /**
     * Called when the overview fragment is instantiate.
     *
     * @param inflater           the LayoutInflater object that can be used to inflate
     *                           any views in the fragment
     * @param container          the parent view that the fragment's UI should be attached to
     * @param savedInstanceState saved state given so that the fragment can be
     *                           re-constructed
     * @return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_plc, container,
                false);

        ApplicationComponent applicationComponent =
                DaggerApplicationComponent.builder()
                        .databaseModule(new DatabaseModule(
                                getActivity().getApplication()))
                        .build();
        applicationComponent.inject(this);

        mPlcNameView = view.findViewById(R.id.txtPlcName);
        mPlcNameView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptEdit();
                return true;
            }
            return false;
        });

        mPlcIpView = view.findViewById(R.id.txtPlcIp);
        mPlcIpView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptEdit();
                return true;
            }
            return false;
        });

        mPlcRackView = view.findViewById(R.id.txtPlcRack);
        mPlcRackView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptEdit();
                return true;
            }
            return false;
        });

        mPlcSlotView = view.findViewById(R.id.txtPlcSlot);
        mPlcSlotView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptEdit();
                return true;
            }
            return false;
        });

        mDbAmountView = view.findViewById(R.id.txtDbAmount);
        mDbAmountView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptEdit();
                return true;
            }
            return false;
        });

        mDbNumberView = view.findViewById(R.id.txtDbNumber);
        mDbNumberView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptEdit();
                return true;
            }
            return false;
        });

        mDbOffsetView = view.findViewById(R.id.txtDbOffset);
        mDbOffsetView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptEdit();
                return true;
            }
            return false;
        });

        Button mEditButton = view.findViewById(R.id.btn_edit);
        // To avoid updating the user before retrieving their information.
        mEditButton.setEnabled(false);
        mEditButton.setOnClickListener(v -> attemptEdit());

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            getPlc(bundle.getInt("id", 0))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableSingleObserver<Plc>() {

                        /**
                         * Notifies the SingleObserver with a single boolean
                         * value and that the Single has finished sending
                         * push-based notifications.
                         */
                        @Override
                        public void onSuccess(Plc plc) {
                            mPlc = plc;
                            mDataBlock = plc.getDataBlock();
                            mPosition = bundle.getInt("position", 0);

                            fillPlcFields(mPlc);
                            fillDataBlockField(mDataBlock);

                            mEditButton.setEnabled(true);
                        }

                        /**
                         * Called once if login registration 'throws' an exception.
                         *
                         * @param e the exception, not null
                         */
                        @Override
                        public void onError(@NonNull Throwable e) {
                            Log.e(TAG, "Unable to get the PLC to edit", e);
                        }
                    });
        }

        return view;
    }

    /**
     * Attempts to edit the PLC specified by the edit form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual edition attempt is made.
     */
    private void attemptEdit() {
        // Reset errors.
        mPlcNameView.setError(null);
        mPlcIpView.setError(null);
        mPlcRackView.setError(null);
        mPlcSlotView.setError(null);

        // Store values at the time of the adding PLC attempt.
        String plcName = mPlcNameView.getText().toString();
        String plcIp = mPlcIpView.getText().toString();
        String plcRack = mPlcRackView.getText().toString();
        String plcSlot = mPlcSlotView.getText().toString();

        String dbAmount = mDbAmountView.getText().toString();
        String dbNumber = mDbNumberView.getText().toString();
        String dbOffset = mDbOffsetView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid PLC name.
        if (TextUtils.isEmpty(plcName)) {
            mPlcNameView.setError(getString(R.string.error_field_required));
            focusView = mPlcNameView;
            cancel = true;
        } else if (!Validator.isValidName(plcName)) {
            mPlcNameView.setError(getString(R.string.error_invalid_first_name));
            focusView = mPlcNameView;
            cancel = true;
        }

        // Check for a valid IP.
        if (TextUtils.isEmpty(plcIp)) {
            mPlcIpView.setError(getString(R.string.error_field_required));
            focusView = mPlcIpView;
            cancel = true;
        } else if (!Validator.isValidIp(plcIp)) {
            mPlcIpView.setError(getString(R.string.error_invalid_ip));
            focusView = mPlcIpView;
            cancel = true;
        }

        // Check for a valid rack.
        if (TextUtils.isEmpty(plcRack)) {
            mPlcRackView.setError(getString(R.string.error_field_required));
            focusView = mPlcRackView;
            cancel = true;

        } else if (!isNumeric(plcRack)) {
            mPlcRackView.setError(getString(R.string.error_invalid_rack));
            focusView = mPlcRackView;
            cancel = true;
        }

        // Check for a valid slot.
        if (TextUtils.isEmpty(plcSlot)) {
            mPlcSlotView.setError(getString(R.string.error_field_required));
            focusView = mPlcSlotView;
            cancel = true;
        } else if (!isNumeric(plcSlot)) {
            mPlcSlotView.setError(getString(R.string.error_invalid_slot));
            focusView = mPlcSlotView;
            cancel = true;
        }

        // Check for a valid data block amount.
        if (TextUtils.isEmpty(dbAmount)) {
            mDbAmountView.setError(getString(R.string.error_field_required));
            focusView = mDbAmountView;
            cancel = true;
        } else if (!isNumeric(dbAmount)) {
            mDbAmountView.setError(getString(R.string.error_invalid_db_amount));
            focusView = mDbAmountView;
            cancel = true;
        }

        // Check for a valid data block number.
        if (TextUtils.isEmpty(dbNumber)) {
            mDbNumberView.setError(getString(R.string.error_field_required));
            focusView = mDbNumberView;
            cancel = true;
        } else if (!isNumeric(dbNumber)) {
            mDbNumberView.setError(getString(R.string.error_invalid_db_number));
            focusView = mDbNumberView;
            cancel = true;
        }

        // Check for a valid data block offset.
        if (TextUtils.isEmpty(dbOffset)) {
            mDbOffsetView.setError(getString(R.string.error_field_required));
            focusView = mDbOffsetView;
            cancel = true;
        } else if (!isNumeric(dbOffset)) {
            mDbOffsetView.setError(getString(R.string.error_invalid_db_offset));
            focusView = mDbOffsetView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt register and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            editPlc(plcName, plcIp, Integer.valueOf(plcRack), Integer.valueOf(plcSlot))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableCompletableObserver() {

                        /**
                         * Called once PLC editing is complete normally.
                         */
                        @Override
                        public void onComplete() {
                            getActivity().getFragmentManager().beginTransaction()
                                    .show(getActivity().getFragmentManager()
                                            .findFragmentByTag("PlcsFragment"))
                                    .commit();

                            getTargetFragment().onActivityResult(
                                    getTargetRequestCode(),
                                    Activity.RESULT_OK,
                                    new Intent()
                                            .putExtra("plc_position", mPosition)
                                            .putExtra("plc", mPlc));
                        }

                        /**
                         * Called once if PLC registration 'throws' an
                         * exception.
                         *
                         * @param e the exception, not null
                         */
                        @Override
                        public void onError(@NonNull Throwable e) {
                            Log.e(TAG, "Unable to edit the PLC", e);
                        }
                    });

            editDataBlock(Integer.valueOf(dbNumber), Integer.valueOf(dbOffset),
                    Integer.valueOf(dbAmount))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableCompletableObserver() {

                        /**
                         * Called once data block editing is complete normally.
                         */
                        @Override
                        public void onComplete() {
                            getFragmentManager().popBackStack();
                        }

                        /**
                         * Called once if data block registration 'throws' an
                         * exception.
                         *
                         * @param e the exception, not null
                         */
                        @Override
                        public void onError(@NonNull Throwable e) {
                            Log.e(TAG, "Unable to edit the PLC", e);
                        }
                    });
        }
    }

    /**
     * Emits either a single successful value for the PLC editing, or an error.
     *
     * @param id the identifier of the PLC
     * @return the new Single instance
     */
    public Single<Plc> getPlc(int id) {
        return Single.create(emitter -> emitter.onSuccess(plcDaoImpl.get(id)));
    }

    /**
     * Edits a data block in the database.
     *
     * @param dbNumber the number of the data block
     * @param offset the offset at the beginning of the data block
     * @param amount the amount of words of the data block
     * @return the new Completable instance
     */
    public Completable editDataBlock(int dbNumber, int offset, int amount) {
        return Completable.create(emitter -> {
            mDataBlock.setDbNumber(dbNumber);
            mDataBlock.setOffset(offset);
            mDataBlock.setAmount(amount);
            dataBlockImpl.update(mDataBlock);

            emitter.onComplete();
        });
    }

    /**
     * Edits a PLC in the database.
     *
     * @param name the name of the PLC
     * @param ip   the IP address of the PLC
     * @param rack the rack of the PLC
     * @param slot the slot of the PLC
     * @return the new Completable instance
     */
    public Completable editPlc(String name, String ip, int rack, int slot) {
        return Completable.create(emitter -> {
            mPlc.setName(name);
            mPlc.setIp(ip);
            mPlc.setRack(rack);
            mPlc.setSlot(slot);
            plcDaoImpl.update(mPlc);

            emitter.onComplete();
        });
    }

    /**
     * Fills in the fields according to the data block information.
     *
     * @param dataBlock the data block
     */
    private void fillDataBlockField(DataBlock dataBlock) {
        mDbAmountView.setText(String.valueOf(dataBlock.getAmount()));
        mDbOffsetView.setText(String.valueOf(dataBlock.getOffset()));
        mDbNumberView.setText(String.valueOf(dataBlock.getDbNumber()));
    }

    /**
     * Fills in the fields according to the PLC information.
     *
     * @param plc the PLC
     */
    private void fillPlcFields(Plc plc) {
        mPlcNameView.setText(plc.getName());
        mPlcIpView.setText(plc.getIp());
        mPlcRackView.setText(String.valueOf(plc.getRack()));
        mPlcSlotView.setText(String.valueOf(plc.getSlot()));
    }
}