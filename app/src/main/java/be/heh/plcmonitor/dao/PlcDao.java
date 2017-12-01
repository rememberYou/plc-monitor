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

package be.heh.plcmonitor.dao;

import be.heh.plcmonitor.model.Plc;

import java.util.List;

/**
 * DAO interface for the PLC entity.
 *
 * @author Terencio Agozzino
 */
public interface PlcDao extends GenericDao<Plc> {

    /**
     * Retrieves a list of PLC objects based on one IP address.
     *
     * @param ip the IP address of the specific PLC
     * @return the list of PLC according to the given IP address
     */
    List<Plc> getPlcsByIp(String ip);

    /**
     * Retrieves a specific PLC object based on his name.
     *
     * @param name the name of the specific PLC
     * @return the specific PLC according his name.
     */
    Plc getPlcByName(String name);

    /**
     * Retrieves a list of PLC objects based on the rack.
     *
     * @param rack the rack of the specific PLCs
     * @return the list of PLCs according to the given rack
     */
    List<Plc> getPlcsByRack(int rack);

    /**
     * Retrieves a list of PLC objects based on the slot.
     *
     * @param slot the slot of the specific PLCs
     * @return the list of PLCs according to the given slot
     */
    List<Plc> getPlcsBySlot(int slot);

    /**
     * Retrieves a list of PLC objects based on one substring.
     *
     * @param subString the given substring
     * @return the list of PLC according to the given substring
     */
    List<Plc> getPlcBySubString(String subString);
}