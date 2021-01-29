package com.example.myphotoeditor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class ChangePaintThicknessView extends View {

    private float mThickness;
    private int mCurrentColor;
    private Paint mCirclePaint;

    private void init(Context context) {
        mThickness = 10f;
        mCurrentColor = ContextCompat.getColor(context, R.color.white);
        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(mCurrentColor);
        mCirclePaint.setStrokeWidth(mThickness);
    }

    public ChangePaintThicknessView(Context context) {
        super(context);
        init(context);
    }

    public ChangePaintThicknessView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ChangePaintThicknessView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(getHeight()/2f, getWidth()/2f, mThickness/2, mCirclePaint);
    }

    public void setThickness(float thickness) {
        mCirclePaint.setStrokeWidth(mThickness);
        mThickness = thickness;
        postInvalidate();
    }

    public void setCurrentColor(int color) {
        mCurrentColor = color;
        mCirclePaint.setColor(color);
    }
}
