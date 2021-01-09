package com.example.myphotoeditor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

public class ViewPagerAdapter extends PagerAdapter implements GestureDetector.OnGestureListener {

    private final ArrayList<String> mFilePaths;
    private final Context mContext;
    private final Activity myActivity;
    private final GestureDetector mGestureDetector;
    private float yDown = 0, init_x = 0, init_y = 0;
    public static int counter = 0;

    ViewPagerAdapter(Context context) {
        mContext = context;
        mFilePaths = MainActivity.getFilePaths();
        myActivity = (Activity) mContext;
        mGestureDetector = new GestureDetector(mContext, this);
    }

    @Override
    public int getCount() {
        return mFilePaths.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @SuppressLint("ClickableViewAccessibility")
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ImageView image = new ImageView(mContext);
        ViewCompat.setTransitionName(image, mFilePaths.get(position));

        Glide.with(mContext)
                .load(mFilePaths.get(position))
                .dontAnimate()
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        ActivityCompat.startPostponedEnterTransition(myActivity);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        ActivityCompat.startPostponedEnterTransition(myActivity);
                        return false;
                    }
                })
                .into(image);

        image.setOnTouchListener((v, event) -> {
            mGestureDetector.onTouchEvent(event);

            if (counter == 0) {
                init_x = image.getX();
                init_y = image.getY();
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

                    image.setX(image.getX());
                    image.setY(image.getY() + distanceY);

                    break;
                }
                case MotionEvent.ACTION_UP: {
                    float final_y = image.getY();

                    if (final_y - init_y > Constants.EXIT_DISTANCE) {
                        ActivityCompat.finishAfterTransition(myActivity);
                    } else if(init_y - final_y > Constants.EXIT_DISTANCE) {
                        vibrate();
                        shareImage(position);
                        animateImage(image);
                    } else {
                        animateImage(image);
                    }
                    break;
                }
            }
            return true;
        });

        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        image.setTag(position);
        container.addView(image);

        return image;
    }

    private void vibrate() {
        Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(Constants.VIBRATION_DURATION, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(Constants.VIBRATION_DURATION);
        }
    }

    private void animateImage(ImageView image) {
        image.animate()
                .x(init_x)
                .y(init_y)
                .setDuration(Constants.RETURN_ANIMATION_DURATION)
                .start();
    }

    private void shareImage(int pos) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/*");
        String path = mFilePaths.get(pos);
        Uri imageUri =  Uri.parse(path);
        share.putExtra(Intent.EXTRA_STREAM, imageUri);
        mContext.startActivity(Intent.createChooser(share, "Share image via"));
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ImageView) object);
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
