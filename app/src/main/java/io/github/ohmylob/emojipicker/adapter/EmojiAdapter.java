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
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.github.ohmylob.emojipicker.R;
import io.github.ohmylob.emojipicker.activity.MainActivity;
import io.github.ohmylob.emojipicker.clipboard.ClipBoard;

public class EmojiAdapter extends RecyclerView.Adapter<EmojiAdapter.ViewHolder> {

    public final String[] emojis;

    private final MainActivity activity;
    private final boolean userHasSkippedSetup;

    public EmojiAdapter(Activity activity, String[] emojis, boolean userHasSkippedSetup) {
        this.activity = (MainActivity) activity;
        this.emojis = emojis;
        this.userHasSkippedSetup = userHasSkippedSetup;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.emoji_grid_view_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final String emoji = emojis[position];

        final View rootView = holder.itemView;
        final TextView emojiTextView = holder.emojiTextView;

        emojiTextView.setText(emoji);

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!userHasSkippedSetup) {
                    if (!activity.computerConnection.send(emoji)) {
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
                    ClipBoard.setClipboard(activity, emoji);
                }

                Snackbar.make(activity.findViewById(android.R.id.content), userHasSkippedSetup
                        ? R.string.text_copied : R.string.snackbar_text, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return emojis.length;
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView emojiTextView;

        protected ViewHolder(View itemView) {
            super(itemView);
            this.emojiTextView = (TextView) itemView.findViewById(R.id.emoji);
        }
    }
}