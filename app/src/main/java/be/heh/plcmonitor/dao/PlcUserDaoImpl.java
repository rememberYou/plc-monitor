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
import be.heh.plcmonitor.model.Plc;
import be.heh.plcmonitor.model.PlcUser;
import be.heh.plcmonitor.model.User;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;

import java.sql.SQLException;

/**
 * Implements basic DAO operations for the PlcUser entity.
 *
 * @author Terencio Agozzino
 */
public class PlcUserDaoImpl extends GenericDaoImpl<PlcUser> implements PlcUserDao {

    /**
     * DAO object used to access the different tables.
     */
    private Dao<Plc, Integer> plcDao;
    private Dao<User, Integer> userDao;
    private Dao<PlcUser, Integer> plcUserDao;

    /**
     * Default constructor that retrieves the DAO of the OrmLite implementation
     * of PlcUser objects.
     *
     * @param databaseHelper the database helper
     * @throws SQLException when DatabaseHelper class contains invalid SQL annotations
     */
    public PlcUserDaoImpl(DatabaseHelper databaseHelper) throws SQLException {
        super(databaseHelper.getPlcUserDao());

        plcUserDao = databaseHelper.getPlcUserDao();
        plcDao = databaseHelper.getPlcDao();
        userDao = databaseHelper.getUserDao();
    }

    /**
     * Builds the query for Plc objects that match a User.
     *
     * @return the query for Plc objects that match a User
     * @throws SQLException when PlcUser class contains invalid SQL annotations
     */
    @Override
    public PreparedQuery<Plc> makePlcsForUserQuery() throws SQLException {
        QueryBuilder<PlcUser, Integer> plcUserQb = plcUserDao.queryBuilder();
        plcUserQb.selectColumns(PlcUser.PLC_ID_FIELD_NAME);
        SelectArg userSelectArg = new SelectArg();
        plcUserQb.where().eq(PlcUser.USER_ID_FIELD_NAME, userSelectArg);

        QueryBuilder<Plc, Integer> plcQb = plcDao.queryBuilder();
        plcQb.where().in(Plc.ID_FIELD_NAME, plcUserQb);
        return plcQb.prepare();
    }

    /**
     * Builds the query for User objects that match a PLC.
     *
     * @return the query for User objects that match a PLC
     * @throws SQLException when PlcUser class contains invalid SQL annotations
     */
    @Override
    public PreparedQuery<User> makeUsersForPlcQuery() throws SQLException {
        QueryBuilder<PlcUser, Integer> userPlcQb
                = dao.queryBuilder().selectColumns(PlcUser.USER_ID_FIELD_NAME);
        userPlcQb.where().eq(PlcUser.PLC_ID_FIELD_NAME, new SelectArg());

        QueryBuilder<User, Integer> userQb = userDao.queryBuilder();
        userQb.where().in(Plc.ID_FIELD_NAME, userQb);
        return userQb.prepare();
    }
}
