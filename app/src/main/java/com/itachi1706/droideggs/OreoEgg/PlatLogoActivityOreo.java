/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.itachi1706.droideggs.OreoEgg;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.itachi1706.droideggs.OreoEgg.EasterEgg.octo.Ocquarium;
import com.itachi1706.droideggs.R;

/**
 * Created by Kenneth on 2/9/2017.
 * for com.itachi1706.droideggs.OreoEgg in DroidEggs
 */
@TargetApi(21)
public class PlatLogoActivityOreo extends AppCompatActivity {
    public static final boolean REVEAL_THE_NAME = false;
    public static final boolean FINISH = false;

    FrameLayout mLayout;
    int mTapCount;
    int mKeyCount;
    PathInterpolator mInterpolator = new PathInterpolator(0f, 0f, 0.5f, 1f);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLayout = new FrameLayout(this);
        setContentView(mLayout);
    }

    @Override
    public void onAttachedToWindow() {
        final DisplayMetrics dm = getResources().getDisplayMetrics();
        final float dp = dm.density;
        final int size = (int)
                (Math.min(Math.min(dm.widthPixels, dm.heightPixels), 600*dp) - 100*dp);

        final ImageView im = new ImageView(this);
        final int pad = (int)(40*dp);
        im.setPadding(pad, pad, pad, pad);
        im.setTranslationZ(20);
        im.setScaleX(0.5f);
        im.setScaleY(0.5f);
        im.setAlpha(0f);

        im.setBackground(new RippleDrawable(
                ColorStateList.valueOf(0xFFFFFFFF),
                getDrawable(R.drawable.oreo_platlogo),
                null));
//        im.setOutlineProvider(new ViewOutlineProvider() {
//            @Override
//            public void getOutline(View view, Outline outline) {
//                outline.setOval(0, 0, view.getWidth(), view.getHeight());
//            }
//        });
        im.setClickable(true);
        im.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                im.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (mTapCount < 5) return false;

                        if (REVEAL_THE_NAME) {
                            final Drawable overlay = getDrawable(
                                    R.drawable.ndp_platlogo_n);
                            overlay.setBounds(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
                            im.getOverlay().clear();
                            im.getOverlay().add(overlay);
                            overlay.setAlpha(0);
                            ObjectAnimator.ofInt(overlay, "alpha", 0, 255)
                                    .setDuration(500)
                                    .start();
                        }

                        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        if (pref.getLong("O_EGG_MODE", 0) == 0){
                            // For posterity: the moment this user unlocked the easter egg
                            pref.edit().putLong("O_EGG_MODE", System.currentTimeMillis()).apply();
                        }
                        im.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Intent octo = new Intent(PlatLogoActivityOreo.this, Ocquarium.class);
                                    startActivity(octo);
                                } catch (ActivityNotFoundException ex) {
                                    Log.e("PlatLogoActivity", "No more eggs.");
                                }
                                if (FINISH) finish();
                            }
                        });
                        return true;
                    }
                });
                mTapCount++;
            }
        });

        // Enable hardware keyboard input for TV compatibility.
        im.setFocusable(true);
        im.requestFocus();
        im.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode != KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                    ++mKeyCount;
                    if (mKeyCount > 2) {
                        if (mTapCount > 5) {
                            im.performLongClick();
                        } else {
                            im.performClick();
                        }
                    }
                    return true;
                } else {
                    return false;
                }
            }
        });

        mLayout.addView(im, new FrameLayout.LayoutParams(size, size, Gravity.CENTER));

        im.animate().scaleX(1f).scaleY(1f).alpha(1f)
                .setInterpolator(mInterpolator)
                .setDuration(500)
                .setStartDelay(800)
                .start();
    }
}