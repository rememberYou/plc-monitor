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

import be.heh.plcmonitor.database.DatabaseHelper;
import be.heh.plcmonitor.model.User;

import java.sql.SQLException;
import java.util.List;

/**
 * Implements basic DAO operations for the User entity.
 *
 * @author Terencio Agozzino
 */
public class UserDaoImpl extends GenericDaoImpl<User> implements UserDao {

    /**
     * Default constructor that retrieves the DAO of the OrmLite implementation
     * of User objects.
     *
     * @param databaseHelper the database helper
     */
    public UserDaoImpl(DatabaseHelper databaseHelper) throws SQLException {
        super(databaseHelper.getUserDao());
    }

    /**
     * Retrieves a specific User object based on his email address.
     *
     * @param email the email address of the specific user
     * @return the specific user according his email address
     */
    @Override
    public User getUserByEmail(String email) {
        try {
            List<User> users = dao.query(dao.queryBuilder()
                    .where().eq(User.EMAIL_FIELD_NAME, email)
                    .prepare());

            if (users.size() == 0) {
                return null;
            } else {
                return users.get(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Retrieves a list of User objects based on the first name.
     *
     * @param firstName the first name of the specific users
     * @return the list of users according to the given first name.
     */
    @Override
    public List<User> getUsersByFirstName(String firstName) {
        List<User> users = null;
        try {
            users = dao.query(dao.queryBuilder()
                    .where().eq(User.FIRST_NAME_FIELD_NAME, firstName)
                    .prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    /**
     * Retrieves a list of User objects based on the last name.
     *
     * @param lastName the last name of the specific users
     * @return the list of users according to the given last name.
     */
    @Override
    public List<User> getUsersByLastName(String lastName) {
        List<User> users = null;
        try {
            users = dao.query(dao.queryBuilder()
                    .where().eq(User.LAST_NAME_FIELD_NAME, lastName)
                    .prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }
}