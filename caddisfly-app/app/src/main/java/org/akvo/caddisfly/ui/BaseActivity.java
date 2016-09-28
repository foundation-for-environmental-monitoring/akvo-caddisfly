/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly
 *
 * Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.ui;

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.util.PreferencesUtil;

/**
 * The base activity with common functions
 */
public abstract class BaseActivity extends AppCompatActivity {

    private final static String THEME_BLUE = "Blue";
    private final static String THEME_BLUE_ORANGE = "BlueOrange";
    private final static String THEME_ORANGE_BLUE = "OrangeBlue";
    private final static String THEME_FLOW = "Flow";
    protected static ResultListener listener;
    private String mTitle;

    protected static void setResultListener(AppCompatActivity activity) {
        listener = (ResultListener) activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateTheme();
        changeActionBarStyleBasedOnCurrentMode();
    }

    private void updateTheme() {

        String theme = PreferencesUtil.getString(this, getString(R.string.selectedThemeKey), "Orange");

        switch (theme) {
            case THEME_BLUE:
                setTheme(R.style.AppTheme_Blue);
                break;
            case THEME_BLUE_ORANGE:
                setTheme(R.style.AppTheme_BlueOrange);
                break;
            case THEME_ORANGE_BLUE:
                setTheme(R.style.AppTheme_OrangeBlue);
                break;
            case THEME_FLOW:
                setTheme(R.style.AppTheme_Flow);
                break;
            default:
                setTheme(R.style.AppTheme_Orange);
                break;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.windowBackground, typedValue, true);
        int windowBackground = typedValue.data;
        getWindow().setBackgroundDrawable(new ColorDrawable(windowBackground));

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            try {
                setSupportActionBar(toolbar);
            } catch (Throwable t) {
                //Ignore crash in Samsung
            }
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        changeActionBarStyleBasedOnCurrentMode();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        setTitle(mTitle);
    }

    @Override
    public void setTitle(CharSequence title) {
        TextView textTitle = (TextView) findViewById(R.id.textToolbarTitle);
        if (textTitle != null && title != null) {
            mTitle = title.toString();
            textTitle.setText(title);
        }
    }

    @Override
    public void setTitle(int titleId) {
        TextView textTitle = (TextView) findViewById(R.id.textToolbarTitle);
        if (textTitle != null && titleId != 0) {
            mTitle = getString(titleId);
            textTitle.setText(titleId);
        }
    }

    /**
     * Changes the action bar style depending on if the app is in user mode or diagnostic mode
     * This serves as a visual indication as to what mode the app is running in
     */
    void changeActionBarStyleBasedOnCurrentMode() {
        if (AppPreferences.isDiagnosticMode()) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(
                        ContextCompat.getColor(this, R.color.diagnostic)));
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.diagnostic_status));
            }
            LinearLayout layoutTitle = (LinearLayout) findViewById(R.id.layoutTitleBar);
            if (layoutTitle != null) {
                layoutTitle.setBackgroundColor(ContextCompat.getColor(this, R.color.diagnostic));
            }

        } else {

            TypedValue typedValue = new TypedValue();
            getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
            int color = typedValue.data;

            if (getSupportActionBar() != null) {
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(color));
            }

            LinearLayout layoutTitle = (LinearLayout) findViewById(R.id.layoutTitleBar);
            if (layoutTitle != null) {
                layoutTitle.setBackgroundColor(color);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
                color = typedValue.data;

                getWindow().setStatusBarColor(color);
            }
        }
    }

    public interface ResultListener {
        void onResult(String result, String imagePath);
    }
}


