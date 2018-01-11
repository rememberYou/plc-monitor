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

package be.heh.plcmonitor.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import be.heh.plcmonitor.R;
import be.heh.plcmonitor.model.DataBlock;
import be.heh.plcmonitor.model.Plc;
import be.heh.plcmonitor.model.User;
import be.heh.plcmonitor.model.PlcUser;

import com.j256.ormlite.cipher.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

import javax.inject.Inject;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.text.RandomStringGenerator;

import static android.content.Context.MODE_PRIVATE;
import static org.apache.commons.text.CharacterPredicates.DIGITS;
import static org.apache.commons.text.CharacterPredicates.LETTERS;

/**
 * Helper class used to manage the creation and upgrading of the database.
 * This class also usually provides the DAOs used by the other classes.
 *
 * @author Terencio Agozzino
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    /**
     * Useful for debug to identify which class has logged.
     */
    private static final String TAG = DatabaseHelper.class.getSimpleName();

    /**
     * Name of the database file of the application.
     */
    private static final String DATABASE_NAME = "user.db";

    /**
     * Database version that may have to be increase at any changes to the
     * database objects.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Retrieves and hold the contents of the key
     */
    private SharedPreferences sp;

    /**
     * Key to encrypt and decrypt the database.
     */
    private String key;

    /**
     * DAO object used to access the different tables.
     */
    private Dao<DataBlock, Integer> dataBlockDao;
    private Dao<Plc, Integer> plcDao;
    private Dao<User, Integer> userDao;
    private Dao<PlcUser, Integer> plcUserDao;

    /**
     * RuntimeException for DAO object used to access the different tables.
     */
    private RuntimeExceptionDao<DataBlock, Integer> dataBlockRuntimeDao;
    private RuntimeExceptionDao<Plc, Integer> plcRuntimeDao;
    private RuntimeExceptionDao<PlcUser, Integer> plcUserRuntimeDao;
    private RuntimeExceptionDao<User, Integer> userRuntimeDao;

    /**
     * Main constructor of the helper class.
     *
     * @param context the application context
     */
    @Inject
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION,
                R.raw.ormlite_config);
        getDatabaseKey(context);
    }

    /**
     * Retrieves or generates the database key to encrypt and decrypt data.
     * The idea is to use a unique key for each device that uses the database.
     *
     * In order to keep in memory the key generated for future uses, I use
     * SharedPreferences which may not be the most ingenious.
     *
     * @param context the application context
     */
    public void getDatabaseKey(Context context) {
        sp = context.getSharedPreferences("db", MODE_PRIVATE);
        key = sp.getString("key", "");

        if (key.equals("")) {
            key = randomString(100);
            sp.edit().putString("key", key).apply();
        } else {
            key = sp.getString("key", "");
        }
    }

    /**
     * Called when the database is created for the first time. Meaning only
     * if a database doesn't already exists on disk with the same
     * DATABASE_NAME.
     *
     * @param sqliteDatabase the SQLite database
     * @param connectionSource a reduction of the SQL DataSource to implement
     *                         its functionality outside of JDBC
     */
    @Override
    public void onCreate(SQLiteDatabase sqliteDatabase,
                         ConnectionSource connectionSource) {
        try {
            dataBlockDao = DaoManager.createDao(connectionSource, DataBlock.class);
            userDao = DaoManager.createDao(connectionSource, User.class);
            plcDao = DaoManager.createDao(connectionSource, Plc.class);
            plcUserDao = DaoManager.createDao(connectionSource, PlcUser.class);

            TableUtils.createTable(connectionSource, DataBlock.class);
            TableUtils.createTable(connectionSource, Plc.class);
            TableUtils.createTable(connectionSource, PlcUser.class);
            TableUtils.createTable(connectionSource, User.class);
        } catch (SQLException e) {
            Log.e(TAG, "Unable to create database", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Called when the database needs to be upgraded. Meaning only
     * if a database already exists on disk with the same
     * DATABASE_NAME, but with a different version than DATABASE_VERSION.
     *
     * @param sqliteDatabase the SQLite database
     * @param connectionSource a reduction of the SQL DataSource to implement
     *                         its functionality outside of JDBC
     * @param oldVersion the old version of the database
     * @param newVersion the new version of the database
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqliteDatabase,
                          ConnectionSource connectionSource,
                          int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, DataBlock.class, true);
            TableUtils.dropTable(connectionSource, Plc.class, true);
            TableUtils.dropTable(connectionSource, PlcUser.class, true);
            TableUtils.dropTable(connectionSource, User.class, true);
            onCreate(sqliteDatabase, connectionSource);
        } catch (SQLException e) {
            Log.e(TAG, "Unable to upgrade database from version " +
                    oldVersion + " to new " + newVersion, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Generates a random string of numbers, upper and lower case letters
     * according to a defined length.
     *
     * @param length the length of the generated character string
     * @return the random string of numbers, upper and lower case letters
     */
    public String randomString(int length) {
        return new RandomStringGenerator.Builder()
                .withinRange('0', 'z')
                .filteredBy(LETTERS, DIGITS)
                .build()
                .generate(length);
    }

    /**
     * Creates or retrieves the DAO (Database Access Object) cached value
     * for the DataBlock class.
     *
     * @return the DAO for the DataBlock class
     * @throws SQLException when DataBlock class contains invalid SQL annotations
     */
    public Dao<DataBlock, Integer> getDataBlockDao() throws SQLException {
        if (dataBlockDao == null) {
            dataBlockDao = getDao(DataBlock.class);
        }

        return dataBlockDao;
    }

    /**
     * Creates or retrieves the RuntimeExceptionDao cached value version of a
     * DAO (Database Access Object) for the DataBlock class.
     *
     * This method should be called only through RuntimeExceptions.
     *
     * @return the RuntimeExceptionDao for the DataBlock class
     */
    public RuntimeExceptionDao<DataBlock, Integer> getDataBlockDataDao() {
        if (dataBlockRuntimeDao == null) {
            dataBlockRuntimeDao = getRuntimeExceptionDao(DataBlock.class);
        }

        return dataBlockRuntimeDao;
    }

    /**
     * Creates or retrieves the DAO (Database Access Object) cached value
     * for the Plc class.
     *
     * @return the DAO for the Plc class
     * @throws SQLException when Plc class contains invalid SQL annotations
     */
    public Dao<Plc, Integer> getPlcDao() throws SQLException {
        if (plcDao == null) {
            plcDao = getDao(Plc.class);
        }

        return plcDao;
    }

    /**
     * Creates or retrieves the RuntimeExceptionDao cached value version of a
     * DAO (Database Access Object) for the Plc class.
     *
     * This method should be called only through RuntimeExceptions.
     *
     * @return the RuntimeExceptionDao for the Plc class
     */
    public RuntimeExceptionDao<Plc, Integer> getPlcDataDao() {
        if (plcRuntimeDao == null) {
            plcRuntimeDao = getRuntimeExceptionDao(Plc.class);
        }

        return plcRuntimeDao;
    }

    /**
     * Creates or retrieves the DAO (Database Access Object) cached value
     * for the PlcUser class.
     *
     * @return the DAO for the PlcUser class
     * @throws SQLException when PlcUser class contains invalid SQL annotations
     */
     public Dao<PlcUser, Integer> getPlcUserDao() throws SQLException {
        if (plcUserDao == null) {
            plcUserDao = getDao(PlcUser.class);
        }

        return plcUserDao;
    }

    /**
     * Creates or retrieves the RuntimeExceptionDao cached value version of a
     * DAO (Database Access Object) for the PlcUser class.
     *
     * This method should be called only through RuntimeExceptions.
     *
     * @return the RuntimeExceptionDao for the UserPlc class
     */
     public RuntimeExceptionDao<PlcUser, Integer> getPlcUserDataDao() {
        if (plcUserRuntimeDao == null) {
            plcUserRuntimeDao = getRuntimeExceptionDao(PlcUser.class);
        }

        return plcUserRuntimeDao;
    }

    /**
     * Creates or retrieves the DAO (Database Access Object) cached value
     * for the User class.
     *
     * @return the DAO for the User class
     * @throws SQLException when User class contains invalid SQL annotations
     */
    public Dao<User, Integer> getUserDao() throws SQLException {
        if (userDao == null) {
            userDao = getDao(User.class);
        }

        return userDao;
    }

    /**
     * Creates or retrieves the RuntimeExceptionDao cached value version of a
     * DAO (Database Access Object) for the User class.
     *
     * This method should be called only through RuntimeExceptions.
     *
     * @return the RuntimeExceptionDao for the User class
     */
    public RuntimeExceptionDao<User, Integer> getUserDataDao() {
        if (userRuntimeDao == null) {
            userRuntimeDao = getRuntimeExceptionDao(User.class);
        }

        return userRuntimeDao;
    }

    /**
     * Closes the database connection and clear any cached DAOs.
     */
    @Override
    public void close() {
        super.close();

        dataBlockDao = null ;
        plcDao = null;
        userDao = null;
        plcUserDao = null;

        dataBlockRuntimeDao = null;
        plcRuntimeDao = null;
        userRuntimeDao = null;
        plcUserRuntimeDao = null;
    }

    /**
     * Gets the password to encrypt and decrypt the database.
     *
     * @return the password to encrypt and decrypt the database
     */
    @Override
    public String getPassword() { return key; }
}