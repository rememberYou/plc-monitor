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

package be.heh.plcmonitor.util;

import android.util.Patterns;

import be.heh.plcmonitor.dao.UserDaoImpl;

/**
 * Validator that allows to check various fields and things for users and PLCs.
 *
 * @author Terencio Agozzino
 */
public class Validator {

    /**
     * Specifies the validation of a login for a user.
     *
     * @param userDaoImpl the implementation of the user DAO
     * @param email the email address of the user
     * @param password the password of the user
     * @return true if the connection is a valid one; false otherwise
     */
    public static boolean isValidConnection(UserDaoImpl userDaoImpl,
                                            String email, String password) {
        return userDaoImpl.getUserByEmail(email).getPassword().equals(password);
    }

    /**
     * Specifies the validation of an email address for a user.
     *
     * @param email the email address of the user
     * @return true if the email is a valid one; false otherwise
     */
    public static boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Specifies the validation of an IPv4 address for a PLC.
     *
     * @param ip the ipv4 address of the PLC
     * @return true if the ipv4 address is a valid one; false otherwise.
     */
    public static boolean isValidIp(String ip) {
        return Patterns.IP_ADDRESS.matcher(ip).matches();
    }

    /**
     * Specifies the validation of a name of a PLC or user.
     *
     * @param name the name of the plc or user
     * @return true if the name is a valid one; false otherwise
     */
    public static boolean isValidName(String name) {
        return name.matches("^([A-Za-z]+)(\\s[A-Za-z]+)*\\s?$");
    }

    /**
     * Specifies the validation of a user password.
     *
     * @param password the password of the user
     * @return true if the password is a valid one; false otherwise.
     */
    public static boolean isValidPassword(String password) {
        return password.length() >= 8;
    }
}