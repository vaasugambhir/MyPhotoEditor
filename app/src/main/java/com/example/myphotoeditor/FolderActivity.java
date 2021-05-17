package com.example.myphotoeditor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.transition.Fade;

import java.util.ArrayList;
import java.util.Map;

public class FolderActivity extends AppCompatActivity {

    private static ArrayList<String> chosenImages;
    private FolderAdapter adapter;
    private ArrayList<String> folderNames;
    private ArrayList<String> imagePaths;
    private ArrayList<Integer> count;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);
        Fade fade = new Fade();
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        fade.excludeTarget(android.R.id.navigationBarBackground, true);
        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);
        permissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LoadedImages.imageCount = 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void set() {
        RecyclerView myList = findViewById(R.id.recyclerView_folder);

        folderNames = new ArrayList<>();
        imagePaths = new ArrayList<>();
        count = new ArrayList<>();

        for (Map.Entry<String, ArrayList<String>> entry : LoadedImages.folderMap.entrySet()) {
            folderNames.add(entry.getKey());
            if (!entry.getValue().isEmpty()) {
                imagePaths.add(entry.getValue().get(0));
                count.add(entry.getValue().size());
            } else {
                imagePaths.add("");
                count.add(0);
            }
        }

        folderNames.add(0, Constants.ALL_IMAGES);
        imagePaths.add(0, LoadedImages.allImages.get(0));
        count.add(0, LoadedImages.allImages.size());

        adapter = new FolderAdapter(this, (pos, textView) -> {
            Intent intent = new Intent(getApplicationContext(), ImageListActivity.class);
            intent.putExtra(Constants.CHOSEN_FOLDER, folderNames.get(pos));
            chosenImages = LoadedImages.folderMap.get(folderNames.get(pos));
            startActivityForResult(intent, 10101);
            overridePendingTransition(R.anim.fadein_special, R.anim.fadeout);
        });
        adapter.add(folderNames, imagePaths, count);
        myList.setAdapter(adapter);
        myList.setLayoutManager(new LinearLayoutManager(this));
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void permissions() {
        if (ActivityCompat.checkSelfPermission(FolderActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            int PERMISSION_CODE = 100;
            ActivityCompat.requestPermissions(FolderActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_CODE);
        } else {
            set();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100)
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                set();
            }
    }

    public static ArrayList<String> getChosenImages() {
        return chosenImages;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (EditorPage.mWasSaved) {
            update();
            EditorPage.mWasSaved = false;
        }
    }

    void update() {
        LoadedImages.folderMap.remove(Constants.ALL_IMAGES);

        folderNames = new ArrayList<>();
        imagePaths = new ArrayList<>();
        count = new ArrayList<>();

        for (Map.Entry<String, ArrayList<String>> entry : LoadedImages.folderMap.entrySet()) {
            folderNames.add(entry.getKey());
            if (!entry.getValue().isEmpty()) {
                imagePaths.add(entry.getValue().get(0));
                count.add(entry.getValue().size());
            } else {
                imagePaths.add("");
                count.add(0);
            }
        }

        folderNames.add(0, Constants.ALL_IMAGES);
        imagePaths.add(0, LoadedImages.allImages.get(0));
        count.add(0, LoadedImages.allImages.size());

        adapter.add(folderNames, imagePaths, count);
        adapter.notifyDataSetChanged();
    }
}