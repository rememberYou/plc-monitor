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

package be.heh.plcmonitor.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import be.heh.plcmonitor.R;
import be.heh.plcmonitor.model.Plc;
import be.heh.plcmonitor.model.User;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

import javax.inject.Inject;

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
     * DAO object used to access the different tables.
     */
    private Dao<Plc, Integer> plcDao;
    private Dao<User, Integer> userDao;
    private RuntimeExceptionDao<Plc, Integer> plcRuntimeDao;
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
            TableUtils.createTable(connectionSource, Plc.class);
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
            TableUtils.dropTable(connectionSource, User.class, true);
            onCreate(sqliteDatabase, connectionSource);
        } catch (SQLException e) {
            Log.e(TAG, "Unable to upgrade database from version " +
                    oldVersion + " to new " + newVersion, e);
            throw new RuntimeException(e);
        }
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
        plcDao = null;
        userDao = null;
        plcRuntimeDao = null;
        userRuntimeDao = null;
    }
}