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

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import be.heh.plcmonitor.database.DatabaseHelper;
import be.heh.plcmonitor.model.Plc;

import java.sql.SQLException;
import java.util.List;

/**
 * Implements basic DAO operations for the Plc entity.
 *
 * @author Terencio Agozzino
 */
public class PlcDaoImpl extends GenericDaoImpl<Plc> implements PlcDao {

    /**
     * Default constructor that retrieves the DAO of the OrmLite implementation
     * of PLC objects.
     *
     * @param databaseHelper the database helper
     */
    public PlcDaoImpl(DatabaseHelper databaseHelper) throws SQLException {
        super(databaseHelper.getPlcDao());
    }

    /**
     * Retrieves a list of PLC objects based on one IP address.
     *
     * @param ip the IP address of the specific PLC
     * @return the list of PLC according to the given IP address
     */
    @Override
    public List<Plc> getPlcsByIp(String ip) {
        List<Plc> plcs = null;
        try {
            plcs = dao.query(dao.queryBuilder()
                    .where().eq(Plc.IP_FIELD_NAME, ip)
                    .prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return plcs;
    }

    /**
     * Retrieves a specific PLC object based on his name.
     *
     * @param name the name of the specific PLC
     * @return the specific PLC according his name.
     */
    @Override
    public Plc getPlcByName(String name) {
        try {
            List<Plc> plcs = dao.query(dao.queryBuilder()
                    .where().eq(Plc.NAME_FIELD_NAME, name)
                    .prepare());

            if (plcs.size() == 0) {
                return null;
            } else {
                return plcs.get(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Retrieves a list of PLC objects based on the rack.
     *
     * @param rack the rack of the specific PLCs
     * @return the list of PLCs according to the given rack
     */
    @Override
    public List<Plc> getPlcsByRack(int rack) {
        List<Plc> plcs = null;
        try {
            plcs = dao.query(dao.queryBuilder()
                    .where().eq(Plc.RACK_FIELD_NAME, rack)
                    .prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return plcs;
    }

    /**
     * Retrieves a list of PLC objects based on the slot.
     *
     * @param slot the slot of the specific PLCs
     * @return the list of PLCs according to the given slot
     */
    @Override
    public List<Plc> getPlcsBySlot(int slot) {
        List<Plc> plcs = null;
        try {
            plcs = dao.query(dao.queryBuilder()
                    .where().eq(Plc.SLOT_FIELD_NAME, slot)
                    .prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return plcs;
    }

    /**
     * Retrieves a list of PLC objects based on one substring.
     *
     * @param subString the given substring
     * @return the list of PLC according to the given substring
     */
    @Override
    public List<Plc> getPlcBySubString(String subString) {
        try {
            QueryBuilder<Plc, Integer> statementBuilder = dao.queryBuilder();

            Where whereClause = statementBuilder.where();
            whereClause.like(Plc.NAME_FIELD_NAME, "%" + subString + "%");

            return dao.query(statementBuilder.prepare());
        } catch (SQLException e) {
            return null;
        }
    }
}