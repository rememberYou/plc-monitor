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

package be.heh.plcmonitor.model;

import com.j256.ormlite.field.DatabaseField;

/**
 * A base model class which links users to their PLCs.
 *
 * @author Terencio Agozzino
 */
public class PlcUser {

    /**
     * Database field names.
     */
    public static final String PLC_ID_FIELD_NAME = "plc_id";
    public static final String USER_ID_FIELD_NAME = "user_id";

    /**
     * Properties with ORMLite annotations for the database.
     */
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(foreign = true, columnName = PLC_ID_FIELD_NAME,
            foreignAutoRefresh = true)
    private Plc plc;

    @DatabaseField(foreign = true, columnName = USER_ID_FIELD_NAME,
            foreignAutoRefresh = true)
    private User user;

    /**
     * Default constructor of the PlcUser class needed for ORMLite.
     */
    public PlcUser() { }

    /**
     * Main constructor of the PlcUser class.
     *
     * @param plc the PLC
     * @param user the user
     */
    public PlcUser(Plc plc, User user) {
        this.plc = plc;
        this.user = user;
    }

    /**
     * Specifies the representation of the join between user and PLC.
     *
     * @return the details of the join between user and PLC
     */
    @Override
    public String toString() {
        return this.plc.toString() + "\n" + this.user.toString();
    }
}