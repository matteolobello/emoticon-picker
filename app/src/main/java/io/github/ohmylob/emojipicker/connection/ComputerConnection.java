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

import android.os.StrictMode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;

import io.github.ohmylob.emojipicker.debug.Logger;

public class ComputerConnection implements Serializable {

    /**
     * The port in which the app will send data
     */
    private static final int PORT = 8888;

    /**
     * The IP address
     */
    private String ip;

    /**
     * The socket used to comunicate with the PC
     */
    private Socket socket;

    /**
     * A private constructor
     */
    private ComputerConnection() {
        // Nothing to put here
    }

    /**
     * Get a new instance of this class
     *
     * @return a new instance of this class
     */
    public static ComputerConnection getInstance() {
        return new ComputerConnection();
    }

    /**
     * Set IP address
     *
     * @param ip The IP address
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * Send data to PC via socket
     *
     * @param what What to send
     * @return true if the data has been correctly sent
     */
    public boolean send(String what) {
        if (ip == null) {
            Logger.print("No IP set!");

            return false;
        }

        Logger.print("Sending " + what);

        boolean error = false;

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        DataOutputStream dataOutputStream = null;

        try {
            socket = new Socket(ip, PORT);
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeUTF(what);
        } catch (IOException e) {
            error = true;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }

            if (dataOutputStream != null) {
                try {
                    dataOutputStream.close();
                } catch (IOException ignored) {
                }
            }
        }

        return !error;
    }
}
