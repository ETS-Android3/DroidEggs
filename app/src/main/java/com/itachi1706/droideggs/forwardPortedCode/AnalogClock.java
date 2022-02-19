/*
 * Copyright (C) 2006 The Android Open Source Project
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

package com.itachi1706.droideggs.forwardPortedCode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.RemoteViews.RemoteView;

import androidx.annotation.InspectableProperty;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.itachi1706.droideggs.R;
import com.itachi1706.droideggs.compat.ScreenMetricsCompat;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Formatter;
import java.util.Locale;

/**
 * This widget display an analogic clock with two hands for hours and
 * minutes.
 *
 * This widget has been deprecated on Android and used only for Android S's egg
 *
 * @attr ref android.R.styleable#AnalogClock_dial
 * @attr ref android.R.styleable#AnalogClock_hand_hour
 * @attr ref android.R.styleable#AnalogClock_hand_minute
 * @attr ref android.R.styleable#AnalogClock_hand_second
 * @attr ref android.R.styleable#AnalogClock_timeZone
 */
@RemoteView
@RequiresApi(api = Build.VERSION_CODES.Q)
public class AnalogClock extends View {
    private static final String LOG_TAG = "AnalogClock";

    /** How many times per second that the seconds hand advances. */
    private final int mSecondsHandFps;

    private Clock mClock;
    @Nullable
    private ZoneId mTimeZone;

    private Drawable mHourHand;
    private final TintInfo mHourHandTintInfo = new TintInfo();
    private Drawable mMinuteHand;
    private final TintInfo mMinuteHandTintInfo = new TintInfo();
    @Nullable
    private Drawable mSecondHand;
    private final TintInfo mSecondHandTintInfo = new TintInfo();
    private Drawable mDial;
    private final TintInfo mDialTintInfo = new TintInfo();

    private int mDialWidth;
    private int mDialHeight;

    private boolean mVisible;

    private float mSeconds;
    private float mMinutes;
    private float mHour;
    private boolean mChanged;

    private Context mContext;

    public AnalogClock(Context context) {
        this(context, null);
    }

