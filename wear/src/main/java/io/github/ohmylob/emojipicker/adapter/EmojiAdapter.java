package io.github.ohmylob.emojipicker.adapter;

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
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

import io.github.ohmylob.emojipicker.R;

public class EmojiAdapter extends RecyclerView.Adapter<EmojiAdapter.ViewHolder> {

    private final List<String> emojis;

    private GoogleApiClient googleApiClient;

    public EmojiAdapter(Context context, List<String> emojis) {
        this.emojis = emojis;

        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    public EmojiAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedLayout = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.wear_recyceler_view_item, parent, false);

        return new ViewHolder(inflatedLayout);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.emojiTextView.setText(emojis.get(position));
        holder.emojiTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Wearable.NodeApi.getConnectedNodes(googleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(@NonNull NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                        for (final Node node : getConnectedNodesResult.getNodes()) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Wearable.MessageApi.sendMessage(googleApiClient, node.getId(),
                                            "/emoji/clicked", emojis.get(position).getBytes()).await();
                                }
                            }).start();
                        }
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return emojis.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView emojiTextView;

        public ViewHolder(View emoji) {
            super(emoji);
            emojiTextView = (TextView) emoji.findViewById(R.id.emoji);
        }
    }
}
