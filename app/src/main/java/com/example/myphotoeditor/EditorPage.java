package com.example.myphotoeditor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.app.SharedElementCallback;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.transition.Fade;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import yuku.ambilwarna.AmbilWarnaDialog;

public class EditorPage extends AppCompatActivity {

    // FIELDS
    private static ActionBar actionBar;
    private ArrayList<String> mFileNames;
    private MyViewPager mViewPager;
    private Button mEdit, mPaint, mRotate, mChooseColor, mSave, mDone, mCancel, mCrop, mSetCrop, mCancelCrop, mUndo;
    private int mDefColor;
    private Bitmap mCurrentBitmap;

    // OVERRIDDEN METHODS
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor_page);
        supportPostponeEnterTransition();

        mFileNames = MainActivity.getFileNames();
        mDefColor = ContextCompat.getColor(this, R.color.white);

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

        loadButtons();
        setMyViewPager();
    }

    private void setMyViewPager() {
        mViewPager = findViewById(R.id.image_viewPager);

        ViewPagerAdapter adapter = new ViewPagerAdapter(this);

        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(MainActivity.position);

        if (getCurrentView() != null)
            mCurrentBitmap = Bitmap.createBitmap(getCurrentView().getImageBitmap());

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                actionBar.setTitle(mFileNames.get(position));
                MainActivity.position = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        MyImageView imageView = getCurrentView();
        if (imageView != null) {
            if (imageView.getEditingMode()) {
                exitEditMode();
                if (getCurrentView() != null)
                    getCurrentView().disablePaintMode();
                if (getCurrentView() != null)
                    getCurrentView().disableCropMode();
            } else {
                super.onBackPressed();
                finishAfterTransition();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            MyImageView imageView = getCurrentView();
            if (imageView != null) {
                if (imageView.getEditingMode()) {
                    exitEditMode();
                    if (getCurrentView() != null)
                        getCurrentView().disablePaintMode();
                    if (getCurrentView() != null)
                        getCurrentView().disableCropMode();
                } else {
                    finishAfterTransition();
                }
            }
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

    // MY METHODS
    public void rotate(View view) {
        MyImageView currentView = getCurrentView();
        if (currentView != null)
            currentView.rotate();
    }

    public void save(View view) {
        /*
        MyImageView currentView = getCurrentView();
        if (currentView != null) {
            BitmapDrawable drawable = (BitmapDrawable) currentView.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            float rotation = currentView.getRotation();
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, System.currentTimeMillis() + "", null);
        }

         */
    }

    public void paint(View view) {
        MyImageView currentView = getCurrentView();
        enterPaintMode();
        if (currentView != null)
            currentView.paint();
    }

    public void chooseColor(View view) {

        MyImageView currentView = getCurrentView();
        if (currentView != null) {

            AmbilWarnaDialog colorDialog = new AmbilWarnaDialog(this, mDefColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                @Override
                public void onCancel(AmbilWarnaDialog dialog) {

                }

                @Override
                public void onOk(AmbilWarnaDialog dialog, int color) {
                    mDefColor = color;
                    currentView.setPaintColor(color);
                }
            });
            colorDialog.show();
        }
    }

    public void cancel(View view) {
        MyImageView currentView = getCurrentView();
        exitPaintMode();
        if (currentView != null)
            currentView.cancel();
    }

    public void done(View view) {
        MyImageView currentView = getCurrentView();
        exitPaintMode();
        if (currentView != null)
            currentView.done();
    }

    private void enterPaintMode() {
        mChooseColor.setVisibility(View.VISIBLE);
        mCancel.setVisibility(View.VISIBLE);
        mDone.setVisibility(View.VISIBLE);
        mCrop.setVisibility(View.GONE);
        mSave.setVisibility(View.GONE);
        mPaint.setVisibility(View.GONE);
        mRotate.setVisibility(View.GONE);
    }

    private void exitPaintMode() {

        mChooseColor.setVisibility(View.GONE);
        mCancel.setVisibility(View.GONE);
        mDone.setVisibility(View.GONE);
        mUndo.setVisibility(View.GONE);
        mCrop.setVisibility(View.VISIBLE);
        mSave.setVisibility(View.VISIBLE);
        mPaint.setVisibility(View.VISIBLE);
        mRotate.setVisibility(View.VISIBLE);
    }

    private void enterCropMode() {
        mCancelCrop.setVisibility(View.VISIBLE);
        mSetCrop.setVisibility(View.VISIBLE);
        mCrop.setVisibility(View.GONE);
        mSave.setVisibility(View.GONE);
        mPaint.setVisibility(View.GONE);
        mRotate.setVisibility(View.GONE);
    }

    private void exitCropMode() {
        mSetCrop.setVisibility(View.GONE);
        mCancelCrop.setVisibility(View.GONE);
        mCrop.setVisibility(View.VISIBLE);
        mSave.setVisibility(View.VISIBLE);
        mPaint.setVisibility(View.VISIBLE);
        mRotate.setVisibility(View.VISIBLE);
    }

    public void undo(View view) {
        MyImageView currentView = getCurrentView();
        if (currentView != null)
            currentView.undo();
    }

    public void crop(View view) {
        enterCropMode();
        MyImageView currentView = getCurrentView();
        if (currentView != null)
            currentView.crop();
    }

    public void setCrop(View view) {
        exitCropMode();
        MyImageView currentView = getCurrentView();
        if (currentView != null)
            currentView.setCrop();
    }

    public void cancelCrop(View view) {
        exitCropMode();
        MyImageView currentView = getCurrentView();
        if (currentView != null)
            currentView.cancelCrop();
    }

    private void loadButtons() {
        mEdit = findViewById(R.id.button_edit);
        mPaint = findViewById(R.id.paint);
        mRotate = findViewById(R.id.rotate);
        mChooseColor = findViewById(R.id.chooseColor);
        mSave = findViewById(R.id.save);
        mDone = findViewById(R.id.done);
        mCancel = findViewById(R.id.cancel);
        mCrop = findViewById(R.id.crop);
        mSetCrop = findViewById(R.id.setCrop);
        mCancelCrop = findViewById(R.id.cancelCrop);
        mUndo = findViewById(R.id.undo);
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

    private MyImageView getCurrentView() {
        try {
            return mViewPager.findViewWithTag(MainActivity.position);
        } catch (NullPointerException | IndexOutOfBoundsException exception) {
            return null;
        }
    }

    public void edit(View view) {
        if (getCurrentView() != null)
            mCurrentBitmap = Bitmap.createBitmap(getCurrentView().getImageBitmap());
        enterEditMode();
    }

    private void enterEditMode() {
        mEdit.setVisibility(View.GONE);
        mCrop.setVisibility(View.VISIBLE);
        mPaint.setVisibility(View.VISIBLE);
        mSave.setVisibility(View.VISIBLE);
        mRotate.setVisibility(View.VISIBLE);

        MyImageView currentImage = getCurrentView();
        if (currentImage != null) {
            currentImage.setEditingMode(true);
            mViewPager.disableScroll(true);
        }

        actionBar.hide();
    }

    private void exitEditMode() {
        exitCropMode();
        exitPaintMode();

        mEdit.setVisibility(View.VISIBLE);
        mCrop.setVisibility(View.GONE);
        mPaint.setVisibility(View.GONE);
        mSave.setVisibility(View.GONE);
        mRotate.setVisibility(View.GONE);

        MyImageView currentImage = getCurrentView();
        if (currentImage != null) {
            currentImage.setRotation(true);
            currentImage.setRotation(0);
            currentImage.setImageBitmap(mCurrentBitmap);
            currentImage.setEditingMode(false);
            mViewPager.disableScroll(false);
        }

        actionBar.show();
    }
}