    public AnalogClock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnalogClock(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AnalogClock(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        mContext = context;
        mSecondsHandFps = 1; // Default Value

//        final TypedArray a = context.obtainStyledAttributes(
//                attrs, com.android.internal.R.styleable.AnalogClock, defStyleAttr, defStyleRes);
//        saveAttributeDataForStyleable(context, com.android.internal.R.styleable.AnalogClock,
//                attrs, a, defStyleAttr, defStyleRes);

        mDial = context.getDrawable(R.drawable.clock_dial);

        if (mDialTintInfo.mHasTintList || mDialTintInfo.mHasTintBlendMode) {
            mDial = mDialTintInfo.apply(mDial);
        }

        mHourHand = context.getDrawable(R.drawable.clock_hand_hour);

        if (mHourHandTintInfo.mHasTintList || mHourHandTintInfo.mHasTintBlendMode) {
            mHourHand = mHourHandTintInfo.apply(mHourHand);
        }

        mMinuteHand = context.getDrawable(R.drawable.clock_hand_minute);

        if (mMinuteHandTintInfo.mHasTintList || mMinuteHandTintInfo.mHasTintBlendMode) {
            mMinuteHand = mMinuteHandTintInfo.apply(mMinuteHand);
        }

//        mSecondHand = a.getDrawable(android.R.styleable.AnalogClock_hand_second);
        mSecondHand = null; // context.getDrawable(R.drawable.clock_hand_minute); // Use minute here as we wont use it anw

        if (mSecondHandTintInfo.mHasTintList || mSecondHandTintInfo.mHasTintBlendMode) {
            mSecondHand = mSecondHandTintInfo.apply(mSecondHand);
        }

        mTimeZone = null; //toZoneId(a.getString(com.android.internal.R.styleable.AnalogClock_timeZone));
        createClock();

//        a.recycle();

        mDialWidth = mDial.getIntrinsicWidth();
        mDialHeight = mDial.getIntrinsicHeight();
    }

    /** Sets the dial of the clock to the specified Icon. */
    public void setDial(@NonNull Icon icon) {
        mDial = icon.loadDrawable(getContext());
        mDialWidth = mDial.getIntrinsicWidth();
        mDialHeight = mDial.getIntrinsicHeight();
        if (mDialTintInfo.mHasTintList || mDialTintInfo.mHasTintBlendMode) {
            mDial = mDialTintInfo.apply(mDial);
        }

        mChanged = true;
        invalidate();
    }

    /**
     * Applies a tint to the dial drawable.
     * <p>
     * Subsequent calls to {@link #setDial(Icon)} will
     * automatically mutate the drawable and apply the specified tint and tint
     * mode using {@link Drawable#setTintList(ColorStateList)}.
     *
     * @param tint the tint to apply, may be {@code null} to clear tint
     *
     * @attr ref android.R.styleable#AnalogClock_dialTint
     * @see #getDialTintList()
     * @see Drawable#setTintList(ColorStateList)
     */
    public void setDialTintList(@Nullable ColorStateList tint) {
        mDialTintInfo.mTintList = tint;
        mDialTintInfo.mHasTintList = true;

        mDial = mDialTintInfo.apply(mDial);
    }

    /**
     * @return the tint applied to the dial drawable
     * @attr ref android.R.styleable#AnalogClock_dialTint
     * @see #setDialTintList(ColorStateList)
     */
    @Nullable
    public ColorStateList getDialTintList() {
        return mDialTintInfo.mTintList;
    }

    /**
     * Specifies the blending mode used to apply the tint specified by
     * {@link #setDialTintList(ColorStateList)}} to the dial drawable.
     * The default mode is {@link BlendMode#SRC_IN}.
     *
     * @param blendMode the blending mode used to apply the tint, may be
     *                 {@code null} to clear tint
     * @attr ref android.R.styleable#AnalogClock_dialTintMode
     * @see #getDialTintBlendMode()
     * @see Drawable#setTintBlendMode(BlendMode)
     */
    public void setDialTintBlendMode(@Nullable BlendMode blendMode) {
        mDialTintInfo.mTintBlendMode = blendMode;
        mDialTintInfo.mHasTintBlendMode = true;

        mDial = mDialTintInfo.apply(mDial);
    }

    /**
     * @return the blending mode used to apply the tint to the dial drawable
     * @attr ref android.R.styleable#AnalogClock_dialTintMode
     * @see #setDialTintBlendMode(BlendMode)
     */
    @Nullable
    public BlendMode getDialTintBlendMode() {
        return mDialTintInfo.mTintBlendMode;
    }

    /** Sets the hour hand of the clock to the specified Icon. */
    public void setHourHand(@NonNull Icon icon) {
        mHourHand = icon.loadDrawable(getContext());
        if (mHourHandTintInfo.mHasTintList || mHourHandTintInfo.mHasTintBlendMode) {
            mHourHand = mHourHandTintInfo.apply(mHourHand);
        }

        mChanged = true;
        invalidate();
    }

    /**
     * Applies a tint to the hour hand drawable.
     * <p>
     * Subsequent calls to {@link #setHourHand(Icon)} will
     * automatically mutate the drawable and apply the specified tint and tint
     * mode using {@link Drawable#setTintList(ColorStateList)}.
     *
     * @param tint the tint to apply, may be {@code null} to clear tint
     *
     * @attr ref android.R.styleable#AnalogClock_hand_hourTint
     * @see #getHourHandTintList()
     * @see Drawable#setTintList(ColorStateList)
     */
    public void setHourHandTintList(@Nullable ColorStateList tint) {
        mHourHandTintInfo.mTintList = tint;
        mHourHandTintInfo.mHasTintList = true;

        mHourHand = mHourHandTintInfo.apply(mHourHand);
    }

    /**
     * @return the tint applied to the hour hand drawable
     * @attr ref android.R.styleable#AnalogClock_hand_hourTint
     * @see #setHourHandTintList(ColorStateList)
     */
    @Nullable
    public ColorStateList getHourHandTintList() {
        return mHourHandTintInfo.mTintList;
    }

    /**
     * Specifies the blending mode used to apply the tint specified by
     * {@link #setHourHandTintList(ColorStateList)}} to the hour hand drawable.
     * The default mode is {@link BlendMode#SRC_IN}.
     *
     * @param blendMode the blending mode used to apply the tint, may be
     *                 {@code null} to clear tint
     * @attr ref android.R.styleable#AnalogClock_hand_hourTintMode
     * @see #getHourHandTintBlendMode()
     * @see Drawable#setTintBlendMode(BlendMode)
     */
    public void setHourHandTintBlendMode(@Nullable BlendMode blendMode) {
        mHourHandTintInfo.mTintBlendMode = blendMode;
        mHourHandTintInfo.mHasTintBlendMode = true;

        mHourHand = mHourHandTintInfo.apply(mHourHand);
    }

    /**
     * @return the blending mode used to apply the tint to the hour hand drawable
     * @attr ref android.R.styleable#AnalogClock_hand_hourTintMode
     * @see #setHourHandTintBlendMode(BlendMode)
     */
    @Nullable
    public BlendMode getHourHandTintBlendMode() {
        return mHourHandTintInfo.mTintBlendMode;
    }

    /** Sets the minute hand of the clock to the specified Icon. */
    public void setMinuteHand(@NonNull Icon icon) {
        mMinuteHand = icon.loadDrawable(getContext());
        if (mMinuteHandTintInfo.mHasTintList || mMinuteHandTintInfo.mHasTintBlendMode) {
            mMinuteHand = mMinuteHandTintInfo.apply(mMinuteHand);
        }

        mChanged = true;
        invalidate();
    }

    /**
     * Applies a tint to the minute hand drawable.
     * <p>
     * Subsequent calls to {@link #setMinuteHand(Icon)} will
     * automatically mutate the drawable and apply the specified tint and tint
     * mode using {@link Drawable#setTintList(ColorStateList)}.
     *
     * @param tint the tint to apply, may be {@code null} to clear tint
     *
     * @attr ref android.R.styleable#AnalogClock_hand_minuteTint
     * @see #getMinuteHandTintList()
     * @see Drawable#setTintList(ColorStateList)
     */
    public void setMinuteHandTintList(@Nullable ColorStateList tint) {
        mMinuteHandTintInfo.mTintList = tint;
        mMinuteHandTintInfo.mHasTintList = true;

        mMinuteHand = mMinuteHandTintInfo.apply(mMinuteHand);
    }

    /**
     * @return the tint applied to the minute hand drawable
     * @attr ref android.R.styleable#AnalogClock_hand_minuteTint
     * @see #setMinuteHandTintList(ColorStateList)
     */
    @Nullable
    public ColorStateList getMinuteHandTintList() {
        return mMinuteHandTintInfo.mTintList;
    }

    /**
     * Specifies the blending mode used to apply the tint specified by
     * {@link #setMinuteHandTintList(ColorStateList)}} to the minute hand drawable.
     * The default mode is {@link BlendMode#SRC_IN}.
     *
     * @param blendMode the blending mode used to apply the tint, may be
     *                 {@code null} to clear tint
     * @attr ref android.R.styleable#AnalogClock_hand_minuteTintMode
     * @see #getMinuteHandTintBlendMode()
     * @see Drawable#setTintBlendMode(BlendMode)
     */
    public void setMinuteHandTintBlendMode(@Nullable BlendMode blendMode) {
        mMinuteHandTintInfo.mTintBlendMode = blendMode;
        mMinuteHandTintInfo.mHasTintBlendMode = true;

        mMinuteHand = mMinuteHandTintInfo.apply(mMinuteHand);
    }

    /**
     * @return the blending mode used to apply the tint to the minute hand drawable
     * @attr ref android.R.styleable#AnalogClock_hand_minuteTintMode
     * @see #setMinuteHandTintBlendMode(BlendMode)
     */
    @Nullable
    public BlendMode getMinuteHandTintBlendMode() {
        return mMinuteHandTintInfo.mTintBlendMode;
    }

    /**
     * Sets the second hand of the clock to the specified Icon, or hides the second hand if it is
     * null.
     */
    public void setSecondHand(@Nullable Icon icon) {
        mSecondHand = icon == null ? null : icon.loadDrawable(getContext());
        if (mSecondHandTintInfo.mHasTintList || mSecondHandTintInfo.mHasTintBlendMode) {
            mSecondHand = mSecondHandTintInfo.apply(mSecondHand);
        }
        // Re-run the tick runnable immediately as the presence or absence of a seconds hand affects
        // the next time we need to tick the clock.
        mTick.run();

        mChanged = true;
        invalidate();
    }

    /**
     * Applies a tint to the second hand drawable.
     * <p>
     * Subsequent calls to {@link #setSecondHand(Icon)} will
     * automatically mutate the drawable and apply the specified tint and tint
     * mode using {@link Drawable#setTintList(ColorStateList)}.
     *
     * @param tint the tint to apply, may be {@code null} to clear tint
     *
     * @attr ref android.R.styleable#AnalogClock_hand_secondTint
     * @see #getSecondHandTintList()
     * @see Drawable#setTintList(ColorStateList)
     */
    public void setSecondHandTintList(@Nullable ColorStateList tint) {
        mSecondHandTintInfo.mTintList = tint;
        mSecondHandTintInfo.mHasTintList = true;

        mSecondHand = mSecondHandTintInfo.apply(mSecondHand);
    }

    /**
     * @return the tint applied to the second hand drawable
     * @attr ref android.R.styleable#AnalogClock_hand_secondTint
     * @see #setSecondHandTintList(ColorStateList)
     */
    @Nullable
    public ColorStateList getSecondHandTintList() {
        return mSecondHandTintInfo.mTintList;
    }

    /**
     * Specifies the blending mode used to apply the tint specified by
     * {@link #setSecondHandTintList(ColorStateList)}} to the second hand drawable.
     * The default mode is {@link BlendMode#SRC_IN}.
     *
     * @param blendMode the blending mode used to apply the tint, may be
     *                 {@code null} to clear tint
     * @attr ref android.R.styleable#AnalogClock_hand_secondTintMode
     * @see #getSecondHandTintBlendMode()
     * @see Drawable#setTintBlendMode(BlendMode)
     */
    public void setSecondHandTintBlendMode(@Nullable BlendMode blendMode) {
        mSecondHandTintInfo.mTintBlendMode = blendMode;
        mSecondHandTintInfo.mHasTintBlendMode = true;

        mSecondHand = mSecondHandTintInfo.apply(mSecondHand);
    }

    /**
     * @return the blending mode used to apply the tint to the second hand drawable
     * @attr ref android.R.styleable#AnalogClock_hand_secondTintMode
     * @see #setSecondHandTintBlendMode(BlendMode)
     */
    @Nullable
    public BlendMode getSecondHandTintBlendMode() {
        return mSecondHandTintInfo.mTintBlendMode;
    }

    /**
     * Indicates which time zone is currently used by this view.
     *
     * @return The ID of the current time zone or null if the default time zone,
     *         as set by the user, must be used
     *
     * @see java.util.TimeZone
     * @see java.util.TimeZone#getAvailableIDs()
     * @see #setTimeZone(String)
     */
    @Nullable
    public String getTimeZone() {
        ZoneId zoneId = mTimeZone;
        return zoneId == null ? null : zoneId.getId();
    }

    /**
     * Sets the specified time zone to use in this clock. When the time zone
     * is set through this method, system time zone changes (when the user
     * sets the time zone in settings for instance) will be ignored.
     *
     * @param timeZone The desired time zone's ID as specified in {@link java.util.TimeZone}
     *                 or null to user the time zone specified by the user
     *                 (system time zone)
     *
     * @see #getTimeZone()
     * @see java.util.TimeZone#getAvailableIDs()
     * @see java.util.TimeZone#getTimeZone(String)
     *
     * @attr ref android.R.styleable#AnalogClock_timeZone
     */
    public void setTimeZone(@Nullable String timeZone) {
        mTimeZone = toZoneId(timeZone);

        createClock();
        onTimeChanged();
    }

    @Override
    public void onVisibilityAggregated(boolean isVisible) {
        super.onVisibilityAggregated(isVisible);

        if (isVisible) {
            onVisible();
        } else {
            onInvisible();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter filter = new IntentFilter();

        if (!mReceiverAttached) {
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);

            // OK, this is gross but needed. This class is supported by the
            // remote views mechanism and as a part of that the remote views
            // can be inflated by a context for another user without the app
            // having interact users permission - just for loading resources.
            // For example, when adding widgets from a user profile to the
            // home screen. Therefore, we register the receiver as the current
            // user not the one the context is for.
            getContext().registerReceiver(mIntentReceiver, filter, null, getHandler());
//            getContext().registerReceiverAsUser(mIntentReceiver,
//                    android.os.Process.myUserHandle(), filter, null, getHandler());
            mReceiverAttached = true;
        }

        // NOTE: It's safe to do these after registering the receiver since the receiver always runs
        // in the main thread, therefore the receiver can't run before this method returns.

        // The time zone may have changed while the receiver wasn't registered, so update the clock.
        createClock();

        // Make sure we update to the current time
        onTimeChanged();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mReceiverAttached) {
            getContext().unregisterReceiver(mIntentReceiver);
            mReceiverAttached = false;
        }
        super.onDetachedFromWindow();
    }

