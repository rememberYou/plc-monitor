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

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Provides a configuration file to avoid using annotation processing in
 * runtime which is very slow under Android.
 *
 * Annotations using this mechanism run 20 times faster than with the native
 * Android calls.
 *
 * The configuration file is written to res/raw/ directory by default.
 * More info at: http://ormlite.com/docs/table-config
 *
 * @author Terencio Agozzino
 */
public class DatabaseConfigUtil extends OrmLiteConfigUtil {

    /**
     * The name of the generated ORMLite config file.
     */
    public static final String CONFIG_FILE_NAME = "ormlite_config.txt";

    /**
     * Main method that needs to be executed after each creation
     * and modification of classes of the database.
     *
     * @param args command line parameters (which are ignored)
     * @throws IOException when the config file cannot be written to `res/raw/`
     *                     directory
     * @throws SQLException when one of the classes contains invalid SQL
     *                      annotations
     */
    public static void main(String[] args) throws IOException, SQLException {
        writeConfigFile(new File(CONFIG_FILE_NAME));
    }
}