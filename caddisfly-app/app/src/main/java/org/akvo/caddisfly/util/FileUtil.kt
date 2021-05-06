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

import android.content.Context
import android.os.Environment
import org.akvo.caddisfly.app.CaddisflyApp.Companion.app
import timber.log.Timber
import java.io.*
import java.nio.charset.StandardCharsets
import java.util.*


/**
 * Utility functions to file and folder manipulation.
 */
object FileUtil {
    /**
     * Delete a file.
     *
     * @param path     the path to the file
     * @param fileName the name of the file to delete
     */
    fun deleteFile(path: File?, fileName: String): Boolean {
        val file = File(path, fileName)
        return file.delete()
    }

    /**
     * Get the root of the files directory, depending on the resource being app internal
     * (not concerning the user) or not.
     *
     * @return The root directory for this kind of resources
     */
    @JvmStatic
    fun getFilesStorageDir(context: Context): String {
        val state = Environment.getExternalStorageState()
        return if (Environment.MEDIA_MOUNTED == state) {
            val path = context.getExternalFilesDir(null)
            if (path == null) {
                context.filesDir.absolutePath + File.separator
            } else {
                path.absolutePath + File.separator
            }
        } else {
            app!!.filesDir.absolutePath + File.separator
        }
    }

    @JvmStatic
    fun saveToFile(folder: File?, name: String, data: String?) {
        val file = File(folder, name)
        var pw: PrintWriter? = null
        try {
            val w: Writer = OutputStreamWriter(FileOutputStream(file), StandardCharsets.UTF_8)
            pw = PrintWriter(w)
            pw.write(data!!)
        } catch (e: IOException) {
            Timber.e(e)
        } finally {
            pw?.close()
        }
    }

    /**
     * Read the text from a file.
     *
     * @param file the file to read text from
     * @return the loaded text
     */
    @JvmStatic
    fun loadTextFromFile(file: File): String {
        if (file.exists()) {
            var isr: InputStreamReader? = null
            var fis: FileInputStream? = null
            try {
                fis = FileInputStream(file)
                isr = InputStreamReader(fis, StandardCharsets.UTF_8)
                val stringBuilder = StringBuilder()
                var i: Int
                while (isr.read().also { i = it } != -1) {
                    stringBuilder.append(i.toChar())
                }
                return stringBuilder.toString()
            } catch (ignored: IOException) { // do nothing
            } finally {
                if (isr != null) {
                    try {
                        isr.close()
                    } catch (e: IOException) {
                        Timber.e(e)
                    }
                }
                if (fis != null) {
                    try {
                        fis.close()
                    } catch (e: IOException) {
                        Timber.e(e)
                    }
                }
            }
        }
        return ""
    }

    /**
     * Load lines of strings from a file.
     *
     * @param path     the path to the file
     * @param fileName the file name
     * @return an list of string lines
     */
    fun loadFromFile(path: File, fileName: String): ArrayList<String>? {
        val arrayList = ArrayList<String>()
        if (path.exists()) {
            val file = File(path, fileName)
            var bufferedReader: BufferedReader? = null
            var isr: InputStreamReader? = null
            var fis: FileInputStream? = null
            try {
                fis = FileInputStream(file)
                isr = InputStreamReader(fis, StandardCharsets.UTF_8)
                bufferedReader = BufferedReader(isr)

                do {
                    val line = bufferedReader.readLine()?.also {
                        it.let { arrayList.add(it) }
                    }
                } while (line != null)

                return arrayList
            } catch (ignored: IOException) { // do nothing
            } finally {
                if (isr != null) {
                    try {
                        isr.close()
                    } catch (e: IOException) {
                        Timber.e(e)
                    }
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close()
                    } catch (e: IOException) {
                        Timber.e(e)
                    }
                }
                if (fis != null) {
                    try {
                        fis.close()
                    } catch (e: IOException) {
                        Timber.e(e)
                    }
                }
            }
        }
        return null
    }

//    /**
//     * Method to write characters to file on SD card.
//     *
//     * @return absolute path name of saved file, or empty string on failure.
//     */
//    @JvmStatic
//    fun writeBitmapToExternalStorage(
//        bitmap: Bitmap?,
//        fileType: FileType?,
//        fileName: String
//    ): String { // Find the root of the directory
//// See http://developer.android.com/guide/topics/data/data-storage.html#filesExternal
//// See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder
//        val dir = FileHelper.getFilesDir(fileType)
//        val file = File(dir, fileName)
//        // check if directory exists and if not, create it
//        var success = true
//        if (!dir.exists()) {
//            success = dir.mkdirs()
//        }
//        if (success && bitmap != null) {
//            try {
//                val f = FileOutputStream(file)
//                val bos = BufferedOutputStream(f)
//                val byteArrayOutputStream = ByteArrayOutputStream()
//                bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos)
//                for (s in byteArrayOutputStream.toByteArray()) {
//                    bos.write(s.toInt())
//                }
//                bos.close()
//                byteArrayOutputStream.close()
//                f.close()
//                // Create a no media file in the folder to prevent images showing up in Gallery app
//                val noMediaFile = File(dir, ".nomedia")
//                if (!noMediaFile.exists()) {
//                    try {
//                        noMediaFile.createNewFile()
//                    } catch (e: IOException) {
//                        e.printStackTrace()
//                    }
//                }
//                return file.absolutePath
//            } catch (e: IOException) {
//                Timber.e(e)
//            }
//        }
//        // on failure, return empty string
//        return ""
//    }

    // https://www.mkyong.com/java/how-to-copy-directory-in-java/
    @JvmStatic
    @Throws(IOException::class)
    fun copyFolder(source: File, destination: File) {
        if (source.isDirectory) {
            if (!destination.exists()) {
                destination.mkdir()
            }
            val files = source.list()
            files?.forEach { file ->
                val srcFile = File(source, file)
                val destFile = File(destination, file)
                copyFolder(srcFile, destFile)
            }
        } else {
            val `in`: InputStream = FileInputStream(source)
            val out: OutputStream = FileOutputStream(destination)
            val buf = ByteArray(1024)
            var length: Int
            while (`in`.read(buf).also { length = it } > 0) {
                out.write(buf, 0, length)
            }
            `in`.close()
            out.close()
        }
    }

    fun deleteRecursive(folder: File) {
        if (folder.isDirectory) {
            val files = folder.listFiles()
            if (files != null) {
                for (child in files) {
                    deleteRecursive(child)
                }
            }
        }
        folder.delete()
    }
}