    private void onVisible() {
        if (!mVisible) {
            mVisible = true;
            mTick.run();
        }

    }

    private void onInvisible() {
        if (mVisible) {
            removeCallbacks(mTick);
            mVisible = false;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize =  MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize =  MeasureSpec.getSize(heightMeasureSpec);

        float hScale = 1.0f;
        float vScale = 1.0f;

        if (widthMode != MeasureSpec.UNSPECIFIED && widthSize < mDialWidth) {
            hScale = (float) widthSize / (float) mDialWidth;
        }

        if (heightMode != MeasureSpec.UNSPECIFIED && heightSize < mDialHeight) {
            vScale = (float )heightSize / (float) mDialHeight;
        }

        float scale = Math.min(hScale, vScale);

        setMeasuredDimension(resolveSizeAndState((int) (mDialWidth * scale), widthMeasureSpec, 0),
                resolveSizeAndState((int) (mDialHeight * scale), heightMeasureSpec, 0));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mChanged = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        boolean changed = mChanged;
        if (changed) {
            mChanged = false;
        }

        int availableWidth = getRight() - getLeft();
        int availableHeight = getBottom() - getTop();

        int x = availableWidth / 2;
        int y = availableHeight / 2;

        final Drawable dial = mDial;
        int w = dial.getIntrinsicWidth();
        int h = dial.getIntrinsicHeight();

        boolean scaled = false;

        if (availableWidth < w || availableHeight < h) {
            scaled = true;
            float scale = Math.min((float) availableWidth / (float) w,
                    (float) availableHeight / (float) h);
            canvas.save();
            canvas.scale(scale, scale, x, y);
        }

        if (changed) {
            dial.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
        }
        dial.draw(canvas);

        canvas.save();
        canvas.rotate(mHour / 12.0f * 360.0f, x, y);
        final Drawable hourHand = mHourHand;
        if (changed) {
            w = hourHand.getIntrinsicWidth();
            h = hourHand.getIntrinsicHeight();
            hourHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
        }
        hourHand.draw(canvas);
        canvas.restore();

        canvas.save();
        canvas.rotate(mMinutes / 60.0f * 360.0f, x, y);

        final Drawable minuteHand = mMinuteHand;
        if (changed) {
            w = minuteHand.getIntrinsicWidth();
            h = minuteHand.getIntrinsicHeight();
            minuteHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
        }
        minuteHand.draw(canvas);
        canvas.restore();

        final Drawable secondHand = mSecondHand;
        if (secondHand != null && mSecondsHandFps > 0) {
            canvas.save();
            canvas.rotate(mSeconds / 60.0f * 360.0f, x, y);

            if (changed) {
                w = secondHand.getIntrinsicWidth();
                h = secondHand.getIntrinsicHeight();
                secondHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
            }
            secondHand.draw(canvas);
            canvas.restore();
        }

        if (scaled) {
            canvas.restore();
        }
    }

    /**
     * Return the current Instant to be used for drawing the clockface. Protected to allow
     * subclasses to override this to show a different time from the system clock.
     *
     * @return the Instant to be shown on the clockface
     */
    protected Instant now() {
        return mClock.instant();
    }

    protected void onTimeChanged() {
        Instant now = now();
        onTimeChanged(now.atZone(mClock.getZone()).toLocalTime(), now.toEpochMilli());
    }

    private void onTimeChanged(LocalTime localTime, long nowMillis) {
        float previousHour = mHour;
        float previousMinutes = mMinutes;

        float rawSeconds = localTime.getSecond() + localTime.getNano() / 1_000_000_000f;
        // We round the fraction of the second so that the seconds hand always occupies the same
        // n positions between two given numbers, where n is the number of ticks per second. This
        // ensures the second hand advances by a consistent distance despite our handler callbacks
        // occurring at inconsistent frequencies.
        mSeconds =
                mSecondsHandFps <= 0
                        ? rawSeconds
                        : Math.round(rawSeconds * mSecondsHandFps) / (float) mSecondsHandFps;
        mMinutes = localTime.getMinute() + mSeconds / 60.0f;
        mHour = localTime.getHour() + mMinutes / 60.0f;
        mChanged = true;

        // Update the content description only if the announced hours and minutes have changed.
        if ((int) previousHour != (int) mHour || (int) previousMinutes != (int) mMinutes) {
            updateContentDescription(nowMillis);
        }
    }

    /** Intent receiver for the time or time zone changing. */
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())) {
                createClock();
            }

            mTick.run();
        }
    };
    private boolean mReceiverAttached;

    private final Runnable mTick = new Runnable() {
        @Override
        public void run() {
            removeCallbacks(this);
            if (!mVisible) {
                return;
            }

            Instant now = now();
            ZonedDateTime zonedDateTime = now.atZone(mClock.getZone());
            LocalTime localTime = zonedDateTime.toLocalTime();

            long millisUntilNextTick;
            if (mSecondHand == null || mSecondsHandFps <= 0) {
                // If there's no second hand, then tick at the start of the next minute.
                //
                // This must be done with ZonedDateTime as opposed to LocalDateTime to ensure proper
                // handling of DST. Also note that because of leap seconds, it should not be assumed
                // that one minute == 60 seconds.
                Instant startOfNextMinute = zonedDateTime.plusMinutes(1).withSecond(0).toInstant();
                millisUntilNextTick = Duration.between(now, startOfNextMinute).toMillis();
                if (millisUntilNextTick <= 0) {
                    // This should never occur, but if it does, then just check the tick again in
                    // one minute to ensure we're always moving forward.
                    millisUntilNextTick = Duration.ofMinutes(1).toMillis();
                }
            } else {
                // If there is a seconds hand, then determine the next tick point based on the fps.
                //
                // How many milliseconds through the second we currently are.
                long millisOfSecond = Duration.ofNanos(localTime.getNano()).toMillis();
                // How many milliseconds there are between tick positions for the seconds hand.
                double millisPerTick = 1000 / (double) mSecondsHandFps;
                // How many milliseconds we are past the last tick position.
                long millisPastLastTick = Math.round(millisOfSecond % millisPerTick);
                // How many milliseconds there are until the next tick position.
                millisUntilNextTick = Math.round(millisPerTick - millisPastLastTick);
                // If we are exactly at the tick position, this could be 0 milliseconds due to
                // rounding. In this case, advance by the full amount of millis to the next
                // position.
                if (millisUntilNextTick <= 0) {
                    millisUntilNextTick = Math.round(millisPerTick);
                }
            }

            // Schedule a callback for when the next tick should occur.
            postDelayed(this, millisUntilNextTick);

            onTimeChanged(localTime, now.toEpochMilli());

            invalidate();
        }
    };

    private void createClock() {
        ZoneId zoneId = mTimeZone;
        if (zoneId == null) {
            mClock = Clock.systemDefaultZone();
        } else {
            mClock = Clock.system(zoneId);
        }
    }

    private void updateContentDescription(long timeMillis) {
        final int flags = DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR;
        String contentDescription =
                DateUtils.formatDateRange(
                                mContext,
                                new Formatter(new StringBuilder(50), Locale.getDefault()),
                                timeMillis /* startMillis */,
                                timeMillis /* endMillis */,
                                flags,
                                getTimeZone())
                        .toString();
        setContentDescription(contentDescription);
    }

    /**
     * Tries to parse a {@link ZoneId} from {@code timeZone}, returning null if it is null or there
     * is an error parsing.
     */
    @Nullable
    private static ZoneId toZoneId(@Nullable String timeZone) {
        if (timeZone == null) {
            return null;
        }

        try {
            return ZoneId.of(timeZone);
        } catch (DateTimeException e) {
            Log.w(LOG_TAG, "Failed to parse time zone from " + timeZone, e);
            return null;
        }
    }

    private final class TintInfo {
        boolean mHasTintList;
        @Nullable ColorStateList mTintList;
        boolean mHasTintBlendMode;
        @Nullable BlendMode mTintBlendMode;

        /**
         * Returns a mutated copy of {@code drawable} with tinting applied, or null if it's null.
         */
        @Nullable
        Drawable apply(@Nullable Drawable drawable) {
            if (drawable == null) return null;

            Drawable newDrawable = drawable.mutate();

            if (mHasTintList) {
                newDrawable.setTintList(mTintList);
            }

            if (mHasTintBlendMode) {
                newDrawable.setTintBlendMode(mTintBlendMode);
            }

            // All drawables should have the same state as the View itself.
            if (drawable.isStateful()) {
                newDrawable.setState(getDrawableState());
            }

            return newDrawable;
        }
    }
}