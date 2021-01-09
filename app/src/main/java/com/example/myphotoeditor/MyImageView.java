package com.example.myphotoeditor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

public class MyImageView extends androidx.appcompat.widget.AppCompatImageView implements GestureDetector.OnGestureListener{

    private boolean editingMode = false;
    private float yDown = 0, init_x = 0, init_y = 0;
    private int counter = 0;
    private final Context mContext;
    private final Activity myActivity;
    private String mFilePath;
    private final GestureDetector mGestureDetector;

    public MyImageView(@NonNull Context context) {
        super(context);
        mContext = context;
        myActivity = (Activity)context;
        mGestureDetector = new GestureDetector(mContext, this);
    }

    public MyImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        myActivity = (Activity)mContext;
        mGestureDetector = new GestureDetector(mContext, this);
    }

    public MyImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        myActivity = (Activity)mContext;
        mGestureDetector = new GestureDetector(mContext, this);
    }

    public void setPath(String path) {
        this.mFilePath = path;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (!editingMode) {

            mGestureDetector.onTouchEvent(event);

            if (counter == 0) {
                init_x = this.getX();
                init_y = this.getY();
            }

            counter++;

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN: {
                    yDown = event.getY();
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    float yMoved = event.getY();

                    float distanceY = yMoved - yDown;

                    this.setX(this.getX());
                    this.setY(this.getY() + distanceY);

                    break;
                }
                case MotionEvent.ACTION_UP: {
                    float final_y = this.getY();

                    if (final_y - init_y > Constants.EXIT_DISTANCE) {
                        ActivityCompat.finishAfterTransition(myActivity);
                    } else if (init_y - final_y > Constants.EXIT_DISTANCE) {
                        vibrate();
                        shareImage();
                        animateImage();
                    } else {
                        animateImage();
                    }
                    break;
                }
            }
        } else {
            return false;
        }
        return true;
    }

    private void vibrate() {
        Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(Constants.VIBRATION_DURATION, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(Constants.VIBRATION_DURATION);
        }
    }

    private void animateImage() {
        this.animate()
                .x(init_x)
                .y(init_y)
                .setDuration(Constants.RETURN_ANIMATION_DURATION)
                .start();
    }

    private void shareImage() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/*");
        Uri imageUri =  Uri.parse(mFilePath);
        share.putExtra(Intent.EXTRA_STREAM, imageUri);
        mContext.startActivity(Intent.createChooser(share, "Share image via"));
    }

    public void setEditingMode(boolean edit) {
        this.editingMode = edit;
    }

    public boolean getEditingMode() {
        return this.editingMode;
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
        EditorPage.changeActionBarPosition();
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }
}
