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
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

public class EditorPage extends AppCompatActivity {

    private ImageView Image;
    private ActionBar actionBar;
    private float yDown = 0, init_x = 0, init_y = 0;
    private int counter = 0;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor_page);
        supportPostponeEnterTransition();

        actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.special_transparent, getApplicationContext().getTheme())));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.hide();

        Fade fade = new Fade();
        View decor = getWindow().getDecorView();
        fade.excludeTarget(decor.findViewById(R.id.action_bar_container), true);
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        fade.excludeTarget(android.R.id.navigationBarBackground, true);

        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);

        load(setTransition());

        setTouchListeners();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setTouchListeners() {
        Image.setOnTouchListener((v, event) -> {

            if (counter == 0) {
                init_x = Image.getX();
                init_y = Image.getY();
            }

            counter++;

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN: {
                    changeActionBarPosition(event);
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
                    System.out.println(final_y-init_y);

                    if (Math.abs(final_y - init_y) > 750f) {
                        finishAfterTransition();
                    }
                    else {
                        Image.animate()
                                .x(init_x)
                                .y(init_y)
                                .setDuration(200)
                                .start();
                    }
                    break;
                }
            }
            return true;
        });
        findViewById(R.id.constraint_layout).setOnTouchListener((v, event) -> {
            changeActionBarPosition(event);
            return true;
        });
    }

    private void changeActionBarPosition(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (actionBar.isShowing())
                actionBar.hide();
            else
                actionBar.show();
        }
    }

    private void load(String[] info) {
        String path = info[0];
        String name = info[1];
        actionBar.setTitle(name);
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
        String transitionName = getIntent().getExtras().getString(MainActivity.IMAGE_TRANSITION_NAME);
        String path = getIntent().getExtras().getString(MainActivity.IMAGE_PATH);
        String name = getIntent().getExtras().getString(MainActivity.IMAGE_NAME);
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
}