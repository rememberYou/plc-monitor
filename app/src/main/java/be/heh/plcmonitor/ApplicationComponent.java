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

package be.heh.plcmonitor;

import be.heh.plcmonitor.activity.LoginActivity;
import be.heh.plcmonitor.activity.MainActivity;
import be.heh.plcmonitor.activity.RegisterActivity;

import dagger.Component;
import be.heh.plcmonitor.database.DatabaseModule;

import javax.inject.Singleton;

/**
 * Interface assigns references in the activities to have access to singletons
 * in the DatabaseModule class.
 *
 * @author Terencio Agozzino
 */
@Singleton
@Component(modules = DatabaseModule.class)
public interface ApplicationComponent {

    /**
     * Allows MainActivity to request dependencies declared by the
     * DatabaseModule class.
     *
     * @param mainActivity the main screen of the application
     */
    void inject(MainActivity mainActivity);

    /**
     * Allows LoginActivity to request dependencies declared by the
     * DatabaseModule class.
     *
     * @param loginActivity the login screen that offers login via
     *                      email/password.
     */
    void inject(LoginActivity loginActivity);


    /**
     * Allows RegisterActivity to request dependencies declared by the
     * DatabaseModule class.
     *
     * @param registerActivity the register screen that offers sign up via
     *                         first name, last name, email address, and
     *                         password.
     */
    void inject(RegisterActivity registerActivity);
}
