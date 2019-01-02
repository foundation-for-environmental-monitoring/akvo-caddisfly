package org.akvo.caddisfly.app;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import androidx.multidex.MultiDex;

import org.akvo.caddisfly.BuildConfig;

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
