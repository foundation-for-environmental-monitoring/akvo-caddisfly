package org.akvo.caddisfly.common

import org.akvo.caddisfly.BuildConfig

/**
 * Global Configuration settings for the app.
 */
object AppConfig {
    /**
     * The url to check for version updates.
     */
    @JvmField
    val UPDATE_CHECK_URL = "http://ffem.io/app/" +
            BuildConfig.APPLICATION_ID.replace(".", "-") + "-version"
}