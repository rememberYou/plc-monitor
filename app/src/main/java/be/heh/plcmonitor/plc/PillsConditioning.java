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
 * A base model class for pills conditioning.
 *
 * @author Terencio Agozzino
 */
public class PillsConditioning {

    private byte[] data;

    /**
     * Main constructor of the PillsConditioning class.
     *
     * @param data the data storage area of the data block
     */
    public PillsConditioning(byte[] data) { this.data = data; }

    /**
     * Gets the number of filled bottles.
     *
     * @return the number of bottles
     */
    public int getFilledBottles() { return S7.GetWordAt(data, 16); }

    /**
     * Gets the number of produces bottles.
     *
     * @return the number of produces bottles
     */
    public int getProducesBottles() { return S7.GetWordAt(data, 16); }

    /**
     * Checks if 5 pills are requested.
     *
     * @return true if 5 pills are requested; false otherwise
     */
    public boolean is5PillsRequest() { return S7.GetBitAt(data, 4, 3); }

    /**
     * Checks if 10 pills are requested.
     *
     * @return true if 10 pills are requested; false otherwise
     */
    public boolean is10PillsRequest() { return S7.GetBitAt(data, 4, 4); }

    /**
     * Checks if 15 pills are requested.
     *
     * @return true if 15 pills are requested; false otherwise
     */
    public boolean is15PillsRequest() { return S7.GetBitAt(data, 4, 5); }

    /**
     * Checks the status of the cylinder to close the bottles of pills.
     *
     * @return true if the cylinder is running; false otherwise
     */
    public boolean isCylinder() { return S7.GetBitAt(data, 4, 2); }

    /**
     * Checks whether the sensor has detected a bottle that needs to be filled.
     *
     * @return true if the sensor detected a bottle that needs to be filled;
     *         false otherwise
     */
    public boolean isEmptyBottle() { return S7.GetBitAt(data, 0, 4); }

    /**
     * Checks if empty bottles coming in.
     *
     * @return true if empty bottles come in; false otherwise
     */
    public boolean isEmptyBottlesComingIn() { return S7.GetBitAt(data, 1, 3); }

    /**
     * Checks whether the sensor has detected a bottle that needs to be closed.
     *
     * @return true if the sensor detected a bottle that needs to be closed;
     *         false otherwise
     */
    public boolean isOpenBottle() { return S7.GetBitAt(data, 0, 5); }

    /**
     * Checks if the pill dispenser is distributing pills.
     *
     * @return true if the pill dispenser is distributing pills; false otherwise
     */
    public boolean isPassingPills() { return S7.GetBitAt(data, 0, 6); }

    /**
     * Checks if pills are requested.
     *
     * @return true if pills are requested; false otherwise
     */
    public boolean isPillsRequest() {
        return is5PillsRequest() || is10PillsRequest() || is15PillsRequest();
    }

    /**
     * Checks the PLC remote connection status.
     *
     * @return true if the PLC is remotely controllable; false otherwise
     */
    public boolean isRemotelyControllable() { return S7.GetBitAt(data, 1,6); }

    /**
     * Checks the status of the conveyor motor
     *
     * @return true if the conveyor is running; false otherwise
     */
    public boolean isMotorConveyor() { return S7.GetBitAt(data, 4, 1); }

    /**
     * Checks the status of the distributor pills motor
     *
     * @return true if the distributor pills motor is running; false otherwise
     */
    public boolean isMotorDistributorPills() { return S7.GetBitAt(data, 4, 0); }
}
