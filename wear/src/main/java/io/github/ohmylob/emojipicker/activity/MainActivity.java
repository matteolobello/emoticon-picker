package io.github.ohmylob.emojipicker.activity;

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

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.Arrays;
import java.util.List;

import io.github.ohmylob.emojipicker.R;
import io.github.ohmylob.emojipicker.adapter.EmoticonAdapter;
import io.github.ohmylob.shared.emoji.Emoticons;

public class MainActivity extends Activity {

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        googleApiClient.connect();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                List<String> emoticons = getIntent().getStringArrayListExtra("emoticons");

                if (emoticons == null) {
                    emoticons = Arrays.asList(Emoticons.EMOTICONS);
                }

                RecyclerView recyclerView = (RecyclerView) stub.findViewById(R.id.recycler_view);
                TextView openOnPhone = (TextView) stub.findViewById(R.id.open_on_phone);

                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

                openOnPhone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        launchActivityOnPhone();
                    }
                });

                if (emoticons.size() != 0) {
                    recyclerView.setVisibility(View.VISIBLE);
                    openOnPhone.setVisibility(View.GONE);
                    recyclerView.setAdapter(new EmoticonAdapter(getApplicationContext(), emoticons));
                } else {
                    launchActivityOnPhone();
                }
            }
        });
    }

    private void launchActivityOnPhone() {
        Wearable.NodeApi.getConnectedNodes(googleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(@NonNull NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                for (final Node node : getConnectedNodesResult.getNodes()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Wearable.MessageApi.sendMessage(googleApiClient, node.getId(),
                                    "/start/activity/on/phone", null).await();
                        }
                    }).start();
                }
            }
        });
    }
}
