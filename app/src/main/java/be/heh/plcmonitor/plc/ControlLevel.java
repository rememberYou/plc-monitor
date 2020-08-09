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

package be.heh.plcmonitor.plc;

import SimaticS7.S7;

/**
 * A base model class for control level
 *
 * @author Terencio Agozzino
 */
public class ControlLevel {

    private byte[] data;

    /**
     * Main constructor of the ControlLevel class.
     *
     * @param data the data storage area of the data block
     */
    public ControlLevel(byte[] data) { this.data = data; }

    /**
     * Checks whether the configuration is manual or automatic.
     *
     * @return true if the configuration is manual; false otherwise
     */
    public boolean isManual() { return S7.GetBitAt(data, 0, 5); }

    /**
     * Checks if valve 1 is open.
     *
     * @return true if valve 1 is open; false otherwise
     */
    public boolean isValve1Open() { return S7.GetBitAt(data, 0, 1); }

    /**
     * Checks if valve 2 is open.
     *
     * @return true if valve 2 is open; false otherwise
     */
    public boolean isValve2Open() { return S7.GetBitAt(data, 0, 2); }

    /**
     * Checks if valve 3 is open.
     *
     * @return true if valve 3 is open; false otherwise
     */
    public boolean isValve3Open() { return S7.GetBitAt(data, 0, 3); }

    /**
     * Checks if valve 4 is open.
     *
     * @return true if valve 4 is open; false otherwise
     */
    public boolean isValve4Open() { return S7.GetBitAt(data, 0, 4); }

    /**
     * Checks the PLC remote connection status.
     *
     * @return true if the PLC is remotely controllable; false otherwise
     */
    public boolean isRemotelyControllable() { return S7.GetBitAt(data, 0, 6); }

    /**
     * Get the manual value.
     *
     * @return the manual value.
     */
    public int getManualValue() { return S7.GetWordAt(data, 20); }

    /**
     * Gets the set point.
     *
     * @return the set point
     */
    public int getSetPoint() { return S7.GetWordAt(data, 18); }

    /**
     * Get the valve control word.
     *
     * @return the valve control word.
     *
     */
    public int getValveControlWord() { return S7.GetWordAt(data, 22); }

    /**
     *  Gets the water level.
     *
     * @return the water level
     */
    public int getWaterLevel() { return S7.GetWordAt(data, 16); }
}