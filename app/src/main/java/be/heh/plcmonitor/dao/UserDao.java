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
import be.heh.plcmonitor.model.User;

import java.util.List;

/**
 * DAO interface for the User entity.
 *
 * @author Terencio Agozzino
 */
public interface UserDao extends GenericDao<User> {

    /**
     * Retrieves a specific User object based on his email address.
     *
     * @param email the email address of the specific user
     * @return the specific user according his email address
     */
    User getUserByEmail(String email);

    /**
     * Retrieves a list of User objects based on the first name.
     *
     * @param firstName the first name of the specific users
     * @return the list of users according to the given first name.
     */
    List<User> getUsersByFirstName(String firstName);

    /**
     * Retrieves a list of User objects based on the last name.
     *
     * @param lastName the last name of the specific users
     * @return the list of users according to the given last name.
     */
    List<User> getUsersByLastName(String lastName);

    /**
     * Retrieves a list of user objects based on one PLC.
     *
     * @param plc the given PLC
     * @return the list of users according to the given plc
     */
    List<User> getUsersByPlc(Plc plc);
}