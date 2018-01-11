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

package be.heh.plcmonitor.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;

import java.util.ArrayList;

/**
 * A base model class for user.
 *
 * This class uses Parcelable instead of Serializable to avoid the slow process
 * of reflection that also create a bunch of temporary objects and cause quite
 * a bit of garbage collection.
 *
 * @author Terencio Agozzino
 */
public class User implements Parcelable {

    /**
     * Database field names.
     */
    public static final String EMAIL_FIELD_NAME = "email";
    public static final String FIRST_NAME_FIELD_NAME = "first_name";
    public static final String ID_FIELD_NAME = "id";
    public static final String LAST_NAME_FIELD_NAME= "last_name";
    public static final String PASSWORD_FIELD_NAME = "password";
    public static final String PERMISSION_FIELD_NAME = "permission";

    /**
     * Properties with ORMLite annotations for the database.
     */
    @DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
    private int id;

    @DatabaseField(columnName = FIRST_NAME_FIELD_NAME, canBeNull = false)
    private String firstName;

    @DatabaseField(columnName = LAST_NAME_FIELD_NAME, canBeNull = false)
    private String lastName;

    @DatabaseField(columnName = EMAIL_FIELD_NAME, unique = true, index = true,
            canBeNull = false)
    private String email;

    @DatabaseField(columnName = PASSWORD_FIELD_NAME, canBeNull = false)
    private String password;

    @DatabaseField(columnName = PERMISSION_FIELD_NAME, defaultValue = "0",
            canBeNull = false)
    private int permission;

    /**
     * Default constructor of the User class needed for ORMLite.
     */
    public User() { }

    /**
     * Main constructor of the User class.
     *
     * @param firstName the first name of the user
     * @param lastName the last name of the user
     * @param email the email address of the user
     * @param password the password of the user
     */
    public User(String firstName, String lastName,
                String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.permission = 0;
    }

    /**
     * Constructor of the User class allowing to assign a permission to the user.
     *
     * @param firstName the first name of the user
     * @param lastName the last name of the user
     * @param email the email address of the user
     * @param password the password of the user
     * @param permission the permission of the user
     */
    public User(String firstName, String lastName, String email,
                String password, int permission) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.permission = permission;
    }

    /**
     * Retrieves User data from Parcel object.
     *
     * This constructor is called from the method
     * createFromParcel(Parcel parcel) of the CREATOR object.
     *
     * @param source the Parcel containing the user data
     */
    private User(Parcel source) {
        this.id = source.readInt();
        this.firstName = source.readString();
        this.lastName = source.readString();
        this.email = source.readString();
        this.password = source.readString();
        this.permission = source.readInt();
    }

    /**
     * Specifies if a user is an administrator or not.
     *
     * @return true if the user is an administrator; false otherwise
     */
    public boolean isAdmin() { return permission == 1; }

    /**
     * Gets the identifier of the user.
     *
     * @return the identifier of the user
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the identifier of the user.
     *
     * @param id the identifier of the PLC
     */
    public void setId(int id) { this.id = id; }

    /**
     * Gets the email address of the user.
     *
     * @return the email address of the user
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address of the user.
     *
     * @param email the email address of the user
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the first name of the user.
     *
     * @return the first name of the user
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name of the user.
     *
     * @param firstName the first name of the user
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the last name of the user.
     *
     * @return the last name of the user
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the last name of the user.
     *
     * @param lastName the last name of the user
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets the password of the user.
     *
     * @return the password of the user
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password of the user.
     *
     * @param password the password of the user
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the permission of the user.
     *
     * @return the permission of the user
     */
    public int getPermission() {
        return permission;
    }

    /**
     * Sets the permission of the user.
     *
     * @param permission the permission of the user
     */
    public void setPermission(int permission) {
        this.permission = permission;
    }

    /**
     * Specifies whether two users are the same.
     *
     * @param user the other user
     * @return true if the identifier of the users are the same; false otherwise
     */
    public boolean equals(User user) { return this.id == user.id; }

    /**
     * Specifies the representation of the user.
     *
     * @return the details of the user
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", permission='" + permission + '\'' +
                '}';
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable
     * instance's marshaled representation. For example, if the object will
     * include a file descriptor in the output of writeToParcel(Parcel, int),
     * the return value of this method must include the CONTENTS_FILE_DESCRIPTOR
     * bit.
     *
     * @return a bitmask indicating the set of special object types marshaled
     *         by this Parcelable object instance.
     *         Value is either 0 or CONTENTS_FILE_DESCRIPTOR.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest the Parcel in which the object should be written.
     * @param flags additional flags about how the object should be written.
     *              May be 0 or PARCELABLE_WRITE_RETURN_VALUE.
     *              Value is either 0 or PARCELABLE_WRITE_RETURN_VALUE.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(email);
        dest.writeString(password);
        dest.writeInt(permission);
    }

    /**
     * Generates instances of your Parcelable class from a Parcel.
     */
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {

        /**
         * Creates a new instance of the User class, instantiating it from the
         * given Parcel whose data had previously been written by.
         *
         * @param source the Parcel to read the object's data from
         * @return a new instance of the User class.
         */
        @Override
        public User createFromParcel(Parcel source) { return new User(source); }

        /**
         * Creates a new array of the User class.

         * @param size the size of the array.
         * @return an array of the User class, with every entry initialized to
         *         null.
         */
        @Override
        public User[] newArray(int size) { return new User[size]; }
    };
}