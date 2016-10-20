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

import android.app.Activity;
import android.graphics.Typeface;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.github.ohmylob.emojipicker.R;
import io.github.ohmylob.emojipicker.activity.MainActivity;
import io.github.ohmylob.emojipicker.clipboard.ClipBoard;

public class EmoticonAdapter extends RecyclerView.Adapter<EmoticonAdapter.ViewHolder> {

    private final String[] emoticons;

    private final MainActivity activity;
    private final boolean userHasSkippedSetup;

    public EmoticonAdapter(Activity activity, String[] emoticons, boolean userHasSkippedSetup) {
        this.activity = (MainActivity) activity;
        this.emoticons = emoticons;
        this.userHasSkippedSetup = userHasSkippedSetup;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.emoticon_grid_view_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final String emoticon = emoticons[position];

        final View rootView = holder.itemView;
        final TextView emoticonTextView = holder.emoticonTextView;

        emoticonTextView.setText(emoticon);
        emoticonTextView.setTypeface(Typeface.createFromAsset(activity.getAssets(), "NotoSans-Regular.ttf"));

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!userHasSkippedSetup) {
                    if (!activity.computerConnection.send(emoticon)) {
                        Snackbar.make(activity.findViewById(android.R.id.content), R.string.error, Snackbar.LENGTH_SHORT)
                                .setAction(R.string.retry, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        activity.recreate();
                                    }
                                }).show();

                        return;
                    }
                } else {
                    ClipBoard.setClipboard(activity, emoticon);
                }

                Snackbar.make(activity.findViewById(android.R.id.content), userHasSkippedSetup
                        ? R.string.text_copied : R.string.snackbar_text, Snackbar.LENGTH_SHORT).show();
            }
        });

        rootView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Snackbar.make(activity.findViewById(android.R.id.content), R.string.text_copied, Snackbar.LENGTH_SHORT).show();

                ClipBoard.setClipboard(activity, emoticon);

                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return emoticons.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView emoticonTextView;

        ViewHolder(View itemView) {
            super(itemView);
            this.emoticonTextView = (TextView) itemView.findViewById(R.id.emoticon);
        }
    }
}