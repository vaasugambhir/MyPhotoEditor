package com.example.myphotoeditor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import android.annotation.SuppressLint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.transition.Fade;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

public class EditorPage extends AppCompatActivity implements View.OnTouchListener, GestureDetector.OnDoubleTapListener, GestureDetector.OnGestureListener {

    private ImageView Image;
    private ActionBar actionBar;
    private float yDown = 0, init_x = 0, init_y = 0;
    private int counter = 0;
    private GestureDetector mGestureDetector;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor_page);
        supportPostponeEnterTransition();

        getWindow().getSharedElementEnterTransition().setDuration(Constants.TRANSITION_DURATION);
        getWindow().getSharedElementReturnTransition().setDuration(Constants.TRANSITION_DURATION);

        mGestureDetector = new GestureDetector(this, this);

        actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.special_transparent, getApplicationContext().getTheme())));
        actionBar.setDisplayHomeAsUpEnabled(true);

        Fade fade = new Fade();
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        fade.excludeTarget(android.R.id.navigationBarBackground, true);
        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);

        load(setTransition());

        findViewById(R.id.constraint_layout).setOnTouchListener(this);
        Image.setOnTouchListener(this);
    }

    private void changeActionBarPosition() {
        if (actionBar.isShowing())
            actionBar.hide();
        else
            actionBar.show();
    }

    private void load(String[] info) {
        String path = info[0];
        String name = info[1];
        actionBar.setTitle(name);
        Image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        Glide.with(this)
                .load(path)
                .dontAnimate()
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        supportStartPostponedEnterTransition();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        supportStartPostponedEnterTransition();
                        return false;
                    }
                })
                .into(Image);
    }

    private String[] setTransition() {
        Image = findViewById(R.id.imageView_Image);
        String transitionName = getIntent().getExtras().getString(Constants.IMAGE_TRANSITION_NAME);
        String path = getIntent().getExtras().getString(Constants.IMAGE_PATH);
        String name = getIntent().getExtras().getString(Constants.IMAGE_NAME);
        ViewCompat.setTransitionName(Image, transitionName);
        return new String[]{path, name};
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        actionBar.hide();
        finishAfterTransition();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finishAfterTransition();
            return true;
        }
        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent event) {

        mGestureDetector.onTouchEvent(event);

        if (view.getId() == Image.getId()) {
            if (counter == 0) {
                init_x = Image.getX();
                init_y = Image.getY();
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

                    Image.setX(Image.getX());
                    Image.setY(Image.getY() + distanceY);

                    break;
                }
                case MotionEvent.ACTION_UP: {
                    float final_y = Image.getY();

                    if (Math.abs(final_y - init_y) > Constants.EXIT_DISTANCE) {
                        finishAfterTransition();
                    } else {
                        Image.animate()
                                .x(init_x)
                                .y(init_y)
                                .setDuration(200)
                                .start();
                    }
                    break;
                }
            }
        }


        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
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
        changeActionBarPosition();
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

    @Override
    public void finishAfterTransition() {
        actionBar.hide();
        super.finishAfterTransition();
    }
}