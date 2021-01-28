package com.example.myphotoeditor;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.app.SharedElementCallback;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.transition.Fade;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
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
    public static boolean mSaved;

    // OVERRIDDEN METHODS
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor_page);
        supportPostponeEnterTransition();

        mSaved = false;

//        Window w = getWindow();
//        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//        w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        mFileNames = MainActivity.getFileNames();
        mDefColor = ContextCompat.getColor(this, R.color.white);

        getWindow().getSharedElementEnterTransition().setDuration(Constants.TRANSITION_DURATION);
        getWindow().getSharedElementReturnTransition().setDuration(Constants.TRANSITION_DURATION);

        actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.special_transparent)));
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
                MyImageView imageView = getCurrentView();
                if (positionOffset > 0.00000000000001) {
                    imageView.setScrolling(true);
                }
                if (positionOffset == 0) {
                    imageView.setScrolling(false);
                }
            }

            @Override
            public void onPageSelected(int position) {
                actionBar.setTitle(mFileNames.get(position));
                MainActivity.position = position;
                MyImageView imageView = getCurrentView();
                imageView.setScrolling(false);
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
        vibrate();
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
        vibrate();
        MyImageView currentView = getCurrentView();
        if (currentView != null)
            currentView.rotate();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void save(View view) {

        vibrate();

        String saved = "Image saved in Gallery";
        Toast saveToast = Toast.makeText(this, saved, Toast.LENGTH_SHORT);
        saveToast.setText(saved);
        saveToast.setDuration(Toast.LENGTH_SHORT);
        saveToast.setGravity(Gravity.CENTER, 0, 0);

        MyImageView currentView = getCurrentView();
        if (currentView != null) {
            BitmapDrawable drawable = (BitmapDrawable) currentView.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            saveImage(bitmap);
            saveToast.show();
        }

        exitEditMode();
        if (getCurrentView() != null)
            getCurrentView().disablePaintMode();
        if (getCurrentView() != null)
            getCurrentView().disableCropMode();

        mSaved = true;
    }

    private void saveImage(Bitmap bitmap) {
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        String folderName = "MyPhotoEditor";
        File myDirectory = new File(root + "/" + folderName);
        if (!myDirectory.exists())
            myDirectory.mkdirs();
        String imageName = "MyPhotoEditor_" + System.currentTimeMillis() + "_rotidEotohPyM.jpg";
        File image = new File(myDirectory, imageName);

        try {
            FileOutputStream outputStream = new FileOutputStream(image);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            final Uri contentUri = Uri.fromFile(image);
            scanIntent.setData(contentUri);
            sendBroadcast(scanIntent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void paint(View view) {
        vibrate();
        MyImageView currentView = getCurrentView();
        enterPaintMode();
        if (currentView != null)
            currentView.paint();
    }

    public void chooseColor(View view) {
        vibrate();
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
        vibrate();
        MyImageView currentView = getCurrentView();
        exitPaintMode();
        if (currentView != null)
            currentView.cancel();
    }

    public void done(View view) {
        vibrate();
        MyImageView currentView = getCurrentView();
        exitPaintMode();
        if (currentView != null)
            currentView.done();
    }

    private void enterPaintMode() {
        Animation animationEnter = AnimationUtils.loadAnimation(this, R.anim.bottom_to_top);
        Animation animationExit = AnimationUtils.loadAnimation(this, R.anim.top_to_bottom);
        mChooseColor.startAnimation(animationEnter);
        mChooseColor.setVisibility(View.VISIBLE);
        mCancel.startAnimation(animationEnter);
        mCancel.setVisibility(View.VISIBLE);
        mDone.startAnimation(animationEnter);
        mDone.setVisibility(View.VISIBLE);
        mCrop.startAnimation(animationExit);
        mCrop.setVisibility(View.GONE);
        mSave.startAnimation(animationExit);
        mSave.setVisibility(View.GONE);
        mPaint.startAnimation(animationExit);
        mPaint.setVisibility(View.GONE);
        mRotate.startAnimation(animationExit);
        mRotate.setVisibility(View.GONE);
    }

    private void exitPaintMode() {
        Animation animationEnter = AnimationUtils.loadAnimation(this, R.anim.bottom_to_top);
        Animation animationExit = AnimationUtils.loadAnimation(this, R.anim.top_to_bottom);
        mChooseColor.startAnimation(animationExit);
        mChooseColor.setVisibility(View.GONE);
        mCancel.startAnimation(animationExit);
        mCancel.setVisibility(View.GONE);
        mDone.startAnimation(animationExit);
        mDone.setVisibility(View.GONE);
        mUndo.startAnimation(animationExit);
        mUndo.setVisibility(View.GONE);
        mCrop.startAnimation(animationEnter);
        mCrop.setVisibility(View.VISIBLE);
        mSave.startAnimation(animationEnter);
        mSave.setVisibility(View.VISIBLE);
        mPaint.startAnimation(animationEnter);
        mPaint.setVisibility(View.VISIBLE);
        mRotate.startAnimation(animationEnter);
        mRotate.setVisibility(View.VISIBLE);
    }

    private void enterCropMode() {
        Animation animationEnter = AnimationUtils.loadAnimation(this, R.anim.bottom_to_top);
        Animation animationExit = AnimationUtils.loadAnimation(this, R.anim.top_to_bottom);
        mCancelCrop.startAnimation(animationEnter);
        mCancelCrop.setVisibility(View.VISIBLE);
        mSetCrop.startAnimation(animationEnter);
        mSetCrop.setVisibility(View.VISIBLE);
        mCrop.startAnimation(animationExit);
        mCrop.setVisibility(View.GONE);
        mSave.startAnimation(animationExit);
        mSave.setVisibility(View.GONE);
        mPaint.startAnimation(animationExit);
        mPaint.setVisibility(View.GONE);
        mRotate.startAnimation(animationExit);
        mRotate.setVisibility(View.GONE);
    }

    private void exitCropMode() {
        Animation animationEnter = AnimationUtils.loadAnimation(this, R.anim.bottom_to_top);
        Animation animationExit = AnimationUtils.loadAnimation(this, R.anim.top_to_bottom);
        mSetCrop.startAnimation(animationExit);
        mSetCrop.setVisibility(View.GONE);
        mCancelCrop.startAnimation(animationExit);
        mCancelCrop.setVisibility(View.GONE);
        mCrop.startAnimation(animationEnter);
        mCrop.setVisibility(View.VISIBLE);
        mSave.startAnimation(animationEnter);
        mSave.setVisibility(View.VISIBLE);
        mPaint.startAnimation(animationEnter);
        mPaint.setVisibility(View.VISIBLE);
        mRotate.startAnimation(animationEnter);
        mRotate.setVisibility(View.VISIBLE);
    }

    public void undo(View view) {
        vibrate();
        MyImageView currentView = getCurrentView();
        if (currentView != null)
            currentView.undo();
    }

    @SuppressLint("ShowToast")
    public void crop(View view) {
        MyImageView currentView = getCurrentView();
        if (currentView!= null) {
            RectF rect = currentView.getImageRect();
            if (rect.height() < 100 || rect.width() < 100) {
                String notPossible = "Cannot crop images of small height/width";
                Toast toast = Toast.makeText(this, notPossible, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;
            }
            vibrate();
            enterCropMode();
            currentView.crop();
        }
    }

    public void setCrop(View view) {
        vibrate();
        exitCropMode();
        MyImageView currentView = getCurrentView();
        if (currentView != null) {
            currentView.setCrop();
            currentView.setRotation(currentView.getRotation());
        }
    }

    public void cancelCrop(View view) {
        vibrate();
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
        mUndo = findViewById(R.id.button_undo);
    }

    public static ActionBar getMyActionBar() {
        return actionBar;
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
        vibrate();
        if (getCurrentView() != null)
            mCurrentBitmap = Bitmap.createBitmap(getCurrentView().getImageBitmap());
        enterEditMode();
    }

    private void enterEditMode() {
        Animation animationEnter = AnimationUtils.loadAnimation(this, R.anim.bottom_to_top);
        Animation animationExit = AnimationUtils.loadAnimation(this, R.anim.top_to_bottom);
        mEdit.startAnimation(animationExit);
        mEdit.setVisibility(View.GONE);
        mCrop.startAnimation(animationEnter);
        mCrop.setVisibility(View.VISIBLE);
        mPaint.startAnimation(animationEnter);
        mPaint.setVisibility(View.VISIBLE);
        mSave.startAnimation(animationEnter);
        mSave.setVisibility(View.VISIBLE);
        mRotate.startAnimation(animationEnter);
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

        Animation animationEnter = AnimationUtils.loadAnimation(this, R.anim.bottom_to_top);
        Animation animationExit = AnimationUtils.loadAnimation(this, R.anim.top_to_bottom);
        mEdit.startAnimation(animationEnter);
        mEdit.setVisibility(View.VISIBLE);
        mCrop.startAnimation(animationExit);
        mCrop.setVisibility(View.GONE);
        mPaint.startAnimation(animationExit);
        mPaint.setVisibility(View.GONE);
        mSave.startAnimation(animationExit);
        mSave.setVisibility(View.GONE);
        mRotate.startAnimation(animationExit);
        mRotate.setVisibility(View.GONE);

        MyImageView currentImage = getCurrentView();
        if (currentImage != null) {
            currentImage.setRotation(0);
            currentImage.setImageBitmap(mCurrentBitmap);
            currentImage.setEditingMode(false);
            mViewPager.disableScroll(false);
        }

        actionBar.show();
    }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(Constants.VIBRATION_DURATION, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(Constants.VIBRATION_DURATION);
        }
    }
}