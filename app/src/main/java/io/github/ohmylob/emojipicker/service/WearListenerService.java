package io.github.ohmylob.emojipicker.service;

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

import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.UnsupportedEncodingException;

import io.github.ohmylob.emojipicker.activity.MainActivity;
import io.github.ohmylob.emojipicker.connection.ComputerConnection;
import io.github.ohmylob.emojipicker.connection.PortScanner;

public class WearListenerService extends WearableListenerService {

    private ComputerConnection computerConnection;

    private String rightIp;

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        if (messageEvent.getPath().equals("/emoticon/clicked")) {
            Log.d("WearListenerService", "onMessageReceived");

            if (computerConnection == null) {
                computerConnection = ComputerConnection.getInstance();
            }

            if (rightIp != null) {
                if (computerConnection.send("Connected to: " + Build.MODEL)) {
                    try {
                        computerConnection.send(new String(messageEvent.getData(), "UTF-8"));

                        return;
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }

            PortScanner.getInstance().startPortScanningAsync(getApplicationContext(), new PortScanner.OnAvailableIpFound() {
                @Override
                public boolean onAvailableIp(String ip) {
                    computerConnection.setIp(ip);

                    if (computerConnection.send("Connected to: " + Build.MODEL)) {
                        rightIp = ip;

                        try {
                            computerConnection.send(new String(messageEvent.getData(), "UTF-8"));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        return true;
                    }

                    return false;
                }
            });
        } else if (messageEvent.getPath().equals("/start/activity/on/phone")) {
            getApplicationContext().startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
    }
}
