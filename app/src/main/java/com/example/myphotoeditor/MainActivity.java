package com.example.myphotoeditor;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.transition.Fade;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private TextView mImageCount;
    private RecyclerView mImageList;

    public static int position = 0;
    private static ArrayList<String> mFilePaths, mFileNames;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissions();
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Choose a picture");
        setSupportActionBar(toolbar);

        getWindow().getSharedElementEnterTransition().setDuration(Constants.TRANSITION_DURATION);
        getWindow().getSharedElementReturnTransition().setDuration(Constants.TRANSITION_DURATION);

        Fade fade = new Fade();
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        fade.excludeTarget(android.R.id.navigationBarBackground, true);
        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);

        mImageCount = findViewById(R.id.textView_image_count);
        mImageList = findViewById(R.id.recyclerView_image_list);
        mFileNames = new ArrayList<>();

        fetchingData();
        setAdapter();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void permissions() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            int PERMISSION_CODE = 100;
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_CODE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void fetchingData() {
        mFilePaths = ReadInternalImages.getImageList(this);
        int imageCount = mFilePaths.size();
        String count = "Displaying " + imageCount + " images";
        mImageCount.setText(count);

        for (String path : mFilePaths) {
            int lastIndex = path.lastIndexOf('/');
            String name = path.substring(lastIndex + 1);
            mFileNames.add(name);
        }
    }

    private void setAdapter() {
        ImageAdapter adapter = new ImageAdapter(this);
        adapter.add(mFilePaths, mFileNames);
        adapter.addOnClickListener((image, path, name, pos) -> {
            Intent intent = new Intent(MainActivity.this, EditorPage.class);
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(MainActivity.this,
                            image, Objects.requireNonNull(ViewCompat.getTransitionName(image)));
            intent.putExtra(Constants.IMAGE_TRANSITION_NAME, ViewCompat.getTransitionName(image));
            intent.putExtra(Constants.IMAGE_PATH, path);
            intent.putExtra(Constants.IMAGE_NAME, name);
            position = pos;
            startActivity(intent, optionsCompat.toBundle());
        });
        mImageList.setAdapter(adapter);
        mImageList.setLayoutManager(new GridLayoutManager(this, 4));
    }

    public static ArrayList<String> getFileNames() {
        return mFileNames;
    }

    public static ArrayList<String> getFilePaths() {
        return mFilePaths;
    }
}