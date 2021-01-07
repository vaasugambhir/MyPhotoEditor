package com.example.myphotoeditor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.app.SharedElementCallback;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.transition.Fade;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EditorPage extends AppCompatActivity {

    private static ActionBar actionBar;
    private ArrayList<String> mFileNames;
    private ViewPager mViewPager;

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

        mViewPager = findViewById(R.id.image_viewPager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(MainActivity.position);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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

    private void load(String info) {
        actionBar.setTitle(info);
    }

    private String setTransition() {
        return getIntent().getExtras().getString(Constants.IMAGE_NAME);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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

        setEnterSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                View view = getCurrentView();
                if (view == null)
                    return;

                names.clear();
                sharedElements.clear();

                String transitionName = ViewCompat.getTransitionName(view);
                names.add(transitionName);
                sharedElements.put(transitionName, view);

                setExitSharedElementCallback((SharedElementCallback) null);
            }
        });

        setResult(RESULT_OK);
        super.finishAfterTransition();
    }

    private View getCurrentView() {
        try {
            return mViewPager.findViewWithTag(MainActivity.position);
        } catch (NullPointerException | IndexOutOfBoundsException exception) {
            return null;
        }
    }
}