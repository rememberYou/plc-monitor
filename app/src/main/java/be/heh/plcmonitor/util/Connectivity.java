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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Connectivity that allows to handling management of network and get details
 * about the currently active default data network.
 *
 * @author Terencio Agozzino
 */
public class Connectivity {

    /**
     * Context for handling management of network connections.
     */
    private final Context mContext;

    /**
     * Main constructor of the Connectivity class.
     *
     * @param mContext the application context
     */
    public Connectivity(Context mContext) { this.mContext = mContext; }

    /**
     * Indicates whether network connectivity exists or is in the process of
     * being established.
     *
     * @return true if network connectivity exists or is in the process of
     *         being established; false otherwise.
     */
    public boolean isConnected() {
        return getActiveNetworkInfo() != null &&
                getActiveNetworkInfo().isConnectedOrConnecting();
    }

    /**
     * Gets details about the currently active default data network.
     * When connected, this network is the default route for outgoing
     * connections. You should always check isConnected() before initiating
     * network traffic.
     *
     * Requires the ACCESS_NETWORK_STATE permission.
     *
     * @return a NetworkInfo object for the current default network or null
     *         if no default network is currently active
     */
    private NetworkInfo getActiveNetworkInfo() {
        return getConnectivityManager().getActiveNetworkInfo();
    }

    /**
     * Retrieves a ConnectivityManager for handling management of network
     * connections.
     *
     * @return the handle to a system-level service by class.
     */
    private ConnectivityManager getConnectivityManager() {
        return (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
    }
}