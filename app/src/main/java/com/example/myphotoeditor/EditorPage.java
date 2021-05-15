package com.example.myphotoeditor;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.app.SharedElementCallback;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.RectF;
import android.graphics.Typeface;
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
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import yuku.ambilwarna.AmbilWarnaDialog;

public class EditorPage extends AppCompatActivity implements ChangePaintThicknessDialog.ChangePaintThicknessDialogListener, OnAddTextListener {

    // FIELDS
    private static ActionBar mActionBar;
    private ConstraintLayout layout;
    private ArrayList<String> mFileNames;
    private MyViewPager mViewPager;
    private Button mEdit, mPaint, mRotate, mChooseColor, mChooseThickness, mSave, mDone, mCancel, mCrop, mSetCrop, mCancelCrop, mUndo, mChangeBrightnessContrast, mDoneCB, mCancelCB, mAddTexts, mAddText, mAddTextDone, mAddTextCancel;
    private int mDefColor;
    private Bitmap mCurrentBitmap;
    private TextView mContrastTV, mBrightnessTV;
    private LinearLayout mBG;
    private boolean mCBMode = false;
    private boolean mTextAdded = false, mAddedTextMode = false;
    public static boolean mSaved, mWasSaved = false;
    public static ArrayList<MyTextView> textViews;
    private Animation mAnimationEnter, mAnimationExit;
    private SeekBar mChangeBrightness, mChangeContrast;
    private float mCurrentBrightness, mCurrentContrast, mPrevBrightness, mPrevContrast;
    private ViewPagerAdapter adapter;
    private SavingDialog dialog;

    // OVERRIDDEN METHODS
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor_page);
        supportPostponeEnterTransition();

        dialog = new SavingDialog(this);

        mAnimationEnter = AnimationUtils.loadAnimation(this, R.anim.bottom_to_top);
        mAnimationExit = AnimationUtils.loadAnimation(this, R.anim.top_to_bottom);
        mSaved = false;

        mCurrentBrightness = 127f;
        mCurrentContrast = 1f;

        mPrevBrightness = 127f;
        mPrevContrast = 1f;

