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

package org.akvo.caddisfly.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.helper.FileHelper;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Utility functions to file and folder manipulation.
 */
public final class FileUtil {

    private FileUtil() {
    }

    /**
     * Delete a file.
     *
     * @param path     the path to the file
     * @param fileName the name of the file to delete
     */
    @SuppressWarnings("UnusedReturnValue")
    public static boolean deleteFile(File path, String fileName) {
        File file = new File(path, fileName);
        return file.delete();
    }

    /**
     * Get the root of the files directory, depending on the resource being app internal
     * (not concerning the user) or not.
     *
     * @param external false for app specific resources, false otherwise
     * @return The root directory for this kind of resources
     */
    @SuppressWarnings("SameParameterValue")
    public static String getFilesStorageDir(Context context, boolean external) {
        if (external) {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                File path = context.getExternalFilesDir(null);
                if (path == null) {
                    return context.getFilesDir().getAbsolutePath() + File.separator;
                } else {
                    return path.getAbsolutePath() + File.separator;
                }
            } else {
                return CaddisflyApp.getApp().getFilesDir().getAbsolutePath() + File.separator;
            }
        }
    }

    public static void saveToFile(File folder, String name, String data) {

        File file = new File(folder, name);

        PrintWriter pw = null;
        try {
            Writer w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            pw = new PrintWriter(w);
            pw.write(data);
        } catch (IOException e) {
            Timber.e(e);
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }

    /**
     * Read the text from a file.
     *
     * @param file the file to read text from
     * @return the loaded text
     */
    public static String loadTextFromFile(File file) {

        if (file.exists()) {

            InputStreamReader isr = null;
            FileInputStream fis = null;
            try {

                fis = new FileInputStream(file);
                isr = new InputStreamReader(fis, StandardCharsets.UTF_8);

                StringBuilder stringBuilder = new StringBuilder();

                int i;
                while ((i = isr.read()) != -1) {
                    stringBuilder.append((char) i);
                }
                return stringBuilder.toString();

            } catch (IOException ignored) {
                // do nothing
            } finally {
                if (isr != null) {
                    try {
                        isr.close();
                    } catch (IOException e) {
                        Timber.e(e);
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        Timber.e(e);
                    }
                }
            }
        }

        return "";
    }

    /**
     * Load lines of strings from a file.
     *
     * @param path     the path to the file
     * @param fileName the file name
     * @return an list of string lines
     */
    public static List<String> loadFromFile(File path, String fileName) {

        ArrayList<String> arrayList = new ArrayList<>();
        if (path.exists()) {

            File file = new File(path, fileName);

            BufferedReader bufferedReader = null;
            InputStreamReader isr = null;
            FileInputStream fis = null;
            try {

                fis = new FileInputStream(file);
                isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                bufferedReader = new BufferedReader(isr);

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    arrayList.add(line);
                }

                return arrayList;

            } catch (IOException ignored) {
                // do nothing
            } finally {
                if (isr != null) {
                    try {
                        isr.close();
                    } catch (IOException e) {
                        Timber.e(e);
                    }
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        Timber.e(e);
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        Timber.e(e);
                    }
                }
            }
        }

        return null;
    }

    /**
     * Method to write characters to file on SD card.
     */
    public static void writeBitmapToExternalStorage(Bitmap bitmap, FileHelper.FileType fileType, String fileName) {
        // Find the root of the external storage
        // See http://developer.android.com/guide/topics/data/data-storage.html#filesExternal
        // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder

        File dir = FileHelper.getFilesDir(fileType);
        File file = new File(dir, fileName);

        // check if directory exists and if not, create it
        boolean success = true;
        if (!dir.exists()) {
            success = dir.mkdirs();
        }

        if (success && bitmap != null) {
            try {
                FileOutputStream f = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(f);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);

                for (byte s : byteArrayOutputStream.toByteArray()) {
                    bos.write(s);
                }
                bos.close();
                byteArrayOutputStream.close();
                f.close();

                // Create a no media file in the folder to prevent images showing up in Gallery app
                File noMediaFile = new File(dir, ".nomedia");
                if (!noMediaFile.exists()) {
                    try {
                        //noinspection ResultOfMethodCallIgnored
                        noMediaFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                Timber.e(e);
            }
        }
        // on failure, return empty string
    }

    public static int byteArrayToLeInt(byte[] b) {
        final ByteBuffer bb = ByteBuffer.wrap(b);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt();
    }

    // https://www.mkyong.com/java/how-to-copy-directory-in-java/
    public static void copyFolder(File source, File destination) throws IOException {

        if (source.isDirectory()) {
            if (!destination.exists()) {
                //noinspection ResultOfMethodCallIgnored
                destination.mkdir();
            }

            String[] files = source.list();
            for (String file : files) {
                File srcFile = new File(source, file);
                File destFile = new File(destination, file);
                copyFolder(srcFile, destFile);
            }
        } else {
            InputStream in = new FileInputStream(source);
            OutputStream out = new FileOutputStream(destination);

            byte[] buf = new byte[1024];
            int length;
            while ((length = in.read(buf)) > 0) {
                out.write(buf, 0, length);
            }
            in.close();
            out.close();
        }
    }

    public static void deleteRecursive(File folder) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteRecursive(child);
                }
            }
        }

        //noinspection ResultOfMethodCallIgnored
        folder.delete();
    }

    public static String readText(File file) {
        String line = "";

        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            fileInputStream.close();
            line = stringBuilder.toString();

            bufferedReader.close();
        } catch (IOException ignored) {
        }
        return line;
    }
}