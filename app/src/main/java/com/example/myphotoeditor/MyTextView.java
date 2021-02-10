package com.example.myphotoeditor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;

public class MyTextView extends androidx.appcompat.widget.AppCompatTextView implements GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {

    private ConstraintLayout layout;
    private boolean initialDraw = true;
    private float xDown, yDown;
    private GestureDetector mGestureDetector;
    private Activity mActivity;
    private Context mContext;
    private int SIZE, COLOR;
    private String TEXT;

    private void init(Context context) {
        mActivity = (Activity) context;
        mContext = context;
        layout = mActivity.findViewById(R.id.activity);
        mGestureDetector = new GestureDetector(context, this);
    }

    public MyTextView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public MyTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MyTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (initialDraw) {
            setX(layout.getWidth() / 2f - getWidth() / 2f);
            setY(layout.getHeight() / 2f - getHeight() / 2f);

            initialDraw = false;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        mGestureDetector.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                xDown = event.getX();
                yDown = event.getY();
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                float xMoved = event.getX();
                float yMoved = event.getY();

                float distanceX = xMoved - xDown;
                float distanceY = yMoved - yDown;

                this.setX(this.getX() + distanceX);
                this.setY(this.getY() + distanceY);

                break;
            }
        }

        postInvalidate();

        return true;
    }

    public void setParams(String text, int color, int size) {
        this.SIZE = size;
        this.COLOR = color;
        this.TEXT = text;
    }

    public int getSIZE() {
        return SIZE;
    }

    public String getTEXT() {
        return TEXT;
    }

    public int getCOLOR() {
        return COLOR;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {}

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) { return false; }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(100);
        }
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        vibrate();
        AddTextDialog dialog = new AddTextDialog(mContext, this.getTEXT(), this.getCOLOR(), this.getSIZE(), this, this.getTypeface());
        dialog.show(((FragmentActivity) mActivity).getSupportFragmentManager(), Constants.EDIT_TAG);

        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }
}
