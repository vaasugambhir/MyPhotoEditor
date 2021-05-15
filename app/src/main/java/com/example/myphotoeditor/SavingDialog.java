package com.example.myphotoeditor;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

public class SavingDialog {
    private AlertDialog dialog;
    private final Activity activity;

    public SavingDialog(Activity activity) {
        this.activity = activity;
    }

    public void start() {
        AlertDialog.Builder builder =new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.save_alert_dialog, null));
        builder.setCancelable(false);
        dialog = builder.create();
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }
}
