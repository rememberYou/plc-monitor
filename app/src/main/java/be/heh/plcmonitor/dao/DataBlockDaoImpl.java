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

package be.heh.plcmonitor.dao;

import be.heh.plcmonitor.database.DatabaseHelper;
import be.heh.plcmonitor.model.DataBlock;

import java.sql.SQLException;

/**
 * Implements basic DAO operations for the Plc entity.
 *
 * @author Terencio Agozzino
 */
public class DataBlockDaoImpl extends GenericDaoImpl<DataBlock> implements DataBlockDao {

    /**
     * Default constructor that retrieves the DAO of the OrmLite implementation
     * of DataBlock objects.
     *
     * @param databaseHelper the database helper
     * @throws SQLException when DatabaseHelper class contains invalid SQL annotations
     */
    public DataBlockDaoImpl(DatabaseHelper databaseHelper) throws SQLException {
        super(databaseHelper.getDataBlockDao());
    }
}