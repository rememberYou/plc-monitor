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

import be.heh.plcmonitor.dao.DataBlockDaoImpl;
import be.heh.plcmonitor.dao.PlcDaoImpl;
import be.heh.plcmonitor.dao.PlcUserDaoImpl;
import be.heh.plcmonitor.dao.UserDaoImpl;

import dagger.Module;
import dagger.Provides;

import java.sql.SQLException;

import javax.inject.Singleton;

/**
 * Module class of the database that will ensures dependency injection.
 *
 * @author Terencio Agozzino
 */
@Module
public class DatabaseModule {

    /**
     * The context of the application.
     */
    private Context mContext;

    /**
     * Main constructor that allows to instantiate the module of the database
     * that allows the search in the available methods for possible instance
     * providers, using the context of the application.
     *
     * @param mContext the context of the application
     */
    public DatabaseModule(Context mContext) { this.mContext = mContext; }

    /**
     * Provides the only instance of the context of the application using
     * Singleton.
     *
     * @return the one and only instance of the context of the application
     */
    @Provides
    @Singleton
    public Context provideContext() { return mContext; }

    /**
     * Provides the only instance of the DatabaseHelper using Singleton.
     *
     * @param context the context of the application
     * @return the one and only instance of the helper
     */
    @Provides
    @Singleton
    public DatabaseHelper provideDatabaseHelper(Context context) {
        return new DatabaseHelper(context);
    }

    /**
     * Provides the only instance of the DataBlockImpl using Singleton.
     *
     * @param databaseHelper the database helper
     * @return the only instance of the DataBlockImpl
     */
    @Provides
    @Singleton
    public DataBlockDaoImpl provideDataBlockDaoImpl(DatabaseHelper databaseHelper) {
        try {
            return new DataBlockDaoImpl(databaseHelper);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Provides the only instance of the PlcDaoImpl using Singleton.
     *
     * @param databaseHelper the database helper
     * @return the only instance of the PlcDaoImpl
     */
    @Provides
    @Singleton
    public PlcDaoImpl providePlcDaoImpl(DatabaseHelper databaseHelper) {
        try {
            return new PlcDaoImpl(databaseHelper);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Provides the only instance of the PlcUserDaoImpl using Singleton.
     *
     * @param databaseHelper the database helper
     * @return the only instance of the PlcUserDaoImpl
     */
    @Provides
    @Singleton
    public PlcUserDaoImpl providePlcUserDaoImpl(DatabaseHelper databaseHelper) {
        try {
            return new PlcUserDaoImpl(databaseHelper);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Provides the only instance of the UserDaoImpl using Singleton.
     *
     * @param databaseHelper the database helper
     * @return the only instance of the UserDaoImpl
     */
    @Provides
    @Singleton
    public UserDaoImpl provideUserDaoImpl(DatabaseHelper databaseHelper) {
        try {
            return new UserDaoImpl(databaseHelper);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}