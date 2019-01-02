/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly.
 *
 * Akvo Caddisfly is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Akvo Caddisfly. If not, see <http://www.gnu.org/licenses/>.
 */

package org.akvo.caddisfly.ui;

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.util.ApiUtil;
import org.akvo.caddisfly.util.PreferencesUtil;

import java.lang.reflect.Field;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

/**
 * The base activity with common functions.
 */
public abstract class BaseActivity extends AppCompatActivity {

    private String mTitle;
    protected int appTheme = R.style.AppTheme_Main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int appThemeResId = -1;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            try {
                String theme = bundle.getString("theme");
                if (theme != null) {
                    theme = String.valueOf(theme.charAt(0)).toUpperCase() +
                            theme.substring(1, Math.min(theme.length(), 10)).toLowerCase();
                    appThemeResId = getThemeResourceId(theme);

                    String packageName = Objects.requireNonNull(getCallingActivity()).getPackageName();

                    PreferencesUtil.setString(this, ConstantKey.APP_THEME, theme);
                    PreferencesUtil.setString(this, theme, packageName);
                }
            } catch (Exception ignored) {
            }
        }

        if (appThemeResId == -1) {
            String theme = PreferencesUtil.getString(this, ConstantKey.APP_THEME, "");
            if (!theme.isEmpty()) {
                String packageName = PreferencesUtil.getString(this, theme, "");
                if (!packageName.isEmpty() && ApiUtil.isAppInstalled(this, packageName)) {
                    appThemeResId = getThemeResourceId(theme);
                }
            }
        }

        if (appThemeResId != -1) {
            appTheme = appThemeResId;
        }

        updateTheme();
    }

    private int getThemeResourceId(String theme) {
        int resourceId = -1;
        try {
            Class res = R.style.class;
            Field field = res.getField("AppTheme_" + theme);
            resourceId = field.getInt(null);

        } catch (Exception ignored) {
        }

        return resourceId;
    }

    private void updateTheme() {

        setTheme(appTheme);

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
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            try {
                setSupportActionBar(toolbar);
            } catch (Exception ignored) {
                // do nothing
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
        TextView textTitle = findViewById(R.id.textToolbarTitle);
        if (textTitle != null && title != null) {
            mTitle = title.toString();
            textTitle.setText(title);
        }
    }

    @Override
    public void setTitle(int titleId) {
        TextView textTitle = findViewById(R.id.textToolbarTitle);
        if (textTitle != null && titleId != 0) {
            mTitle = getString(titleId);
            textTitle.setText(titleId);
        }
    }

    /**
     * Changes the action bar style depending on if the app is in user mode or diagnostic mode
     * This serves as a visual indication as to what mode the app is running in.
     */
    protected void changeActionBarStyleBasedOnCurrentMode() {
        if (AppPreferences.isDiagnosticMode()) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(
                        ContextCompat.getColor(this, R.color.diagnostic)));
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.diagnostic_status));
            }
            LinearLayout layoutTitle = findViewById(R.id.layoutTitleBar);
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

            LinearLayout layoutTitle = findViewById(R.id.layoutTitleBar);
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
}


