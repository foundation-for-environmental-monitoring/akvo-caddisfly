package org.akvo.caddisfly.common

import org.akvo.caddisfly.BuildConfig

/**
 * Global Configuration settings for the app.
 */
object AppConfig {
    const val SOUND_ON = true
    const val USE_SCREEN_PINNING = true
    /**
     * Date on which the app version will expire.
     * This is to ensure that installs from apk meant for testing only are not used for too long.
     */
    const val APP_EXPIRY = false
    const val APP_EXPIRY_DAY = 1
    const val APP_EXPIRY_MONTH = 11
    const val APP_EXPIRY_YEAR = 2019
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