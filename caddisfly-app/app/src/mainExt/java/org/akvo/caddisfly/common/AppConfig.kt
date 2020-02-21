package org.akvo.caddisfly.common

import org.akvo.caddisfly.BuildConfig

/**
 * Global Configuration settings for the app.
 */
object AppConfig {
    /**
     * Uri for photos from built in camera.
     */
    const val FILE_PROVIDER_AUTHORITY_URI = BuildConfig.APPLICATION_ID + ".fileprovider"
    /**
     * The url to check for version updates.
     */
    @JvmField
    val UPDATE_CHECK_URL = "http://ffem.io/app/" +
            BuildConfig.APPLICATION_ID.replace(".", "-") + "-version"
}