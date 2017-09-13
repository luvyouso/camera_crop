/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.google.android.cameraview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

@TargetApi(14)
public class FocusMarkerLayout extends FrameLayout {

    private FrameLayout mFocusMarkerContainer;

    private int mMaxWidth;

    private int mMaxHeight;

    private int mPositionX;

    private int mPositionY;

    public FocusMarkerLayout(@NonNull Context context) {
        this(context, null);
    }

    public FocusMarkerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(getContext()).inflate(R.layout.layout_focus_marker, this);

        mFocusMarkerContainer = (FrameLayout) findViewById(R.id.focusMarkerContainer);
//        mFocusMarkerContainer.setAlpha(0);
    }

    public void updateWidthHeight(int width, int height) {
//        mFocusMarkerContainer.
    }

    public void focus(float mx, float my, int paddingBottom) {

        mPositionX = (int) (mx - Constants.DEFAULT_WIDTH_HEIGHT / 2);
        mPositionY = (int) (my - Constants.DEFAULT_WIDTH_HEIGHT / 2);
        if (mPositionX < 0) {
            mPositionX = 0;
        } else if (mx > (mMaxWidth - Constants.DEFAULT_WIDTH_HEIGHT / 2)) {
            mPositionX = mMaxWidth - Constants.DEFAULT_WIDTH_HEIGHT;
        }

        if (mPositionY < 0) {
            mFocusMarkerContainer.setTranslationY(0);
            mPositionY = 0;
        } else if (my > (mMaxHeight - paddingBottom - Constants.PADDING_BOTTOM - Constants.DEFAULT_WIDTH_HEIGHT / 2)) {
            mPositionY = mMaxHeight - paddingBottom - Constants.PADDING_BOTTOM - Constants.DEFAULT_WIDTH_HEIGHT;
        }

        mFocusMarkerContainer.setTranslationX(mPositionX);
        mFocusMarkerContainer.setTranslationY(mPositionY);

        mFocusMarkerContainer.animate().setListener(null).cancel();
        mFocusMarkerContainer.setScaleX(1.36f);
        mFocusMarkerContainer.setScaleY(1.36f);
        mFocusMarkerContainer.setAlpha(1f);

        mFocusMarkerContainer.animate().scaleX(1).scaleY(1).setStartDelay(0).setDuration(330)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mFocusMarkerContainer.animate().alpha(100).setStartDelay(750).setDuration
                                (800).setListener(null).start();
                    }
                }).start();
    }

    public int getMaxWidth() {
        return mMaxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        mMaxWidth = maxWidth;
    }

    public int getMaxHeight() {
        return mMaxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        mMaxHeight = maxHeight;
    }

    public int getPositionX() {
        return mPositionX;
    }

    public void setPositionX(int x) {
        this.mPositionX = x;
    }

    public int getPositionY() {
        return mPositionY;
    }

    public void setPositionY(int y) {
        this.mPositionY = y;
    }
}
