package com.example.myphotoeditor;

import android.app.SharedElementCallback;
import android.view.View;

import androidx.core.view.ViewCompat;

import java.util.List;
import java.util.Map;

public class CustomSharedElementCallback extends SharedElementCallback {

    private View mView;

    public void setView(View view) {
        mView = view;
    }

    @Override
    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
        names.clear();
        sharedElements.clear();

        String transitionName = ViewCompat.getTransitionName(mView);
        names.add(transitionName);
        sharedElements.put(transitionName, mView);
    }
}
