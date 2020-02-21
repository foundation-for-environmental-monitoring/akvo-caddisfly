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
     * The url for the experimental tests json config.
     */
    const val EXPERIMENT_TESTS_URL = "https://raw.githubusercontent.com/foundation-for-environmental-monitoring/experimental-tests/ffem-experiment/experimental_tests.json"
    /**
     * The url to check for version updates.
     */
    @JvmField
    val UPDATE_CHECK_URL = "http://ffem.io/app/" +
            BuildConfig.APPLICATION_ID.replace(".", "-") + "-version"
}