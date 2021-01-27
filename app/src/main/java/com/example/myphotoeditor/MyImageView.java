package com.example.myphotoeditor;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class MyImageView extends androidx.appcompat.widget.AppCompatImageView implements GestureDetector.OnGestureListener{

    // FIELDS
    //for image movement
    private float yDown = 0, init_x = 0, init_y = 0;
    private int counter = 0;
    private GestureDetector mGestureDetector;

    // necessary
    private Context mContext;
    private Activity myActivity;
    private String mFilePath;

    // for paint
    private int mDefColor;
    private boolean mPaintMode, mCropMode, mHappened, mEditingMode;
    private ArrayList<Path> mPaintPaths;
    private ArrayList<Paint> mPaints;
    private Paint mCurrentPaint, mRectPaint, mOuterRectPaint, mLinesPaint;
    private Path mCurrentPath;

    // for cropping
    private RectF mCropRectangle;
    private Rect mCropOuterRect;
    private float mCropRectTopH, mCropRectLeftW, mCropRectInitH, mCropRectInitW;
    private int mCropCounter;
    private final float mCircleRadius = 15f;
    private boolean mScrolling;

    public MyImageView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public MyImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MyImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    // MY METHODS
    private void init(Context context) {
        mContext = context;
        mEditingMode = false;
        mScrolling = false;
        myActivity = (Activity)context;
        mGestureDetector = new GestureDetector(mContext, this);
        mDefColor = R.color.white;
        mPaintMode = false;
        mCropMode = false;
        mHappened = false;
        mPaintPaths = new ArrayList<>();
        mPaints = new ArrayList<>();
        mCurrentPath = new Path();
        mCurrentPaint = initPaint();
        int mSpecialTransparent = ContextCompat.getColor(mContext, R.color.special_transparent);
        mCurrentPaint.setColor(ContextCompat.getColor(mContext, R.color.white));
        mRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRectPaint.setColor(mSpecialTransparent);
        mOuterRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOuterRectPaint.setColor(ContextCompat.getColor(mContext, R.color.black));
        mOuterRectPaint.setStrokeWidth(15f);
        mOuterRectPaint.setStyle(Paint.Style.STROKE);
        mLinesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinesPaint.setColor(ContextCompat.getColor(mContext, R.color.black));
        mLinesPaint.setStrokeWidth(5f);
        mLinesPaint.setStyle(Paint.Style.STROKE);
        mCropRectangle = new RectF();
        mCropOuterRect = new Rect();
        mCropCounter = 0;
    }

    public void setPath(String path) {
        this.mFilePath = path;
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
        this.mEditingMode = edit;
    }

    public boolean getEditingMode() {
        return this.mEditingMode;
    }

    private void freeFlyingModeOnTouch(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);

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
    }

    public void rotate() {
        Bitmap bmp = getImageBitmap();
        Matrix matrix = new Matrix();
        matrix.postRotate(getRotation() + 90);
        Bitmap bitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        this.setImageBitmap(bitmap);
    }

    public void paint() {
        mPaintMode = true;
    }

    public void setPaintColor(int color) {
        this.mDefColor = color;
        if (color == R.color.white)
            mCurrentPaint.setColor(ContextCompat.getColor(mContext, R.color.white));
        else
            mCurrentPaint.setColor(this.mDefColor);
    }

    public void setScrolling(boolean set) {
        mScrolling = set;
    }

    private Paint initPaint() {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (mDefColor == R.color.white)
            paint.setColor(ContextCompat.getColor(mContext, R.color.white));
        else
            paint.setColor(this.mDefColor);
        paint.setStrokeWidth(10f);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStyle(Paint.Style.STROKE);
        return paint;
    }

    public void done() {
        getButton().setVisibility(GONE);

        if (mPaintPaths.isEmpty()) {
            cancel();
            return;
        }

        RectF rect = getImageRect();
        this.setDrawingCacheEnabled(true);
        this.buildDrawingCache();
        Bitmap bmp = Bitmap.createBitmap(this.getDrawingCache(), (int) rect.left, (int) rect.top, (int) rect.width(), (int) rect.height());
        this.setDrawingCacheEnabled(false);
        this.setImageBitmap(bmp);
        cancel();
    }

    private RectF getImageRect() {

        int[] offset = new int[2];
        float[] values = new float[9];
        Matrix m = this.getImageMatrix();

        m.getValues(values);

        offset[0] = (int) values[Matrix.MTRANS_Y];
        offset[1] = (int) values[Matrix.MTRANS_X];
        float drawableWidth = this.getDrawable().getIntrinsicWidth();
        float drawableHeight = this.getDrawable().getIntrinsicHeight();
        float displayedWidth = drawableWidth * values[Matrix.MSCALE_X];
        float displayedHeight = drawableHeight * values[Matrix.MSCALE_Y];

        float topMargin = offset[0];
        float leftMargin = offset[1];

        return new RectF(
                leftMargin,
                topMargin,
                (leftMargin + displayedWidth),
                (topMargin + displayedHeight));
    }

    public void cancel() {
        getButton().setVisibility(GONE);
        mPaintPaths.clear();
        mPaints.clear();
        mPaintMode = false;
        mCurrentPath.reset();
        postInvalidate();
    }

    public void undo() {
        updateList(false);
        postInvalidate();
    }

    private void updateList(boolean increment) {
        if (increment) {
            mPaintPaths.add(mCurrentPath);
            mPaints.add(mCurrentPaint);
            mCurrentPath = new Path();
            mCurrentPaint = initPaint();

            if (mPaintPaths.size() == 1)
                getButton().setVisibility(VISIBLE);

        } else {
            if (mPaintPaths.isEmpty())
                return;
            mPaintPaths.remove(mPaintPaths.size() - 1);
            mPaints.remove(mPaints.size() - 1);

            if (mPaintPaths.isEmpty())
                getButton().setVisibility(GONE);
        }
    }

    private Button getButton() {
        Activity myActivity = (Activity) mContext;
        return myActivity.findViewById(R.id.button_undo);
    }

    private boolean paintModeOnTouch(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mCurrentPath.moveTo(x, y);
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                mCurrentPath.lineTo(x, y);
                break;
            }
            case MotionEvent.ACTION_UP: {
                updateList(true);
                break;
            }
            default:
                return false;
        }
        postInvalidate();
        return true;
    }

    private boolean isInsideCircle(float x, float y, float cx, float cy) {
        return (Math.pow(x-cx, 2) + Math.pow(y-cy, 2)) <= Math.pow(mCircleRadius + 10f, 2);
    }

    private boolean cropModeOnTouch(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        mCropCounter++;

        if (!getImageRect().contains(mCropRectangle)) {
            float centreY = getHeight() / 2f;
            float centreX = getWidth() / 2f;

            mCropRectangle.top = centreY - mCropRectInitH/2;
            mCropRectangle.bottom = centreY + mCropRectInitH/2;
            mCropRectangle.left = centreX - mCropRectInitW/2;
            mCropRectangle.right = centreX + mCropRectInitW/2;

            mCropOuterRect.top = (int) (centreY - mCropRectInitH/2);
            mCropOuterRect.bottom = (int) (centreY + mCropRectInitH/2);
            mCropOuterRect.left = (int) (centreX - mCropRectInitW/2);
            mCropOuterRect.right = (int) (centreX + mCropRectInitW/2);
            mHappened = true;
            return false;
        }

        if (mHappened) {
            mHappened = false;
            mCropRectTopH = y - mCropRectangle.top;
            mCropRectLeftW = x - mCropRectangle.left;
            mCropRectInitH = mCropRectangle.height();
            mCropRectInitW = mCropRectangle.width();
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE) {

            boolean left = (x < mCropRectangle.left + 50f && x > mCropRectangle.left - 50f) && (y > mCropRectangle.top + 25f && y < mCropRectangle.bottom - 25f);
            boolean right = (x < mCropRectangle.right + 50f && x > mCropRectangle.right - 50f) && (y > mCropRectangle.top + 25f && y < mCropRectangle.bottom - 25f);
            boolean top = (y < mCropRectangle.top + 50f && y > mCropRectangle.top - 50f) && (x > mCropRectangle.left + 25f && x < mCropRectangle.right - 25f);
            boolean bottom = (y < mCropRectangle.bottom + 50f && y > mCropRectangle.bottom - 50f) && (x > mCropRectangle.left + 25f && x < mCropRectangle.right - 25f);
            boolean topLeft = isInsideCircle(x, y, mCropOuterRect.left, mCropOuterRect.top);
            boolean topRight = isInsideCircle(x, y, mCropOuterRect.right, mCropOuterRect.top);
            boolean bottomLeft = isInsideCircle(x, y, mCropOuterRect.left, mCropOuterRect.bottom);
            boolean bottomRight = isInsideCircle(x, y, mCropOuterRect.right, mCropOuterRect.bottom);

            if (left) {

                mCropOuterRect.left = (int) x;
                mCropRectangle.left = x;
                postInvalidate();
                return true;

            } else if (right) {

                mCropOuterRect.right = (int) x;
                mCropRectangle.right = x;
                postInvalidate();
                return true;

            } else if (top) {

                mCropOuterRect.top = (int) y;
                mCropRectangle.top = y;
                postInvalidate();
                return true;

            } else if (bottom) {

                mCropOuterRect.bottom = (int) y;
                mCropRectangle.bottom = y;
                postInvalidate();
                return true;

            } else if (topLeft) {

                mCropOuterRect.left = (int) x;
                mCropRectangle.left = x;
                mCropOuterRect.top = (int) y;
                mCropRectangle.top = y;
                postInvalidate();
                return true;

            } else if (topRight) {

                mCropOuterRect.right = (int) x;
                mCropRectangle.right = x;
                mCropOuterRect.top = (int) y;
                mCropRectangle.top = y;
                postInvalidate();
                return true;

            } else if (bottomLeft) {

                mCropOuterRect.left = (int) x;
                mCropRectangle.left = x;
                mCropOuterRect.bottom = (int) y;
                mCropRectangle.bottom = y;
                postInvalidate();
                return true;

            } else if (bottomRight) {

                mCropOuterRect.right = (int) x;
                mCropRectangle.right = x;
                mCropOuterRect.bottom = (int) y;
                mCropRectangle.bottom = y;
                postInvalidate();
                return true;

            }

        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (mCropCounter == 1) {
                    mCropRectTopH = y - mCropRectangle.top;
                    mCropRectLeftW = x - mCropRectangle.left;
                    mCropRectInitH = mCropRectangle.height();
                    mCropRectInitW = mCropRectangle.width();
                }
                return true;
            }
            case MotionEvent.ACTION_MOVE: {

                System.out.println(mCropRectangle);

                if (mCropRectangle.contains(x, y)) {

                    mCropRectangle.left = x - mCropRectLeftW;
                    mCropRectangle.top = y - mCropRectTopH;
                    mCropRectangle.right = mCropRectangle.left + mCropRectInitW;
                    mCropRectangle.bottom = mCropRectangle.top + mCropRectInitH;

                    mCropOuterRect.left = (int) (x - mCropRectLeftW);
                    mCropOuterRect.top = (int) (y - mCropRectTopH);
                    mCropOuterRect.right = (int) (mCropRectangle.left + mCropRectInitW);
                    mCropOuterRect.bottom = (int) (mCropRectangle.top + mCropRectInitH);
                }

                break;
            }
            case MotionEvent.ACTION_UP: {
                mCropCounter = 0;
                return true;
            }
        }

        postInvalidate();
        return true;
    }

    public void crop() {
        mCropMode = true;

        float centreY = getHeight() / 2f;
        float centreX = getWidth() / 2f;

        mCropRectangle.top = centreY - 400;
        mCropRectangle.bottom = centreY + 400;
        mCropRectangle.left = centreX - 250;
        mCropRectangle.right = centreX + 250;

        mCropOuterRect.top = (int) (centreY - 400);
        mCropOuterRect.bottom = (int) (centreY + 400);
        mCropOuterRect.left = (int) (centreX - 250);
        mCropOuterRect.right = (int) (centreX + 250);

        RectF rect = getImageRect();

        if (mCropOuterRect.height() >=  rect.height()) {
            mCropOuterRect.top = (int) (rect.top + 100);
            mCropRectangle.top = (int) (rect.top + 100);
            mCropOuterRect.bottom = (int) (rect.bottom - 100);
            mCropRectangle.bottom = (int) (rect.bottom - 100);
        } else if (mCropRectangle.width() >= rect.width()) {
            mCropOuterRect.left = (int) (rect.left + 100);
            mCropRectangle.left = (int) (rect.left + 100);
            mCropOuterRect.right = (int) (rect.right - 100);
            mCropRectangle.right = (int) (rect.right - 100);
        }

        postInvalidate();
    }

    public void cancelCrop() {
        mCropMode = false;
        postInvalidate();
    }

    public void setCrop() {
        mCropMode = false;
        this.setDrawingCacheEnabled(true);
        this.buildDrawingCache();
        Bitmap bmp = Bitmap.createBitmap(this.getDrawingCache(), (int) mCropRectangle.left, (int) mCropRectangle.top, (int) mCropRectangle.width(), (int) mCropRectangle.height());
        this.setDrawingCacheEnabled(false);
        this.setImageBitmap(bmp);
        postInvalidate();
    }

    public Bitmap getImageBitmap() {
        BitmapDrawable drawable = (BitmapDrawable) this.getDrawable();
        return drawable.getBitmap();
    }

    // OVERRIDDEN METHODS
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();

        if (counter == 0) {
            init_x = this.getX();
            init_y = this.getY();
        }

        counter++;

        if (mScrolling) {
            this.animate().y(init_y).setDuration(0).start();
        } else if (!mEditingMode) {
            freeFlyingModeOnTouch(event);
        } else {
            if (mPaintMode) {
                if (!getImageRect().contains(x, y))
                    return false;
                return paintModeOnTouch(event);
            } else if (mCropMode) {
                if (!getImageRect().contains(x, y))
                    return false;
                return cropModeOnTouch(event);
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mCropMode) {
            canvas.drawRect(mCropRectangle, mRectPaint);
            canvas.drawRect(mCropOuterRect, mOuterRectPaint);
            canvas.drawCircle(mCropOuterRect.left, mCropOuterRect.top, mCircleRadius, mOuterRectPaint);
            canvas.drawCircle(mCropOuterRect.left, mCropOuterRect.bottom, mCircleRadius, mOuterRectPaint);
            canvas.drawCircle(mCropOuterRect.right, mCropOuterRect.top, mCircleRadius, mOuterRectPaint);
            canvas.drawCircle(mCropOuterRect.right, mCropOuterRect.bottom, mCircleRadius, mOuterRectPaint);
            canvas.drawLine(mCropOuterRect.left + mCropOuterRect.width()/3f, mCropOuterRect.top, mCropOuterRect.left + mCropOuterRect.width()/3f, mCropOuterRect.bottom, mLinesPaint);
            canvas.drawLine(mCropOuterRect.left + 2*mCropOuterRect.width()/3f, mCropOuterRect.top, mCropOuterRect.left + 2*mCropOuterRect.width()/3f, mCropOuterRect.bottom, mLinesPaint);
            canvas.drawLine(mCropOuterRect.left, mCropOuterRect.top + mCropOuterRect.height()/3f, mCropOuterRect.right, mCropOuterRect.top + mCropOuterRect.height()/3f, mLinesPaint);
            canvas.drawLine(mCropOuterRect.left, mCropOuterRect.top + 2*mCropOuterRect.height()/3f, mCropOuterRect.right, mCropOuterRect.top + 2*mCropOuterRect.height()/3f, mLinesPaint);
        }

        for (int i = 0; i < mPaintPaths.size(); i++) {
            Paint paint = mPaints.get(i);
            Path path = mPaintPaths.get(i);
            canvas.drawPath(path, paint);
        }
        canvas.drawPath(mCurrentPath, mCurrentPaint);
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
        ActionBar actionBar = EditorPage.getMyActionBar();
        Button editButton = myActivity.findViewById(R.id.button_edit);

        if (actionBar.isShowing()) {
            actionBar.hide();

            editButton.animate().translationYBy(getHeight()).setDuration(300).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    editButton.setVisibility(VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    editButton.setVisibility(GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            }).start();
        }
        else {
            actionBar.show();
            editButton.animate().translationY(0).setDuration(300).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    editButton.setVisibility(VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    editButton.setVisibility(VISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            }).start();

        }
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

    public void disablePaintMode() {
        mPaintMode = false;
        mPaintPaths.clear();
        mPaints.clear();
        mCurrentPath.reset();
        postInvalidate();
    }

    public void disableCropMode() {
        mCropMode = false;
        postInvalidate();
    }
}
