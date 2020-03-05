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
package org.akvo.caddisfly.util

import android.content.res.AssetManager
import org.akvo.caddisfly.app.CaddisflyApp.Companion.app
import org.akvo.caddisfly.common.Constants
import org.akvo.caddisfly.helper.FileHelper
import org.akvo.caddisfly.util.FileUtil.loadTextFromFile
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets

class AssetsManager {
    lateinit var customJson: String
    lateinit var json: String
    private val manager: AssetManager? = app!!.applicationContext.assets
    private fun loadJsonFromAsset(@Suppress("SameParameterValue") fileName: String): String {
        val json: String
        var `is`: InputStream? = null
        try {
            if (manager == null) {
                return ""
            }
            `is` = manager.open(fileName)
            val size = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            json = String(buffer, StandardCharsets.UTF_8)
        } catch (ex: IOException) {
            Timber.e(ex)
            return ""
        } finally {
            if (`is` != null) {
                try {
                    `is`.close()
                } catch (e: IOException) {
                    Timber.e(e)
                }
            }
        }
        return json
    }

    val experimentalJson: String
        get() {
            val experimentalConfig = File(FileHelper.getFilesDir(FileHelper.FileType.EXP_CONFIG),
                    Constants.TESTS_META_FILENAME)
            return loadTextFromFile(experimentalConfig)
        }

    companion object {
        private var assetsManager: AssetsManager? = null
        val instance: AssetsManager?
            get() {
                if (assetsManager == null) {
                    assetsManager = AssetsManager()
                }
                return assetsManager
            }
    }

    init {
        json = loadJsonFromAsset(Constants.TESTS_META_FILENAME)
        val customConfig = File(FileHelper.getFilesDir(FileHelper.FileType.CUSTOM_CONFIG),
                Constants.TESTS_META_FILENAME)
        customJson = loadTextFromFile(customConfig)
    }
}