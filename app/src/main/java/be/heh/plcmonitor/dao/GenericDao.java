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

import java.util.List;

/**
 * Interface to be implemented by DAO interfaces.
 *
 * GenericDAO can support the CRUD of a specified entity as well
 * as find a specific entity by his primary key, and retrieves all
 * rows linked of an entity to the database.
 *
 * @author Terencio Agozzino
 */
public interface GenericDao<T> {

    /**
     * Creates a new entity from persistent storage in the database.
     *
     * @param entity the new entity to create
     * @return the entity created
     */
    T create(T entity);

    /**
     * Retrieves an entity that was previously persisted to the database using
     * the indicated primary key.
     *
     * @param id the indicated primary key of the entity to retrieve
     * @return the entity to retrieve
     */
    T get(int id);

    /**
     * Retrieves all rows linked of an entity to the database.
     *
     * @return the list of rows linked of an entity to retrieve
     */
    List<T> getAll();

    /**
     * Updates the entity in the database.
     *
     * @param entity the entity to update
     */
    void update(T entity);

    /**
     * Deletes an entity from persistent storage in the database.
     *
     * @param entity the entity to delete
     */
    void delete(T entity);
}