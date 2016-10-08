package io.github.ohmylob.emojipicker.view;

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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;

public class AutoFitRecyclerView extends RecyclerView {

    public static final float TOOLBAR_ELEVATION = 14f;

    private GridLayoutManager manager;
    private int columnWidth = -1;

    public AutoFitRecyclerView(Context context) {
        super(context);
        init(context, null);
    }

    public AutoFitRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AutoFitRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static OnScrollListener getOnScrollListener(final Toolbar toolbar) {
        toolbar.setElevation(AutoFitRecyclerView.TOOLBAR_ELEVATION);

        return new OnScrollListener() {
            int verticalOffset;
            boolean scrollingUp;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (scrollingUp) {
                        if (verticalOffset > toolbar.getHeight()) {
                            toolbarAnimateHide();
                        } else {
                            toolbarAnimateShow();
                        }
                    } else {
                        if (toolbar.getTranslationY() < toolbar.getHeight() * -0.6 && verticalOffset > toolbar.getHeight()) {
                            toolbarAnimateHide();
                        } else {
                            toolbarAnimateShow();
                        }
                    }
                }
            }

            @Override
            public final void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                verticalOffset += dy;
                scrollingUp = dy > 0;
                int toolbarYOffset = (int) (dy - toolbar.getTranslationY());
                toolbar.animate().cancel();
                if (scrollingUp) {
                    if (toolbarYOffset < toolbar.getHeight()) {
                        if (verticalOffset > toolbar.getHeight()) {
                            toolbarSetElevation();
                        }
                        toolbar.setTranslationY(-toolbarYOffset);
                    } else {
                        toolbarSetElevation();
                        toolbar.setTranslationY(-toolbar.getHeight());
                    }
                } else {
                    if (toolbarYOffset < 0) {
                        if (verticalOffset <= 0) {
                            toolbarSetElevation();
                        }
                        toolbar.setTranslationY(0);
                    } else {
                        if (verticalOffset > toolbar.getHeight()) {
                            toolbarSetElevation();
                        }
                        toolbar.setTranslationY(-toolbarYOffset);
                    }
                }
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            private void toolbarSetElevation() {
                toolbar.setElevation(TOOLBAR_ELEVATION);
            }

            private void toolbarAnimateShow() {
                toolbar.animate()
                        .translationY(0)
                        .setInterpolator(new LinearInterpolator())
                        .setDuration(180)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                toolbarSetElevation();
                            }
                        });
            }

            private void toolbarAnimateHide() {
                toolbar.animate()
                        .translationY(-toolbar.getHeight())
                        .setInterpolator(new LinearInterpolator())
                        .setDuration(180)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                toolbarSetElevation();
                            }
                        });
            }
        };
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            int[] attrsArray = {
                    android.R.attr.columnWidth
            };
            TypedArray array = context.obtainStyledAttributes(attrs, attrsArray);
            columnWidth = array.getDimensionPixelSize(0, -1);
            array.recycle();
        }

        manager = new GridLayoutManager(getContext(), 3);
        setLayoutManager(manager);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        if (columnWidth > 0) {
            int spanCount = Math.max(1, getMeasuredWidth() / columnWidth);
            manager.setSpanCount(spanCount);
        }
    }
}