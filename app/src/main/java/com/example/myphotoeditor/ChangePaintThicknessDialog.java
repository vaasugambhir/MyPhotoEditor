package com.example.myphotoeditor;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class ChangePaintThicknessDialog extends AppCompatDialogFragment {

    private ChangePaintThicknessView changePaintThicknessView;
    private SeekBar seekBar;
    private View thisView;
    private final float mThickness;
    private final int color;
    private ChangePaintThicknessDialogListener listener;

    @SuppressLint("InflateParams")
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (ChangePaintThicknessDialogListener) context;
        assert (getActivity()!=null);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        thisView = inflater.inflate(R.layout.change_thickness_layout, null);
        changePaintThicknessView = thisView.findViewById(R.id.layout_paint_thickness);
        changePaintThicknessView.setCurrentColor(color);
        changePaintThicknessView.setThickness(10f);
        seekBar = thisView.findViewById(R.id.seekBar_change_thickness);
        seekBar.setProgress((int) (mThickness-10f));
        changePaintThicknessView.setThickness(mThickness);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                changePaintThicknessView.setThickness(10f + (progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public ChangePaintThicknessDialog(int color, float thickness) {
        this.color = color;
        this.mThickness = thickness;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(thisView)
                .setTitle("Choose thickness")
                .setNegativeButton("cancel", (dialog, which) -> listener.applyThickness(mThickness))
                .setPositiveButton("set", (dialog, which) -> {
                    float thickness = 10f + seekBar.getProgress();
                    listener.applyThickness(thickness);
                });
        return builder.create();
    }

    public interface ChangePaintThicknessDialogListener {
        void applyThickness(float thickness);
    }
}
