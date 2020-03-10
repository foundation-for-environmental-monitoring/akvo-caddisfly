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
import android.graphics.Bitmap
import android.os.Environment
import org.akvo.caddisfly.app.CaddisflyApp.Companion.app
import org.akvo.caddisfly.helper.FileHelper
import org.akvo.caddisfly.helper.FileType
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
     * Get the root of the files storage directory, depending on the resource being app internal
     * (not concerning the user) or not (users might need to pull the resource from the storage).
     *
     * @param internal true for app specific resources, false otherwise
     * @return The root directory for this kind of resources
     */
    @JvmStatic
    fun getFilesStorageDir(context: Context, internal: Boolean): String {
        if (internal) {
            val state = Environment.getExternalStorageState()
            return if (Environment.MEDIA_MOUNTED == state) {
                val path = context.getExternalFilesDir(null)
                if (path == null) {
                    context.filesDir.absolutePath
                } else {
                    path.absolutePath
                }
            } else {
                app!!.filesDir.absolutePath
            }
        }
        @Suppress("DEPRECATION")
        return Environment.getExternalStorageDirectory().absolutePath
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

    /**
     * Method to write characters to file on SD card. Note that you must add a
     * WRITE_EXTERNAL_STORAGE permission to the manifest file or this method will throw
     * a FileNotFound Exception because you won't have write permission.
     *
     * @return absolute path name of saved file, or empty string on failure.
     */
    @JvmStatic
    fun writeBitmapToExternalStorage(bitmap: Bitmap?, fileType: FileType?, fileName: String): String { // Find the root of the external storage
// See http://developer.android.com/guide/topics/data/data-  storage.html#filesExternal
// See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder
        val dir = FileHelper.getFilesDir(fileType)
        val file = File(dir, fileName)
        // check if directory exists and if not, create it
        var success = true
        if (!dir.exists()) {
            success = dir.mkdirs()
        }
        if (success && bitmap != null) {
            try {
                val f = FileOutputStream(file)
                val bos = BufferedOutputStream(f)
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos)
                for (s in byteArrayOutputStream.toByteArray()) {
                    bos.write(s.toInt())
                }
                bos.close()
                byteArrayOutputStream.close()
                f.close()
                // Create a no media file in the folder to prevent images showing up in Gallery app
                val noMediaFile = File(dir, ".nomedia")
                if (!noMediaFile.exists()) {
                    try {
                        noMediaFile.createNewFile()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                return file.absolutePath
            } catch (e: IOException) {
                Timber.e(e)
            }
        }
        // on failure, return empty string
        return ""
    }

    //    public static void writeByteArray(Context context, byte[] data, String fileName) {
//
//        FileOutputStream outputStream;
//
//        try {
//            outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
//            BufferedOutputStream bos = new BufferedOutputStream(outputStream);
//            for (byte s : data) {
//                bos.write(s);
//            }
//            bos.close();
//            outputStream.close();
//
//        } catch (Exception e) {
//            Timber.e(e);
//        }
//    }
//
//    public static byte[] readByteArray(Context context, String fileName) throws IOException {
//
//        byte[] data;
//        int c;
//
//        FileInputStream fis = context.openFileInput(fileName);
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        BufferedInputStream bos = new BufferedInputStream(fis);
//
//        while ((c = bos.read()) != -1) {
//            byteArrayOutputStream.write(c);
//        }
//
//        data = byteArrayOutputStream.toByteArray();
//
//        bos.close();
//        byteArrayOutputStream.close();
//        fis.close();
//
//        return data;
//    }
//
//    public static void writeToInternalStorage(Context context, String fileName, String json) {
//
//        FileOutputStream outputStream = null;
//        try {
//            outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
//            for (byte s : json.getBytes(StandardCharsets.UTF_8)) {
//                outputStream.write(s);
//            }
//        } catch (Exception e) {
//            Timber.e(e);
//        } finally {
//            if (outputStream != null) {
//                try {
//                    outputStream.close();
//                } catch (IOException e) {
//                    Timber.e(e);
//                }
//            }
//        }
//    }
//
//    public static String readFromInternalStorage(Context context, String fileName) {
//
//        File file = new File(context.getFilesDir(), fileName);
//
//        try {
//            FileInputStream fis = new FileInputStream(file);
//            DataInputStream in = new DataInputStream(fis);
//            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
//            String line;
//
//            StringBuilder stringBuilder = new StringBuilder();
//            while ((line = br.readLine()) != null) {
//                stringBuilder.append(line);
//            }
//
//            br.close();
//            in.close();
//            fis.close();
//
//            return stringBuilder.toString();
//
//        } catch (IOException e) {
//            Timber.e(e);
//        }
//
//        return null;
//    }
//
//    public static void deleteFromInternalStorage(Context context, final String contains) throws IOException {
//        File file = context.getFilesDir();
//        FilenameFilter filter = (dir, filename) -> filename.contains(contains);
//        File[] files = file.listFiles(filter);
//        if (files != null) {
//            for (File f : files) {
//                //noinspection ResultOfMethodCallIgnored
//                if (!f.delete()) {
//                    throw new IOException("Error while deleting files");
//                }
//            }
//        }
//    }
//
//    public static boolean fileExists(Context context, String fileName) {
//        return new File(context.getFilesDir() + File.separator + fileName).exists();
//    }
//
//    public static int byteArrayToLeInt(byte[] b) {
//        final ByteBuffer bb = ByteBuffer.wrap(b);
//        bb.order(ByteOrder.LITTLE_ENDIAN);
//        return bb.getInt();
//    }
//
//    public static byte[] leIntToByteArray(int i) {
//        final ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
//        bb.order(ByteOrder.LITTLE_ENDIAN);
//        bb.putInt(i);
//        return bb.array();
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
    } //    private static void deleteRecursive(File file) {
//        if (file.isDirectory()) {
//            File[] files = file.listFiles();
//            if (files != null) {
//                for (File child : files) {
//                    deleteRecursive(child);
//                }
//            }
//        }
//
//        //noinspection ResultOfMethodCallIgnored
//        file.delete();
//    }
}