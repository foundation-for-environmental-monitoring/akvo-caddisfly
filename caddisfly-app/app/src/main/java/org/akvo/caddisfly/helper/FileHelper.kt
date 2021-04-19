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
package org.akvo.caddisfly.helper

import android.widget.Toast
import org.akvo.caddisfly.app.CaddisflyApp.Companion.app
import org.akvo.caddisfly.common.AppConstants
import org.akvo.caddisfly.preference.AppPreferences.showDebugInfo
import org.akvo.caddisfly.util.FileUtil
import org.akvo.caddisfly.util.FileUtil.getFilesStorageDir
import java.io.File
import java.io.IOException

/**
 * The different types of files.
 */
enum class FileType {
    CALIBRATION, CUSTOM_CONFIG, EXP_CONFIG, CARD, TEST_IMAGE, DIAGNOSTIC_IMAGE, RESULT_IMAGE, TEMP_IMAGE
}

object FileHelper {

    /**
     * The user created configuration file name.
     */
    // Folders
    private val ROOT_DIRECTORY = File.separator + AppConstants.APP_FOLDER
    private const val DIR_CALIBRATION = "calibration" // Calibration files
    private const val DIR_CONFIG = "custom-config" // Custom config json folder
    private val DIR_EXP_CONFIG =
        "qa" + File.separator + "experiment-config" // Experimental config json folder
    private val DIR_TEST_IMAGE = "qa" + File.separator + "test-image" // Images saved for testing
    private val DIR_CARD = "qa" + File.separator + "color-card" // Color card for debugging
    private const val DIR_RESULT_IMAGES =
        "result-images" // Images to be sent with result to dashboard
    private val DIR_DIAGNOSTIC_IMAGE =
        "qa" + File.separator + "diagnostic-images" // Images saved for testing
    private val DIR_TEMP_IMAGES = "test" + File.separator + "images" // Images saved for testing

    /**
     * Get the appropriate files directory for the given FileType. The directory may or may
     * not be in the app-specific External Storage. The caller cannot assume anything about
     * the location.
     *
     * @param type FileType to determine the type of resource attempting to use.
     * @return File representing the root directory for the given FileType.
     */
    @JvmStatic
    fun getFilesDir(type: FileType?): File {
        return getFilesDir(type, "")
    }

    /**
     * Get the appropriate files directory for the given FileType. The directory may or may
     * not be in the app-specific External Storage. The caller cannot assume anything about
     * the location.
     *
     * @param type    FileType to determine the type of resource attempting to use.
     * @param subPath a sub directory to be created
     * @return File representing the root directory for the given FileType.
     */
    @JvmStatic
    fun getFilesDir(type: FileType?, subPath: String): File {
        val path: String = when (type) {
            FileType.CALIBRATION -> getFilesStorageDir(app!!, false) + DIR_CALIBRATION
            FileType.CUSTOM_CONFIG -> getFilesStorageDir(app!!, false) + DIR_CONFIG
            FileType.EXP_CONFIG -> getFilesStorageDir(app!!, false) + DIR_EXP_CONFIG
            FileType.CARD -> getFilesStorageDir(app!!, false) + DIR_CARD
            FileType.RESULT_IMAGE -> getFilesStorageDir(app!!, false) + DIR_RESULT_IMAGES
            FileType.TEST_IMAGE -> getFilesStorageDir(app!!, false) + DIR_TEST_IMAGE
            FileType.DIAGNOSTIC_IMAGE -> getFilesStorageDir(app!!, false) + DIR_DIAGNOSTIC_IMAGE
            FileType.TEMP_IMAGE -> getFilesStorageDir(app!!, false) + DIR_TEMP_IMAGES
            else -> getFilesStorageDir(app!!, false)
        }
        var dir = File(path)
        if (subPath.isNotEmpty()) {
            dir = File(dir, subPath)
        }
        try {
            migrateFolders()
        } catch (ignored: Exception) {
        }

        // create folder if it does not exist
        if (!dir.exists() && !dir.mkdirs() && showDebugInfo) {
            Toast.makeText(
                app,
                "Error creating folder: " + dir.absolutePath, Toast.LENGTH_SHORT
            ).show()
        }
        return dir
    }

    //TODO remove migration at some point in future
    fun migrateFolders() {
        try {
            val appFolder = File(getFilesStorageDir(app!!.applicationContext, false))
            val oldAppFolder = File(
                getFilesStorageDir(
                    app!!.applicationContext,
                    true
                ) + File.separator + ROOT_DIRECTORY
            )
            if (appFolder.exists()) {
                if (oldAppFolder.exists() && oldAppFolder.isDirectory) {
                    try {
                        FileUtil.copyFolder(oldAppFolder, appFolder)
                        FileUtil.deleteRecursive(oldAppFolder)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
        }
    }
}