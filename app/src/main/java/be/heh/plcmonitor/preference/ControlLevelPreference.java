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

package be.heh.plcmonitor.preference;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import SimaticS7.S7;
import SimaticS7.S7OrderCode;
import be.heh.plcmonitor.ApplicationComponent;
import be.heh.plcmonitor.DaggerApplicationComponent;
import be.heh.plcmonitor.R;
import be.heh.plcmonitor.dao.PlcDaoImpl;
import be.heh.plcmonitor.database.DatabaseModule;
import be.heh.plcmonitor.model.DataBlock;
import be.heh.plcmonitor.model.Plc;
import be.heh.plcmonitor.plc.ControlLevel;
import be.heh.plcmonitor.s7.PlcConnection;

/**
 * Represents the general and account settings screen of the user.
 *
 * @author Terencio Agozzino
 */
public class ControlLevelPreference extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * Message codes.
     */
    private static final int MESSAGE_PRE_EXECUTE = 1;
    private static final int MESSAGE_PROGRESS_UPDATE = 2;
    private static final int MESSAGE_POST_EXECUTE = 3;

    /**
     * Injections.
     */
    @Inject
    PlcDaoImpl plcDaoImpl;

    private byte[] datasPLC = new byte[512];


    public static AtomicBoolean isRunning = new AtomicBoolean(false);
    public static Thread readThread;
    public static PlcConnection plcConnection;

    private ControlLevel controlLevel;
    private DataBlock dataBlock;

    /**
     * UI references.
     */
    private Preference mCpuCodePreference;
    private Preference mStatusPreference;

    private Preference mBtnConnectionPreference;

    private SwitchPreference mValve1Preference;
    private SwitchPreference mValve2Preference;
    private SwitchPreference mValve3Preference;
    private SwitchPreference mValve4Preference;
    private SwitchPreference mManualPreference;
    private SwitchPreference mRemotePreference;

    private Preference mWaterLevelPreference;
    private Preference mSetPointPreference;
    private Preference mManualValuePreference;
    private Preference mControlWordPreference;

    /**
     * Called during onCreate(Bundle) to supply the preferences for this
     * fragment.
     *
     * @param savedInstanceState saved state given so that the fragment can be
     *                           re-constructed
     * @param rootKey fragment should be rooted at the PreferenceScreen with
     *                this key
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.regulation);

        ApplicationComponent applicationComponent =
                DaggerApplicationComponent.builder()
                        .databaseModule(new DatabaseModule(getActivity()
                                .getApplication()))
                        .build();
        applicationComponent.inject(this);

        mCpuCodePreference = findPreference("pref_cpu_code");

        mStatusPreference = findPreference("pref_status");

        mValve1Preference = (SwitchPreference) findPreference("switch_valve1");
        mValve2Preference = (SwitchPreference) findPreference("switch_valve2");
        mValve3Preference = (SwitchPreference) findPreference("switch_valve3");
        mValve4Preference = (SwitchPreference) findPreference("switch_valve4");

        mManualPreference = (SwitchPreference) findPreference("switch_manual");

        mRemotePreference = (SwitchPreference) findPreference("switch_remote");

        mWaterLevelPreference = findPreference("pref_water_level");
        mSetPointPreference = findPreference("pref_setpoint");
        mManualValuePreference = findPreference("pref_manual");
        mControlWordPreference = findPreference("pref_control_word");

        Plc plc = plcDaoImpl.getPlcByName("Control Level");
        dataBlock = plc.getDataBlock();

        plcConnection = new PlcConnection(plc, S7.S7_BASIC);
        controlLevel = new ControlLevel(dataBlock.getData());

        readThread = new Thread(new AutomateS7());
        start();

        mBtnConnectionPreference = findPreference("pref_connection");
        mBtnConnectionPreference.setOnPreferenceClickListener(preference -> {
            connection();
            return true;
        });
    }

    /**
     * Registers a callback to be invoked when a change happens to a
     * preference when the fragment will start interacting with the user.
     */
    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager()
                .getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Unregisters a previous callback when the system is about to start
     * resuming the SettingsPreferenceFragment.
     */
    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager()
                .getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Called when a shared preference is changed, added, or removed. This may
     * be called even if a preference is set to its existing value.
     *
     * @param sharedPreferences the SharedPreferences that received the change
     * @param key the key of the preference that was changed, added, or removed
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        Preference preference = findPreference(key);

        if (preference instanceof SwitchPreference) { }

        if (preference instanceof EditTextPreference) { }
    }

    /**
     * Connects or disconnects to the PLC.
     */
    public void connection() {
        if (isRunning.get()) {
            stop();

            mStatusPreference.setSummary("Disconnected");
            mBtnConnectionPreference.setTitle("CONNECTED");
        } else {
            start();

            mStatusPreference.setSummary("Connected");
            mBtnConnectionPreference.setTitle("DISCONNECTED");
        }
    }

    /**
     * Called before the reading thread.
     *
     * @param cpuCode the CPU code of the PLC
     */
    private void downloadOnPreExecute(int cpuCode) {
        mStatusPreference.setSummary("Connected");
        mCpuCodePreference.setSummary(String.valueOf(cpuCode));
    }

    /**
     * Called on the reading thread.
     *
     * @param progress the progress
     */
    private void downloadOnProgressUpdate (int progress) { updateAll(); }

    /**
     * Called after the reading thread.
     */
    private void downloadOnPostExecute() { }

    /**
     * Gets the CPU code of a PLC.
     *
     * @return the CPU code if the connection to the PLC was successful and the
     *         code could be read; -1 otherwise.
     */
    public int getCpuCode() {
        S7OrderCode s7OrderCode = new S7OrderCode();
        int resOrderCode = plcConnection.getS7Client().GetOrderCode(s7OrderCode);

        int cpu = -1;

        if (resOrderCode == 0) {
            cpu = Integer.valueOf(s7OrderCode.Code().substring(5, 8));
        }

        return cpu;
    }

    /**
     * Starts the reading thread if not already thrown.
     */
    public void start() {
        if (!readThread.isAlive()) {
            readThread.start();
            isRunning.set(true);
        }
    }

    /**
     * Stops the reading thread if not already stopped.
     */
    public void stop() {
        isRunning.set(false);
        readThread.interrupt();
        plcConnection.close();
    }

    @SuppressLint("HandlerLeak")
    private Handler readingHandler = new Handler() {

        /**
         * Subclasses must implement this to receive messages.
         *
         * @param msg the message whose name is being queried
         */
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MESSAGE_PRE_EXECUTE:
                    downloadOnPreExecute(msg.arg1);
                    break;
                case MESSAGE_PROGRESS_UPDATE:
                    downloadOnProgressUpdate(msg.arg1);
                    break;
                case MESSAGE_POST_EXECUTE:
                    downloadOnPostExecute();
                    break;
                default:
                    break;
            }
        }
    };

    private class AutomateS7 implements Runnable {
        @Override
        public void run() {
            try {
                plcConnection.open();

                sendPreExecuteMessage(getCpuCode());

                while (isRunning.get()) {
                    int retInfo = plcConnection.getS7Client().ReadArea(S7.S7AreaDB,
                            dataBlock.getDbNumber(), dataBlock.getOffset(),
                            dataBlock.getAmount(), dataBlock.getData());

                    if (retInfo == 0) {
                        sendProgressMessage(S7.GetWordAt(dataBlock.getData(), 0));
                    }
                }
                sendPostExecuteMessage();
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Sends post-execute message.
     */
    private void sendPostExecuteMessage(){
        Message postExecuteMsg = new Message();
        postExecuteMsg.what = MESSAGE_POST_EXECUTE;
        readingHandler.sendMessage(postExecuteMsg);
    }

    /**
     * Sends pre-execute message.
     *
     * @param arg1
     */
    private void sendPreExecuteMessage(int arg1) {
        Message preExecuteMsg = new Message();
        preExecuteMsg.what = MESSAGE_PRE_EXECUTE;
        preExecuteMsg.arg1 = arg1;
        readingHandler.sendMessage(preExecuteMsg);
    }

    /**
     * Sends progress execute message.
     *
     * @param arg1
     */
    private void sendProgressMessage(int arg1) {
        Message progressMsg = new Message();
        progressMsg.what = MESSAGE_PROGRESS_UPDATE;
        progressMsg.arg1 = arg1;
        readingHandler.sendMessage(progressMsg);
    }

    /**
     * Updates the manual preference.
     */
    private void updateManual() {
        if (controlLevel.isManual()) {
            mManualPreference.setChecked(true);
        } else {
            mManualPreference.setChecked(false);
        }
    }

    /**
     * Updates the remote preference.
     */
    private void updateRemotelyControllable() {
        if (controlLevel.isRemotelyControllable()) {
            mRemotePreference.setChecked(true);
        } else {
            mRemotePreference.setChecked(false);
        }
    }

    /**
     * Updates the valves preference.
     */
    private void updateValves() {
        if (controlLevel.isValve1Open()) {
            mValve1Preference.setChecked(true);
        } else {
            mValve1Preference.setChecked(false);
        }

        if (controlLevel.isValve2Open()) {
            mValve2Preference.setChecked(true);
        } else {
            mValve2Preference.setChecked(false);
        }

        if (controlLevel.isValve3Open()) {
            mValve3Preference.setChecked(true);
        } else {
            mValve3Preference.setChecked(false);
        }

        if (controlLevel.isValve4Open()) {
            mValve4Preference.setChecked(true);
        } else {
            mValve4Preference.setChecked(false);
        }
    }

    public void updateWaterLevel() {
        mWaterLevelPreference.setSummary("Value: " + controlLevel.getWaterLevel());
    }

    public void updateSetPoint() {
        mSetPointPreference.setSummary("Value: " + controlLevel.getSetPoint());
    }

    public void updateManualValue() {
        mManualValuePreference.setSummary("Value: " + controlLevel.getManualValue());
    }

    public void updateControlWord() {
        mControlWordPreference.setSummary("Value: " + controlLevel.getValveControlWord());
    }

    /**
     * Update all the preferences for the industrial process.
     */
    public void updateAll() {
        updateWaterLevel();
        updateSetPoint();
        updateManualValue();
        updateControlWord();
        updateManual();
        updateRemotelyControllable();
        updateValves();
    }
}