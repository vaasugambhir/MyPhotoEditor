package com.example.myphotoeditor;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import yuku.ambilwarna.AmbilWarnaDialog;

@SuppressLint("InflateParams")
public class AddTextDialog extends AppCompatDialogFragment {

    private OnAddTextListener listener;
    private EditText mEditAddText;
    private TextView mTextDisplay, mSizeCounter;
    private Button mChooseColor, mChooseFont;
    private final String mDefText;
    private final Context mContext;
    private final int mDefColor;
    private int mCurrentColor;
    private final int mDefSize;
    private int mCurrentSize;
    private SeekBar mTextSizeSeekBar;
    private RecyclerView mFontList;
    private LinearLayout layout1, layout2;
    private final MyTextView mTvInFocus;
    private Typeface mCurrentFont;
    private final Typeface mDefFont;
    private FontListAdapter mAdapter;

    AddTextDialog(Context context, String defText, int defColor, int defSize, MyTextView tvInFocus, Typeface defFont) {
        mDefText = defText;
        mDefColor = defColor;
        mContext = context;
        mCurrentColor = defColor;
        mDefSize = defSize;
        mCurrentSize = defSize;
        mTvInFocus = tvInFocus;
        mCurrentFont = defFont;
        mDefFont = defFont;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (OnAddTextListener) context;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View thisView = Objects.requireNonNull(getActivity()).getLayoutInflater().inflate(R.layout.layout_add_text, null);
        String posButtonName, negButtonName;
        assert getTag() != null;
        if (getTag().equals(Constants.ADD_TAG)) {
            posButtonName = "add";
            negButtonName = "cancel";
            builder.setView(thisView)
                    .setTitle("Preferences")
                    .setPositiveButton(posButtonName, (dialog, which) -> {
                        String newText = mEditAddText.getText().toString();
                        listener.onTextAdded(newText, false, mCurrentColor, mCurrentSize, mTvInFocus, mCurrentFont);
                    })
                    .setNegativeButton(negButtonName, (dialog, which) -> listener.onTextAdded(mDefText, true, mDefColor, mDefSize, mTvInFocus, mDefFont));
        } else {
            posButtonName = "set";
            negButtonName = "delete";
            builder.setView(thisView)
                    .setTitle("Preferences")
                    .setPositiveButton(posButtonName, (dialog, which) -> {
                        String newText = mEditAddText.getText().toString();
                        listener.onTextUpdated(newText, false, mCurrentColor, mCurrentSize, mTvInFocus, mCurrentFont);
                    })
                    .setNegativeButton(negButtonName, (dialog, which) -> listener.onTextUpdated(mDefText, true, mDefColor, mDefSize, mTvInFocus, mDefFont));
        }

        mEditAddText = thisView.findViewById(R.id.editText_addedText);
        mTextDisplay = thisView.findViewById(R.id.textView_addedText);
        mChooseColor = thisView.findViewById(R.id.button_chooseAddTextColor);
        mChooseFont = thisView.findViewById(R.id.button_chooseAddTextFont);
        mTextSizeSeekBar = thisView.findViewById(R.id.seekBar_chooseAddTextSize);
        mSizeCounter = thisView.findViewById(R.id.textView_addedTextSizeCounter);
        mFontList = thisView.findViewById(R.id.recyclerView_fontList);
        layout1 = thisView.findViewById(R.id.layout1);
        layout2 = thisView.findViewById(R.id.layout2);

        setList();
        setDefValues();
        setButtonListeners();
        addTextChangeListener();
        setSeekBarChangeListener();

        return builder.create();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setList() {
        mAdapter = new FontListAdapter(getResources(), position -> {
            mCurrentFont = (Constants.getFonts(getResources()))[position];
            layout1.setVisibility(View.VISIBLE);
            layout2.setVisibility(View.GONE);
            mTextDisplay.setTypeface(mCurrentFont);
        });
        mAdapter.add(mCurrentSize, mCurrentColor);
        mFontList.setLayoutManager(new LinearLayoutManager(mContext));
        mFontList.setAdapter(mAdapter);
    }

    private void setDefValues() {
        mTextDisplay.setTypeface(mDefFont);
        mTextSizeSeekBar.setProgress(mDefSize);
        String text = "Size: " + mDefSize;
        mSizeCounter.setText(text);
        mTextDisplay.setText(mDefText);
        mTextDisplay.setTextColor(mDefColor);
        mTextDisplay.setTextSize(mDefSize);
        mEditAddText.setText(mDefText);
    }

    private void setSeekBarChangeListener() {
        mTextSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mCurrentSize = progress;
                mTextDisplay.setTextSize(mCurrentSize);
                String text = "Size: " + progress;
                mSizeCounter.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mAdapter.add(mCurrentSize, mCurrentColor);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void setButtonListeners() {

        mChooseFont.setOnClickListener(v -> {
            layout2.setVisibility(View.VISIBLE);
            layout1.setVisibility(View.GONE);
        });

        mChooseColor.setOnClickListener(v -> {
            AmbilWarnaDialog dialog = new AmbilWarnaDialog(mContext, mDefColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                @Override
                public void onCancel(AmbilWarnaDialog dialog) {

                }

                @Override
                public void onOk(AmbilWarnaDialog dialog, int color) {
                    mCurrentColor = color;
                    mAdapter.add(mCurrentSize, mCurrentColor);
                    mAdapter.notifyDataSetChanged();
                    mTextDisplay.setTextColor(mCurrentColor);
                }
            });
            dialog.show();
        });

    }

    private void addTextChangeListener() {
        mEditAddText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mTextDisplay.setText(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}

interface OnAddTextListener {
    void onTextAdded(String text, boolean deleted, int color, int size, MyTextView textView, Typeface font);
    void onTextUpdated(String text, boolean deleted, int color, int size, MyTextView textView, Typeface font);
}
