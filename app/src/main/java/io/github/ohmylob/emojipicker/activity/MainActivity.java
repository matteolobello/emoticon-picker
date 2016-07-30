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

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import io.github.ohmylob.emojifromdroid.R;
import io.github.ohmylob.emojipicker.adapter.EmojiAdapter;
import io.github.ohmylob.emojipicker.connection.ComputerConnection;
import io.github.ohmylob.emojipicker.connection.ConnectionUtils;
import io.github.ohmylob.emojipicker.connection.PortScanner;
import io.github.ohmylob.emojipicker.debug.Logger;
import io.github.ohmylob.emojipicker.util.ScreenMath;
import io.github.ohmylob.emojipicker.view.AutofitRecyclerView;
import io.github.ohmylob.shared.emoji.Emojis;

public class MainActivity extends AppCompatActivity {

    /**
     * Used to connect the phone to the computer via Socket
     */
    public ComputerConnection computerConnection;

    /**
     * Used for portscanning the current network
     */
    public PortScanner portScanner;

    /**
     * The View shown when the phone is pairing with the PC
     */
    public View pairingWithPc;

    /**
     * The View shown when we couldn't find any PC
     */
    public View noPcFound;

    /**
     * The grid RecyclerView where are Emojis are displayed
     */
    public RecyclerView emojiRecyclerView;

    /**
     * The app Toolbar
     */
    private Toolbar toolbar;

    /**
     * The progressbar shown while we're portscanning
     */
    private ProgressBar progressBar;

    /**
     * A View wrapper that contains the RecyclerView
     */
    private View emojiGridViewWrapper;

    /**
     * The Button shown when no PC has been found
     */
    private View retryView;

    /**
     * The GoogleApiClient used to connect the app to Android Wear
     */
    private GoogleApiClient googleApiClient;

    /**
     * The RecyclerView adapter
     */
    private EmojiAdapter emojiAdapter;

    /**
     * A boolean used to check if user has skipped the setup
     */
    private boolean userHasSkippedSetup;

    /**
     * The Runnable run on UI thread that starts animation and setup other UI elements
     */
    private Runnable setupEmojis = new Runnable() {
        @Override
        public void run() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    View decor = getWindow().getDecorView();
                    decor.setSystemUiVisibility(0);
                }

                getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
            }

            Animation fadeOut = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_out);
            fadeOut.setDuration(400);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    final int fourDp = ScreenMath.dpToPx(4);

                    TypedArray styledAttributes = getTheme().obtainStyledAttributes(
                            new int[]{android.R.attr.actionBarSize});
                    final int toolBarHeight = (int) styledAttributes.getDimension(0, 0);

                    pairingWithPc.setVisibility(View.GONE);
                    emojiGridViewWrapper.setVisibility(View.VISIBLE);
                    emojiRecyclerView.setVisibility(View.INVISIBLE);

                    toolbar.setVisibility(View.VISIBLE);
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) emojiRecyclerView.getLayoutParams();
                        layoutParams.topMargin = toolBarHeight + ScreenMath.dpToPx(12);
                        emojiRecyclerView.setLayoutParams(layoutParams);
                    }
                    setSupportActionBar(toolbar);

                    Animation slideInToolbarAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_from_top);
                    slideInToolbarAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            emojiAdapter = new EmojiAdapter(MainActivity.this, Emojis.EMOJIS, userHasSkippedSetup);

                            emojiRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
                                @Override
                                public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                                    int margin = ScreenMath.dpToPx(4);
                                    outRect.set(margin, margin, margin, margin);
                                }
                            });
                            emojiRecyclerView.setAdapter(emojiAdapter);
                            emojiRecyclerView.setPadding(fourDp,
                                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                                            ? toolBarHeight + fourDp
                                            : 0,
                                    fourDp,
                                    fourDp);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                emojiRecyclerView.setOnScrollListener(AutofitRecyclerView.getOnScrollListener(toolbar));
                            }
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
                            fadeIn.setDuration(400);
                            fadeIn.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {
                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    emojiRecyclerView.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {
                                }
                            });
                            emojiRecyclerView.startAnimation(fadeIn);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });

                    toolbar.setTitleTextColor(Color.WHITE);
                    toolbar.setTitle(R.string.app_name);
                    toolbar.startAnimation(slideInToolbarAnimation);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            pairingWithPc.startAnimation(fadeOut);

            if (!userHasSkippedSetup) {
                Snackbar.make(findViewById(android.R.id.content), R.string.paired, Snackbar.LENGTH_SHORT).show();

                if (isAndroidWearAppInstalled()) {
                    openAppOnWear();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.whiteStatusBarColor));
        }

        if (!ConnectionUtils.isConnectedToWif(getApplicationContext())) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.no_wifi_title)
                    .setMessage(R.string.no_wifi_message)
                    .setPositiveButton(R.string.continue_anyway, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            userHasSkippedSetup = true;

                            setup();
                        }
                    })
                    .setNeutralButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .show();
            return;
        }

        setup();
    }

    /**
     * The method in which we setup the application
     */
    private void setup() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        googleApiClient.connect();

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        noPcFound = findViewById(R.id.no_pc_found_wrapper);
        pairingWithPc = findViewById(R.id.pairing_pc_wrapper);
        retryView = findViewById(R.id.retry);

        emojiRecyclerView = (RecyclerView) findViewById(R.id.emoji_recycler_view);
        emojiGridViewWrapper = findViewById(R.id.recycler_view_wrapper);

        computerConnection = ComputerConnection.getInstance();
        portScanner = PortScanner.getInstance();

        progressBar.getProgressDrawable().setColorFilter(
                Color.parseColor("#69F0AE"), android.graphics.PorterDuff.Mode.SRC_IN);
        progressBar.getIndeterminateDrawable().setColorFilter(
                Color.parseColor("#4CAF50"), android.graphics.PorterDuff.Mode.SRC_IN);

        retryView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recreateWithFade();
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!portScanner.isFinished() && !userHasSkippedSetup) {
                    Snackbar.make(findViewById(android.R.id.content), R.string.use_the_basic_feature_instead, Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.yes, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    userHasSkippedSetup = true;
                                    portScanner.finish();

                                    runOnUiThread(setupEmojis);
                                }
                            }).show();
                }
            }
        }, 2500);

        if (!userHasSkippedSetup) {
            portScanner.startPortScanningAsync(this, new PortScanner.OnAvailableIpFound() {
                @Override
                public boolean onAvailableIp(String ip) {
                    computerConnection.setIp(ip);

                    if (computerConnection.send("Connected to: " + Build.MODEL)) {
                        runOnUiThread(setupEmojis);

                        return true;
                    }

                    return false;
                }
            });
        } else {
            runOnUiThread(setupEmojis);
        }
    }

    /**
     * Restart the application with a fade animation
     */
    private void recreateWithFade() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * Open EmojiPicker Android Wear app
     */
    private void openAppOnWear() {
        Wearable.NodeApi.getConnectedNodes(googleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(@NonNull NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                for (Node node : getConnectedNodesResult.getNodes()) {
                    Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), "/start/MainActivity", arrayToByteArray(Emojis.EMOJIS))
                            .setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                                @Override
                                public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
                                    if (!sendMessageResult.getStatus().isSuccess()) {
                                        Logger.print("Google API failed to send message with status code: "
                                                + sendMessageResult.getStatus().getStatusCode());
                                    }
                                }
                            });
                }
            }
        });
    }

    /**
     * Convert a String array to byte array
     *
     * @param list The String array
     * @return the byte array
     */
    private byte[] arrayToByteArray(String[] list) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        for (String element : list) {
            try {
                dataOutputStream.writeUTF(element);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Check if the Android Wear app is present on phone
     *
     * @return true if app is present
     */
    private boolean isAndroidWearAppInstalled() {
        PackageManager packageManager = getApplicationContext().getPackageManager();
        try {
            packageManager.getPackageInfo("com.google.android.wearable.app", PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_setup) {
            if (userHasSkippedSetup) {
                recreateWithFade();
            } else {
                Snackbar.make(emojiRecyclerView, R.string.already_connected, Snackbar.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}
