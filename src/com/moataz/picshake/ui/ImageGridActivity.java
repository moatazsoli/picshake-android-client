/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.moataz.picshake.ui;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.example.android.displayingbitmaps.util.Utils;
import com.moataz.picshake.BuildConfig;

/**
 * Simple FragmentActivity to hold the main {@link ImageGridFragment} and not much else.
 */
public class ImageGridActivity extends FragmentActivity {
    private static final String TAG = "ImageGridActivity";
    private ArrayList <String> pics;
    private ArrayList <String> thumbs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            Utils.enableStrictMode();
        }
        
        super.onCreate(savedInstanceState);
        pics = (ArrayList<String>) getIntent().getSerializableExtra("pics");
        thumbs = (ArrayList<String>) getIntent().getSerializableExtra("thumbs");
        ImageGridFragment lFragment = new ImageGridFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("pics", pics);
        bundle.putStringArrayList("thumbs", thumbs);
        lFragment.setArguments(bundle);
        
        if (getSupportFragmentManager().findFragmentByTag(TAG) == null) {
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(android.R.id.content, lFragment, TAG);
            ft.commit();
        }
    }
}
