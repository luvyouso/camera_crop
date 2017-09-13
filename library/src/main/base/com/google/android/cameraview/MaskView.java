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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.view.View;

/**
 * Created by nguyentu on 9/11/17.
 */

public class MaskView extends View {

    private int mMaskWidth = Constants.DEFAULT_WIDTH_HEIGHT;

    private int mMaskHeight = Constants.DEFAULT_WIDTH_HEIGHT;

    private int mWidthScreen;

    private int mHeightScreen;

    private Paint mRectPaint;

    private int mLeftCropImage;
    private int mTopCropImage;
    private int mRightCropImage;
    private int mBottomCropImage;

    private float mXView;
    private float mYView;
    private int mPaddingBottom;

    public MaskView(Context context) {
        super(context);

        mRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRectPaint.setColor(ContextCompat.getColor(getContext(), R.color.border_camera));
        mRectPaint.setStyle(Paint.Style.FILL);
        mRectPaint.setAlpha(50);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mMaskHeight == 0 && mMaskWidth == 0) {
            return;
        }
        if (mMaskHeight == mHeightScreen || mMaskWidth == mWidthScreen) {
            return;
        }
        int x = (int) (getXView() - mMaskWidth / 2);
        int y = (int) (getYView() - mMaskHeight / 2);
        if (x < 0) {
            x = 0;
        } else if (getXView() > (mWidthScreen - mMaskWidth / 2)) {
            x = mWidthScreen - mMaskWidth;
        }
        if (y < 0) {
            y = 0;
        } else if (getYView() > (mHeightScreen - mPaddingBottom - Constants.PADDING_BOTTOM - mMaskHeight / 2)) {
            y = mHeightScreen - mPaddingBottom - Constants.PADDING_BOTTOM - mMaskHeight;
        }
        //top
        canvas.drawRect(0, 0, mWidthScreen, y, this.mRectPaint);
        //right
        canvas.drawRect(x + Constants.DEFAULT_WIDTH_HEIGHT, y, mWidthScreen, y + mMaskHeight,
                this.mRectPaint);//left top right bottom
        //bottom
        canvas.drawRect(0, y + Constants.DEFAULT_WIDTH_HEIGHT, mWidthScreen, mHeightScreen,
                this.mRectPaint);//left top right bottom
        //left
        canvas.drawRect(0, y, x, y + mMaskHeight, this.mRectPaint);

        setLeftCropImage(x);
        setTopCropImage(y);
        setRightCropImage(x + mMaskWidth);
        setBottomCropImage(y + mMaskHeight);
        super.onDraw(canvas);
    }

    public int getWidthLayout() {
        return mWidthScreen;
    }

    public void setWidthLayout(int widthLayout) {
        mWidthScreen = widthLayout;
    }

    public int getHeightLayout() {
        return mHeightScreen;
    }

    public void setHeightLayout(int heightLayout) {
        mHeightScreen = heightLayout;
    }

    public int getLeftCropImage() {
        return mLeftCropImage;
    }

    public void setLeftCropImage(int leftCropImage) {
        mLeftCropImage = leftCropImage;
    }

    public int getTopCropImage() {
        return mTopCropImage;
    }

    public void setTopCropImage(int topCropImage) {
        mTopCropImage = topCropImage;
    }

    public int getRightCropImage() {
        return mRightCropImage;
    }

    public void setRightCropImage(int rightCropImage) {
        mRightCropImage = rightCropImage;
    }

    public int getBottomCropImage() {
        return mBottomCropImage;
    }

    public void setBottomCropImage(int bottomCropImage) {
        mBottomCropImage = bottomCropImage;
    }

    public float getXView() {
        return mXView;
    }

    public void setXView(float XView) {
        mXView = XView;
    }

    public float getYView() {
        return mYView;
    }

    public void setYView(float YView) {
        mYView = YView;
    }

    public int getPaddingBottom() {
        return mPaddingBottom;
    }

    public void setPaddingBottom(int padding) {
        this.mPaddingBottom = padding;
    }
}
