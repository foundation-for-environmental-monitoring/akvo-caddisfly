package org.akvo.caddisfly.app

import android.annotation.SuppressLint
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import org.akvo.caddisfly.di.DaggerAppComponent


@SuppressLint("Registered")
open class BaseApplication : DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.factory().create(this as CaddisflyApp?)
    }
}
