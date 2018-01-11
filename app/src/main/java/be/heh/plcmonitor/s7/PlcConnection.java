/*
 * Copyright 2017 the original Terencio Agozzino
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

package be.heh.plcmonitor.s7;

import android.util.Log;

import SimaticS7.S7Client;

import be.heh.plcmonitor.model.Plc;

/**
 * PlcConnection allows to manage the connection to a PLC.
 *
 * @author Terencio Agozzino
 */
public class PlcConnection {

    /**
     * The PLC to manage the connection.
     */
    private Plc mPlc;

    /**
     * SimaticS7 API client to proceed with the connection of the PLC.
     */
    private S7Client mS7Client;

    /**
     * Builds a connection using a PLC and a connection type.
     *
     * @param mPlc the PLC to manage the connection
     * @param connectionType the connection resource type
     */
    public PlcConnection(Plc mPlc, short connectionType) {
        this.mPlc = mPlc;
        mS7Client = new S7Client();
        mS7Client.SetConnectionType(connectionType);
    }

    /**
     * Closes the PLC connection.
     */
    public void close() {
        mS7Client.Disconnect();
    }

    /**
     * Opens a connection to the PLC if possible.
     */
    public void open() {
        try {
            mS7Client.ConnectTo(mPlc.getIp(), mPlc.getRack(), mPlc.getSlot());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks that the SimaticS7 API client is connected to the PLC.
     *
     * @return true if the SimaticS7 API client is connected; false otherwise.
     */
    public boolean isOpen() {
        try {
            return mS7Client.Connect() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets the PLC on which the connection is built.
     *
     * @return the plc on which the connection is build
     */
    public Plc getPlc() { return mPlc; }

    /**
     * Gets the client of the API.
     *
     * @return the client of the API
     */
    public S7Client getS7Client() { return mS7Client; }
}
