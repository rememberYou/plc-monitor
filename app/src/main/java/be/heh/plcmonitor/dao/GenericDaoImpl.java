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

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Class to be extends by DAO implementations classes, it allows basic DAO
 * operations.
 *
 * @author Terencio Agozzino
 */
public class GenericDaoImpl<T> implements GenericDao<T> {

    protected Dao<T, Integer> dao;

    /**
     * Default constructor that retrieves the DAO object of the OrmLite
     * implementation.
     */
    public GenericDaoImpl(Dao<T, Integer> dao) {
        this.dao = dao;
    }

    /**
     * Creates a new entity from persistent storage in the database.
     *
     * @param entity the new entity to create
     * @return the entity created
     */
    @Override
    public T create(T entity) {
        try {
            return dao.createIfNotExists(entity);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves an entity that was previously persisted to the database using
     * the indicated primary key.
     *
     * @param id the indicated primary key of the entity to retrieve
     * @return the entity to retrieve
     */
    @Override
    public T get(int id) {
        try {
            return dao.queryForId(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves all rows linked of an entity to the database.
     *
     * @return the list of rows linked of an entity to retrieve
     */
    @Override
    public List<T> getAll() {
        try {
            return dao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Updates the entity in the database.
     *
     * @param entity the entity to update
     */
    @Override
    public void update(T entity) {
        try {
            dao.update(entity);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes an entity from persistent storage in the database.
     *
     * @param entity the entity to delete
     */
    @Override
    public void delete(T entity) {
        try {
            dao.delete(entity);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}