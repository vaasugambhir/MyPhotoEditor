package com.example.myphotoeditor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.transition.Fade;
import android.view.MenuItem;

import java.util.ArrayList;

public class EditorPage extends AppCompatActivity {

    private static ActionBar actionBar;
    private ArrayList<String> mFileNames;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor_page);
        supportPostponeEnterTransition();

        mFileNames = MainActivity.getFileNames();

        getWindow().getSharedElementEnterTransition().setDuration(Constants.TRANSITION_DURATION);
        getWindow().getSharedElementReturnTransition().setDuration(Constants.TRANSITION_DURATION);

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

        ViewPager pager = findViewById(R.id.image_viewPager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        pager.setAdapter(adapter);
        pager.setCurrentItem(MainActivity.position);

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                ViewPagerAdapter.counter = 0;
                actionBar.setTitle(mFileNames.get(position));
                MainActivity.position = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public static void changeActionBarPosition() {
        if (actionBar.isShowing())
            actionBar.hide();
        else
            actionBar.show();
    }

    private void load(String[] info) {
        // String path = info[0];
        String name = info[1];
        actionBar.setTitle(name);
    }

    private String[] setTransition() {
        // String transitionName = getIntent().getExtras().getString(Constants.IMAGE_TRANSITION_NAME);
        String path = getIntent().getExtras().getString(Constants.IMAGE_PATH);
        String name = getIntent().getExtras().getString(Constants.IMAGE_NAME);
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


    @Override
    public void finishAfterTransition() {
        actionBar.hide();
        super.finishAfterTransition();
    }
}