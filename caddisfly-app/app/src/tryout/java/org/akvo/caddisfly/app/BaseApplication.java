package org.akvo.caddisfly.app;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import org.akvo.caddisfly.BuildConfig;

import androidx.multidex.MultiDex;

@SuppressLint("Registered")
public class BaseApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        //noinspection ConstantConditions
        if (BuildConfig.BUILD_TYPE.equals("debug")) {
            MultiDex.install(this);
        }
    }
}
