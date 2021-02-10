package com.example.myphotoeditor;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class Constants {
    public final static String IMAGE_TRANSITION_NAME = "TRANSITION_NAME";
    public final static String IMAGE_PATH = "PATH";
    public final static String IMAGE_NAME = "NAME";
    public final static String CHOSEN_FOLDER = "FOLDER";
    public final static String MY_DIRECTORY = "Photo Editor+";
    public final static String ALL_IMAGES = "All Images";
    public final static long TRANSITION_DURATION = 250;
    public final static float EXIT_DISTANCE = 500f;
    public final static long VIBRATION_DURATION = 20;
    public final static long RETURN_ANIMATION_DURATION = 100;
    public final static int REQUEST_CODE = 100;
    public final static String ADD_TAG = "Add text";
    public final static String EDIT_TAG = "Edit text";

    public static final String[] mFontNames = new String[] {"Default", "Alice", "Acme", "Atomic Age", "Montez"};
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Typeface[] getFonts(Resources resources) {
        return new Typeface[] {null, resources.getFont(R.font.alice), resources.getFont(R.font.acme),
                resources.getFont(R.font.atomic_age), resources.getFont(R.font.montez)};
    }
}
