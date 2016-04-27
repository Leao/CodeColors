package io.leao.codecolors.sample.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.Calendar;

public class SimpleTextClock extends TextView {
    private static final String sFormat12 = "h:mm:ss a";
    private static final String sFormat24 = "HH:mm:ss";

    private Calendar mTime = Calendar.getInstance();

    private boolean mAttached;

    private final Runnable mTicker = new Runnable() {
        public void run() {
            onTimeChanged();

            long now = SystemClock.uptimeMillis();
            long next = now + (1000 - now % 1000);

            getHandler().postAtTime(mTicker, next);
        }
    };

    public SimpleTextClock(Context context) {
        super(context);
        init();
    }

    public SimpleTextClock(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SimpleTextClock(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SimpleTextClock(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (!mAttached) {
            mAttached = true;

            mTicker.run();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mAttached) {
            getHandler().removeCallbacks(mTicker);

            mAttached = false;
        }
    }

    protected void onTimeChanged() {
        mTime.setTimeInMillis(System.currentTimeMillis());
        setText(DateFormat.format(sFormat12, mTime));
    }
}
