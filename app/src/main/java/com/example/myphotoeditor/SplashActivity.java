package com.example.myphotoeditor;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        permission();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void start() {
        Thread loadPicsThread = new Thread(() -> {
            LoadedImages.folderMap = ReadInternalImages.getImageAndAlbums(SplashActivity.this);
            LoadedImages.allImages = ReadInternalImages.getImageList(SplashActivity.this);
            startActivity(new Intent(getApplicationContext(), FolderActivity.class));
            overridePendingTransition(R.anim.fadein, R.anim.fadeout_special);
            finish();
        });

        loadPicsThread.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void permission() {
        if (ContextCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            int PERMISSION_CODE = 100;
            ActivityCompat.requestPermissions(SplashActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_CODE);
        } else {
            start();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                start();
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                showDialog();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void showDialog() {
        AlertDialog.Builder builder = new  AlertDialog.Builder(this);
        String title = "Allow storage";
        String message = "PERMISSION_STORAGE";
        builder.setMessage(message).setTitle(title)
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) -> {
                    dialog.dismiss();
                    permission();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                });

        AlertDialog dialog = builder.create();
        dialog.show();

    }
}