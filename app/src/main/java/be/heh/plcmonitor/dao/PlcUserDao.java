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

import be.heh.plcmonitor.model.Plc;
import be.heh.plcmonitor.model.PlcUser;
import be.heh.plcmonitor.model.User;

import com.j256.ormlite.stmt.PreparedQuery;

import java.sql.SQLException;

/**
 * DAO interface for the PlcUser entity.
 *
 * @author Terencio Agozzino
 */
public interface PlcUserDao extends GenericDao<PlcUser> {

    /**
     * Builds the query for Plc objects that match a User.
     *
     * @return the query for Plc objects that match a User
     * @throws SQLException when PlcUser class contains invalid SQL annotations
     */
    PreparedQuery<Plc> makePlcsForUserQuery() throws SQLException;

    /**
     * Builds the query for User objects that match a PLC.
     *
     * @return the query for User objects that match a PLC
     * @throws SQLException when PlcUser class contains invalid SQL annotations
     */
    PreparedQuery<User> makeUsersForPlcQuery() throws SQLException;
}