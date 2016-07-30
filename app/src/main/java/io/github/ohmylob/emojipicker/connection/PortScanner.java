package io.github.ohmylob.emojipicker.connection;

/*
 * Copyright (C) 2016 Matteo Lobello
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

import java.net.InetAddress;

import io.github.ohmylob.emojipicker.activity.MainActivity;
import io.github.ohmylob.emojipicker.debug.Logger;

import static io.github.ohmylob.emojipicker.connection.ConnectionUtils.getIp;
import static io.github.ohmylob.emojipicker.connection.ConnectionUtils.getSubnet;

public class PortScanner {

    /**
     * The milliseconds of timeout
     */
    private static final int TIMEOUT = 150;

    /**
     * A boolean value to check if the AsyncTask is finished
     */
    private boolean isFinished;

    /**
     * A boolean value to finish the AsyncTask
     */
    private boolean finishAsyncTask;

    /**
     * A private constructor
     */
    private PortScanner() {
        // Nothing to put here
    }

    /**
     * Get a new instance of this class
     *
     * @return a new instance of this class
     */
    public static PortScanner getInstance() {
        return new PortScanner();
    }

    /**
     * A method used to check if the AsyncTask is finished
     *
     * @return true if the AsyncTask is finished
     */
    public boolean isFinished() {
        return isFinished;
    }

    /**
     * Finish AsyncTask
     */
    public void finish() {
        finishAsyncTask = true;
    }

    /**
     * Start an AsyncTask to portscan the network
     *
     * @param activity           The current Activity or Context
     * @param onAvailableIpFound Used as callback when a new IP has been found
     */
    public void startPortScanningAsync(final Context activity, final OnAvailableIpFound onAvailableIpFound) {
        new AsyncTask<Void, Void, Void>() {

            private boolean found;

            @Override
            protected void onPreExecute() {
            }

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    String subnet = getSubnet(getIp(activity));

                    for (int i = 1; i < 254; i++) {
                        if (!finishAsyncTask) {
                            String host = subnet + i;

                            Logger.print("Checking " + host);

                            if (InetAddress.getByName(host).isReachable(TIMEOUT)) {
                                if (onAvailableIpFound != null) {
                                    if (onAvailableIpFound.onAvailableIp(host)) {
                                        found = true;
                                        break;
                                    }
                                }
                            }
                        } else {
                            found = true;
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (!found && activity != null) {
                    if (activity instanceof MainActivity) {
                        final MainActivity mainActivity = (MainActivity) activity;

                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mainActivity.pairingWithPc.setVisibility(View.GONE);
                                mainActivity.noPcFound.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }

                isFinished = true;
            }
        }.execute();
    }

    /**
     * An interface used as callback in the AsyncTask
     */
    public interface OnAvailableIpFound {

        /**
         * The callback when a new available IP has been found
         *
         * @param ip
         * @return true if found a PC with the program opened
         */
        boolean onAvailableIp(String ip);
    }
}
