/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.scrolltricks;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ScrollView;

/**
 * A custom ScrollView that can accept a scroll listener.
 */
public class ObservableScrollView extends ScrollView {
    private Callbacks mCallbacks;
    int touchSlop;
    int infoHeight;
    int imageHeight;
    int imageFullHeight;

    public ObservableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOverScrollMode(OVER_SCROLL_NEVER);
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        infoHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, displayMetrics);
        imageFullHeight = displayMetrics.widthPixels * 3 / 2;//(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, displayMetrics);
        imageHeight = imageFullHeight / 3;

        this.post(new Runnable() {
            @Override
            public void run() {
                ObservableScrollView.this.scrollTo(0, imageFullHeight - imageHeight);
            }
        });
        setDescendantFocusability(ScrollView.FOCUS_BEFORE_DESCENDANTS);
    }


    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mCallbacks != null) {
//            this.t = t;
            if (t < imageFullHeight && oldt > imageFullHeight) { //stop fling
                MotionEvent upEvent = MotionEvent.obtain(
                        SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(), MotionEvent.ACTION_UP,
                        0, 0, 0);
                dispatchTouchEvent(upEvent);
                MotionEvent downevent = MotionEvent.obtain(
                        SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(),
                        MotionEvent.ACTION_DOWN, 0, 0, 0);
                dispatchTouchEvent(downevent);
                upEvent.recycle();
                downevent.recycle();
            }
            mCallbacks.onScrollChanged(t);
        }
    }

    int t;

    float startY;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mCallbacks != null) {
            switch (ev.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    startY = ev.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    break;
            }
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        t=getScrollY();
        if (mCallbacks != null) {
            switch (ev.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    mCallbacks.onDownMotionEvent();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    float diffY = ev.getY() - startY;

                    if (t < imageFullHeight - imageHeight) {
                        if (diffY > 0) {
                            smoothScrollTo(0, 0);
                            return true;
                        } else {
                            smoothScrollTo(0, imageFullHeight - imageHeight);
                            return true;
                        }
                    } else if (t >= imageFullHeight - imageHeight && t < imageFullHeight) {
                        if (diffY > 0) {
                            smoothScrollTo(0, 0);
                            return true;
                        } else {
                            smoothScrollTo(0, imageFullHeight);
                            return true;
                        }
                    } else {

                    }
                    mCallbacks.onUpOrCancelMotionEvent();
                    break;
            }
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public void fling(int velocityY) {
        if (getScrollY() > imageFullHeight)
            super.fling(velocityY);
    }

    @Override
    public int computeVerticalScrollRange() {
        return super.computeVerticalScrollRange();
    }

    public void setCallbacks(Callbacks listener) {
        mCallbacks = listener;
    }

    public static interface Callbacks {
        public void onScrollChanged(int scrollY);

        public void onDownMotionEvent();

        public void onUpOrCancelMotionEvent();
    }
}