//        Window w = getWindow();
//        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//        w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        mFileNames = ImageListActivity.getFileNames();
        mDefColor = ContextCompat.getColor(this, R.color.white);

        getWindow().getSharedElementEnterTransition().setDuration(Constants.TRANSITION_DURATION);
        getWindow().getSharedElementReturnTransition().setDuration(Constants.TRANSITION_DURATION);

        mActionBar = getSupportActionBar();
        assert mActionBar != null;
        mActionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.special_transparent)));
        mActionBar.setDisplayHomeAsUpEnabled(true);

        Fade fade = new Fade();
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        fade.excludeTarget(android.R.id.navigationBarBackground, true);
        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);

        textViews = new ArrayList<>();

        load(setTransition());

        loadViews();
        setSeekBarActions();
        setMyViewPager();
    }

    private void setSeekBarActions() {
        mChangeBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                MyImageView imageView = getCurrentView();
                if (imageView != null) {
                    mCurrentBrightness = progress;
                    imageView.setColorFilter(setContrastAndBrightness(mCurrentContrast, mCurrentBrightness));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mChangeContrast.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                MyImageView imageView = getCurrentView();
                if (imageView != null) {
                    mCurrentContrast = progress / 100f;
                    imageView.setColorFilter(setContrastAndBrightness(mCurrentContrast, mCurrentBrightness));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void setMyViewPager() {
        mViewPager = findViewById(R.id.image_viewPager);

        adapter = new ViewPagerAdapter(this);

        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(ImageListActivity.position);

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
                mActionBar.setTitle(mFileNames.get(position));
                ImageListActivity.position = position;
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
            if (mAddedTextMode) {
                exitTextMode();
                for (MyTextView myTextView : textViews) {
                    layout.removeView(myTextView);
                }
                textViews.clear();
            } else if (imageView.getPaintMode()) {
                exitPaintMode();
                imageView.disablePaintMode();
            } else if (imageView.getCropMode()) {
                exitCropMode();
                imageView.disableCropMode();
            } else if (mCBMode) {
                imageView.setColorFilter(setContrastAndBrightness(mPrevContrast, mPrevBrightness));
                exitContrastBrightnessChangeMode();
            } else if (imageView.getEditingMode()) {
                imageView.resetDegrees();
                exitEditMode();
                imageView.disablePaintMode();
                imageView.disableCropMode();
            } else {
                if (mSaved) {
                    String load = "Loading the album";
                    Toast toast = Toast.makeText(this, load, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                super.onBackPressed();
                finishAfterTransition();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        vibrate();
        if (item.getItemId() == android.R.id.home) {
            finishAfterTransition();
            return true;
        }
        return false;
    }


    @Override
    public void finishAfterTransition() {
        mActionBar.hide();

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
        if (currentView != null) {
            currentView.rotate();
            if (currentView.getRotationDegrees() % 360 != 0 || MyImageView.mHasBeenCropped || MyImageView.mHasBeenPainted) {
                if (mSave.getVisibility() != View.VISIBLE) {
                    mSave.startAnimation(mAnimationEnter);
                    mSave.setVisibility(View.VISIBLE);
                }
            } else {
                mSave.startAnimation(mAnimationExit);
                mSave.setVisibility(View.GONE);
            }
        }
    }

    private void getFinalImage(MyImageView currentView, Toast toast) {

        if (currentView != null) {
            RectF rect = currentView.getImageRect();
            currentView.setDrawingCacheEnabled(true);
            currentView.buildDrawingCache();
            Bitmap bmp = Bitmap.createBitmap(currentView.getDrawingCache(), (int) rect.left, (int) rect.top, (int) rect.width(), (int) rect.height());
            currentView.setDrawingCacheEnabled(false);
            saveImage(bmp);
            currentView.post(() -> {
                currentView.setImageBitmap(mCurrentBitmap);
                dialog.dismiss();
                adapter.notifyDataSetChanged();
                toast.show();
                exitEditMode();

                currentView.disablePaintMode();
                currentView.disableCropMode();

                mSaved = true;
                mWasSaved = true;
            });
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void save(View view) {

        vibrate();

        String saved = "Image saved in Gallery";
        @SuppressLint("ShowToast") Toast saveToast = Toast.makeText(this, saved, Toast.LENGTH_SHORT);
        saveToast.setText(saved);
        saveToast.setDuration(Toast.LENGTH_SHORT);
        saveToast.setGravity(Gravity.CENTER, 0, 0);

        MyImageView currentView = getCurrentView();
        dialog.start();
        new Thread(() -> getFinalImage(currentView, saveToast)).start();


    }

    private void saveImage(Bitmap bitmap) {
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        String folderName = Constants.MY_DIRECTORY;
        File myDirectory = new File(root + "/" + folderName);
        if (!myDirectory.exists())
            myDirectory.mkdirs();
        StringBuilder stringBuilder = new StringBuilder(Constants.MY_DIRECTORY);
        String rev = stringBuilder.reverse().toString();
        String imageName = Constants.MY_DIRECTORY + "_" + System.currentTimeMillis() + "_" + rev + ".jpg";
        File image = new File(myDirectory, imageName);
        LoadedImages.allImages.add(0, image.getAbsolutePath());
        ArrayList<String> myFolder = LoadedImages.folderMap.remove(Constants.MY_DIRECTORY);
        if (myFolder == null)
            myFolder = new ArrayList<>();
        myFolder.add(0, image.getAbsolutePath());
        LoadedImages.folderMap.put(Constants.MY_DIRECTORY, myFolder);

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
        if (currentView != null) {
            currentView.cancel();
            if (MyImageView.mHasBeenPainted || MyImageView.mHasBeenCropped || currentView.getRotationDegrees() % 360 != 0) {
                mSave.startAnimation(mAnimationEnter);
                mSave.setVisibility(View.VISIBLE);
            }
        }
    }

    public void done(View view) {
        vibrate();
        MyImageView currentView = getCurrentView();
        exitPaintMode();
        if (currentView != null) {
            currentView.setColorFilter(setContrastAndBrightness(1f, 127f));
            currentView.done();
            if (MyImageView.mHasBeenPainted || MyImageView.mHasBeenCropped || currentView.getRotationDegrees() % 360 != 0) {
                mSave.startAnimation(mAnimationEnter);
                mSave.setVisibility(View.VISIBLE);
            }
            currentView.setColorFilter(setContrastAndBrightness(mCurrentContrast, mCurrentBrightness));
        }
    }

    private void enterPaintMode() {
        mChooseColor.startAnimation(mAnimationEnter);
        mChooseColor.setVisibility(View.VISIBLE);
        mChooseThickness.startAnimation(mAnimationEnter);
        mChooseThickness.setVisibility(View.VISIBLE);
        mAddTexts.startAnimation(mAnimationExit);
        mAddTexts.setVisibility(View.GONE);
        mCancel.startAnimation(mAnimationEnter);
        mCancel.setVisibility(View.VISIBLE);
        mSave.startAnimation(mAnimationExit);
        mSave.setVisibility(View.GONE);
        mCrop.startAnimation(mAnimationExit);
        mCrop.setVisibility(View.GONE);
        mChangeBrightnessContrast.startAnimation(mAnimationExit);
        mChangeBrightnessContrast.setVisibility(View.GONE);
        mPaint.startAnimation(mAnimationExit);
        mPaint.setVisibility(View.GONE);
        mRotate.startAnimation(mAnimationExit);
        mRotate.setVisibility(View.GONE);
    }

    private void exitPaintMode() {
        mChooseColor.startAnimation(mAnimationExit);
        mChooseColor.setVisibility(View.GONE);
        mChooseThickness.startAnimation(mAnimationExit);
        mChooseThickness.setVisibility(View.GONE);
        mAddTexts.startAnimation(mAnimationEnter);
        mAddTexts.setVisibility(View.VISIBLE);
        mCancel.startAnimation(mAnimationExit);
        mCancel.setVisibility(View.GONE);
        mUndo.startAnimation(mAnimationExit);
        mUndo.setVisibility(View.GONE);
        mDone.startAnimation(mAnimationExit);
        mDone.setVisibility(View.GONE);
        mCrop.startAnimation(mAnimationEnter);
        mCrop.setVisibility(View.VISIBLE);
        mChangeBrightnessContrast.startAnimation(mAnimationEnter);
        mChangeBrightnessContrast.setVisibility(View.VISIBLE);
        mPaint.startAnimation(mAnimationEnter);
        mPaint.setVisibility(View.VISIBLE);
        mRotate.startAnimation(mAnimationEnter);
        mRotate.setVisibility(View.VISIBLE);

    }

    private void enterCropMode() {
        mCancelCrop.startAnimation(mAnimationEnter);
        mCancelCrop.setVisibility(View.VISIBLE);
        mSetCrop.startAnimation(mAnimationEnter);
        mSetCrop.setVisibility(View.VISIBLE);
        mSave.startAnimation(mAnimationExit);
        mSave.setVisibility(View.GONE);
        mAddTexts.startAnimation(mAnimationExit);
        mAddTexts.setVisibility(View.GONE);
        mCrop.startAnimation(mAnimationExit);
        mCrop.setVisibility(View.GONE);
        mChangeBrightnessContrast.startAnimation(mAnimationExit);
        mChangeBrightnessContrast.setVisibility(View.GONE);
        mPaint.startAnimation(mAnimationExit);
        mPaint.setVisibility(View.GONE);
        mRotate.startAnimation(mAnimationExit);
        mRotate.setVisibility(View.GONE);
    }

    private void exitCropMode() {
        mSetCrop.startAnimation(mAnimationExit);
        mSetCrop.setVisibility(View.GONE);
        mCancelCrop.startAnimation(mAnimationExit);
        mCancelCrop.setVisibility(View.GONE);
        mAddTexts.startAnimation(mAnimationEnter);
        mAddTexts.setVisibility(View.VISIBLE);
        mCrop.startAnimation(mAnimationEnter);
        mCrop.setVisibility(View.VISIBLE);
        mChangeBrightnessContrast.startAnimation(mAnimationEnter);
        mChangeBrightnessContrast.setVisibility(View.VISIBLE);
        mPaint.startAnimation(mAnimationEnter);
        mPaint.setVisibility(View.VISIBLE);
        mRotate.startAnimation(mAnimationEnter);
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
        if (currentView != null) {
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
            if (MyImageView.mHasBeenCropped || MyImageView.mHasBeenPainted || currentView.getRotationDegrees() % 360 != 0) {
                mSave.startAnimation(mAnimationEnter);
                mSave.setVisibility(View.VISIBLE);
            }
        }
    }

    public void cancelCrop(View view) {
        vibrate();
        exitCropMode();
        MyImageView currentView = getCurrentView();
        if (currentView != null) {
            currentView.cancelCrop();
            if (MyImageView.mHasBeenCropped || MyImageView.mHasBeenPainted || currentView.getRotationDegrees() % 360 != 0) {
                mSave.startAnimation(mAnimationEnter);
                mSave.setVisibility(View.VISIBLE);
            }
        }
    }

    private void loadViews() {
        layout = findViewById(R.id.activity);
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
        mChooseThickness = findViewById(R.id.button_changeThickness);
        mChangeBrightnessContrast = findViewById(R.id.button_changeBrightnessContrast);
        mDoneCB = findViewById(R.id.button_doneCB);
        mCancelCB = findViewById(R.id.button_cancelCB);
        mChangeBrightness = findViewById(R.id.seekBar_changeBrightness);
        mChangeContrast = findViewById(R.id.seekBar_changeContrast);
        mContrastTV = findViewById(R.id.textView_contrast);
        mBrightnessTV = findViewById(R.id.textView_brightness);
        mBG = findViewById(R.id.linearLayout_bg);

        mAddTexts = findViewById(R.id.button_addTexts);
        mAddText = findViewById(R.id.button_addText);
        mAddTextCancel = findViewById(R.id.button_addTextCancel);
        mAddTextDone = findViewById(R.id.button_addTextDone);

        mBG.bringToFront();
        mContrastTV.bringToFront();
        mBrightnessTV.bringToFront();
        mChangeBrightness.bringToFront();
        mChangeContrast.bringToFront();
    }

    public static ActionBar getMyActionBar() {
        return mActionBar;
    }

    private void load(String info) {
        mActionBar.setTitle(info);
    }

    private String setTransition() {
        return getIntent().getExtras().getString(Constants.IMAGE_NAME);
    }

    private MyImageView getCurrentView() {
        try {
            return mViewPager.findViewWithTag(ImageListActivity.position);
        } catch (NullPointerException | IndexOutOfBoundsException exception) {
            return null;
        }
    }

    public void edit(View view) {
        vibrate();
        mCurrentBrightness = 127f;
        mCurrentContrast = 1f;
        mPrevBrightness = 127f;
        mPrevContrast = 1f;
        mChangeBrightness.setProgress(127);
        mChangeContrast.setProgress(100);
        if (getCurrentView() != null) {
            mCurrentBitmap = Bitmap.createBitmap(getCurrentView().getImageBitmap());
            getCurrentView().resetDegrees();
        }
        enterEditMode();
    }

    private void enterEditMode() {
        mEdit.startAnimation(mAnimationExit);
        mEdit.setVisibility(View.GONE);
        mCrop.startAnimation(mAnimationEnter);
        mCrop.setVisibility(View.VISIBLE);
        mChangeBrightnessContrast.startAnimation(mAnimationEnter);
        mChangeBrightnessContrast.setVisibility(View.VISIBLE);
        mPaint.startAnimation(mAnimationEnter);
        mPaint.setVisibility(View.VISIBLE);
        mRotate.startAnimation(mAnimationEnter);
        mRotate.setVisibility(View.VISIBLE);
        mAddTexts.startAnimation(mAnimationEnter);
        mAddTexts.setVisibility(View.VISIBLE);

        MyImageView currentImage = getCurrentView();
        if (currentImage != null) {
            currentImage.setEditingMode(true);
            mViewPager.disableScroll(true);
        }

        mActionBar.hide();
    }

    private void exitEditMode() {
        MyImageView.mHasBeenPainted = false;
        MyImageView.mHasBeenCropped = false;
        mTextAdded = false;
        mEdit.startAnimation(mAnimationEnter);
        mEdit.setVisibility(View.VISIBLE);
        mAddTexts.startAnimation(mAnimationExit);
        mAddTexts.setVisibility(View.GONE);
        mCrop.startAnimation(mAnimationExit);
        mCrop.setVisibility(View.GONE);
        mChangeBrightnessContrast.startAnimation(mAnimationExit);
        mChangeBrightnessContrast.setVisibility(View.GONE);
        mPaint.startAnimation(mAnimationExit);
        mPaint.setVisibility(View.GONE);
        if (mSave.getVisibility() == View.VISIBLE)
            mSave.startAnimation(mAnimationExit);
        mSave.setVisibility(View.GONE);
        mRotate.startAnimation(mAnimationExit);
        mRotate.setVisibility(View.GONE);

        MyImageView currentImage = getCurrentView();
        if (currentImage != null) {
            currentImage.setColorFilter(setContrastAndBrightness(1, 127));
            currentImage.setRotation(0);
            currentImage.setEditingMode(false);
            mViewPager.disableScroll(false);
        }

        mActionBar.show();
    }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(Constants.VIBRATION_DURATION, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(Constants.VIBRATION_DURATION);
        }
    }

    @Override
    public void applyThickness(float thickness) {
        MyImageView imageView = getCurrentView();
        if (imageView != null) {
            imageView.setCurrentPaintThickness(thickness);
        }
    }

    public void changeThickness(View view) {
        vibrate();
        MyImageView imageView = getCurrentView();
        if (imageView != null) {
            ChangePaintThicknessDialog dialog = new ChangePaintThicknessDialog(mDefColor, imageView.getCurrentPaintThickness());
            dialog.show(getSupportFragmentManager(), "Change Thickness");
        }
    }

    private void enterContrastBrightnessChangeMode() {
        mCBMode = true;
        mChangeContrast.startAnimation(mAnimationEnter);
        mChangeContrast.setVisibility(View.VISIBLE);
        mChangeBrightness.startAnimation(mAnimationEnter);
        mChangeBrightness.setVisibility(View.VISIBLE);
        mDoneCB.startAnimation(mAnimationEnter);
        mDoneCB.setVisibility(View.VISIBLE);
        mCancelCB.startAnimation(mAnimationEnter);
        mCancelCB.setVisibility(View.VISIBLE);
        mAddTexts.startAnimation(mAnimationExit);
        mAddTexts.setVisibility(View.GONE);
        mBrightnessTV.startAnimation(mAnimationEnter);
        mBrightnessTV.setVisibility(View.VISIBLE);
        mBG.startAnimation(mAnimationEnter);
        mBG.setVisibility(View.VISIBLE);
        mContrastTV.startAnimation(mAnimationEnter);
        mContrastTV.setVisibility(View.VISIBLE);
        mSave.startAnimation(mAnimationExit);
        mSave.setVisibility(View.GONE);
        mCrop.startAnimation(mAnimationExit);
        mCrop.setVisibility(View.GONE);
        mChangeBrightnessContrast.startAnimation(mAnimationExit);
        mChangeBrightnessContrast.setVisibility(View.GONE);
        mPaint.startAnimation(mAnimationExit);
        mPaint.setVisibility(View.GONE);
        mRotate.startAnimation(mAnimationExit);
        mRotate.setVisibility(View.GONE);
    }

    private void exitContrastBrightnessChangeMode() {
        mCBMode = false;
        mChangeContrast.startAnimation(mAnimationExit);
        mChangeContrast.setVisibility(View.GONE);
        mChangeBrightness.startAnimation(mAnimationExit);
        mChangeBrightness.setVisibility(View.GONE);
        mDoneCB.startAnimation(mAnimationExit);
        mDoneCB.setVisibility(View.GONE);
        mAddTexts.startAnimation(mAnimationEnter);
        mAddTexts.setVisibility(View.VISIBLE);
        mCancelCB.startAnimation(mAnimationExit);
        mCancelCB.setVisibility(View.GONE);
        mBrightnessTV.startAnimation(mAnimationExit);
        mBrightnessTV.setVisibility(View.GONE);
        mBG.startAnimation(mAnimationExit);
        mBG.setVisibility(View.GONE);
        mContrastTV.startAnimation(mAnimationExit);
        mContrastTV.setVisibility(View.GONE);
        if (mCurrentBrightness != 127 || mCurrentContrast != 1) {
            mSave.startAnimation(mAnimationEnter);
            mSave.setVisibility(View.VISIBLE);
        }
        mCrop.startAnimation(mAnimationEnter);
        mCrop.setVisibility(View.VISIBLE);
        mChangeBrightnessContrast.startAnimation(mAnimationEnter);
        mChangeBrightnessContrast.setVisibility(View.VISIBLE);
        mPaint.startAnimation(mAnimationEnter);
        mPaint.setVisibility(View.VISIBLE);
        mRotate.startAnimation(mAnimationEnter);
        mRotate.setVisibility(View.VISIBLE);
    }

    public void doneCB(View view) {
        vibrate();
        mPrevContrast = mCurrentContrast;
        mPrevBrightness = mCurrentBrightness;
        exitContrastBrightnessChangeMode();
    }

    public void cancelCB(View view) {
        vibrate();
        if (getCurrentView() != null) {
            getCurrentView().setColorFilter(setContrastAndBrightness(mPrevContrast, mPrevBrightness));
        }
        exitContrastBrightnessChangeMode();
    }

    public void changeBrightnessContrast(View view) {
        vibrate();
        mChangeContrast.setProgress((int) (mPrevContrast * 100));
        mChangeBrightness.setProgress((int) mPrevBrightness);
        enterContrastBrightnessChangeMode();
    }

    public ColorMatrixColorFilter setContrastAndBrightness(float contrast, float brightness) {
        brightness -= 127;
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                });

        return new ColorMatrixColorFilter(cm);
    }

    public void addText(View view) {
        vibrate();
        AddTextDialog dialog = new AddTextDialog(this, "New Text", ContextCompat.getColor(this, R.color.white), 30, null, null);
        dialog.show(getSupportFragmentManager(), Constants.ADD_TAG);
    }

    private void enterTextMode() {
        mAddedTextMode = true;

        mAddTextCancel.startAnimation(mAnimationEnter);
        mAddTextCancel.setVisibility(View.VISIBLE);
        mAddText.startAnimation(mAnimationEnter);
        mAddText.setVisibility(View.VISIBLE);
        mCrop.startAnimation(mAnimationExit);
        mCrop.setVisibility(View.GONE);
        mSave.startAnimation(mAnimationExit);
        mSave.setVisibility(View.GONE);
        mAddTexts.startAnimation(mAnimationExit);
        mAddTexts.setVisibility(View.GONE);
        mChangeBrightnessContrast.startAnimation(mAnimationExit);
        mChangeBrightnessContrast.setVisibility(View.GONE);
        mPaint.startAnimation(mAnimationExit);
        mPaint.setVisibility(View.GONE);
        mRotate.startAnimation(mAnimationExit);
        mRotate.setVisibility(View.GONE);
    }

    private void exitTextMode() {
        mAddedTextMode = false;
        if (mAddTextDone.getVisibility() == View.VISIBLE) {
            mAddTextDone.startAnimation(mAnimationExit);
            mAddTextDone.setVisibility(View.GONE);
        }
        mAddTextCancel.startAnimation(mAnimationExit);
        mAddTextCancel.setVisibility(View.GONE);
        mAddText.startAnimation(mAnimationExit);
        mAddText.setVisibility(View.GONE);
        mCrop.startAnimation(mAnimationEnter);
        mCrop.setVisibility(View.VISIBLE);
        if (textViews.size() >= 1 || mTextAdded) {
            mSave.startAnimation(mAnimationEnter);
            mSave.setVisibility(View.VISIBLE);
            mTextAdded = true;
        }
        mAddTexts.startAnimation(mAnimationEnter);
        mAddTexts.setVisibility(View.VISIBLE);
        mChangeBrightnessContrast.startAnimation(mAnimationEnter);
        mChangeBrightnessContrast.setVisibility(View.VISIBLE);
        mPaint.startAnimation(mAnimationEnter);
        mPaint.setVisibility(View.VISIBLE);
        mRotate.startAnimation(mAnimationEnter);
        mRotate.setVisibility(View.VISIBLE);
    }

    public void addTexts(View view) {
        enterTextMode();
        vibrate();
    }

    public void addTextCancel(View view) {
        vibrate();
        for (MyTextView myTextView : textViews) {
            layout.removeView(myTextView);
        }
        textViews.clear();
        exitTextMode();
    }

    public void addTextDone(View view) {
        vibrate();
        MyImageView imageView = getCurrentView();
        if (imageView != null) {
            imageView.setColorFilter(setContrastAndBrightness(1f, 127f));
            imageView.startPaint();
            imageView.takeImageSnap();
            imageView.clearPaints();
            imageView.setColorFilter(setContrastAndBrightness(mCurrentContrast, mCurrentBrightness));
        }
        for (MyTextView myTextView : textViews) {
            layout.removeView(myTextView);
        }
        exitTextMode();
        textViews.clear();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onTextAdded(String text, boolean deleted, int color, int size, MyTextView tvInFocus, Typeface font) {
        MyTextView textView = new MyTextView(this);
        textView.setText(text);
        textView.setTextSize(size);
        textView.setTextColor(color);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setParams(text, color, size);

        layout.addView(textView);
        textView.setTypeface(font);
        textViews.add(textView);
        if (deleted) {
            layout.removeView(textView);
            textViews.remove(textView);
        }
        if (textViews.size() == 1) {
            mAddTextDone.startAnimation(mAnimationEnter);
            mAddTextDone.setVisibility(View.VISIBLE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onTextUpdated(String text, boolean deleted, int color, int size, MyTextView textView, Typeface font) {
        if (deleted) {
            layout.removeView(textView);
            textViews.remove(textView);
            if (textViews.size() == 0) {
                mAddTextDone.startAnimation(mAnimationExit);
                mAddTextDone.setVisibility(View.GONE);
            }
        }
        textView.setText(text);
        textView.setTextSize(size);
        textView.setTextColor(color);
        textView.setParams(text, color, size);
        textView.setTypeface(font);
    }
}