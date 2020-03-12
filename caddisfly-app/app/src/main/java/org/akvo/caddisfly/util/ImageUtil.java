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

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.Display;
import android.view.Surface;

import androidx.annotation.NonNull;

import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.helper.FileType;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import timber.log.Timber;

import static org.akvo.caddisfly.common.Constants.DEGREES_180;
import static org.akvo.caddisfly.common.Constants.DEGREES_270;
import static org.akvo.caddisfly.common.Constants.DEGREES_90;
import static org.akvo.caddisfly.preference.AppPreferences.getCameraCenterOffset;

/**
 * Set of utility functions to manipulate images.
 */
public final class ImageUtil {

    //Custom color matrix to convert to GrayScale
    private static final float[] MATRIX = new float[]{
            0.3f, 0.59f, 0.11f, 0, 0,
            0.3f, 0.59f, 0.11f, 0, 0,
            0.3f, 0.59f, 0.11f, 0, 0,
            0, 0, 0, 1, 0};

    private ImageUtil() {
    }

    /**
     * Decode bitmap from byte array.
     *
     * @param bytes the byte array
     * @return the bitmap
     */
    public static Bitmap getBitmap(@NonNull byte[] bytes) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }

    /**
     * Crop a bitmap to a square shape with  given length.
     *
     * @param bitmap the bitmap to crop
     * @param length the length of the sides
     * @return the cropped bitmap
     */
    @SuppressWarnings("SameParameterValue")
    public static Bitmap getCroppedBitmap(@NonNull Bitmap bitmap, int length) {

        int[] pixels = new int[length * length];

        int centerX = bitmap.getWidth() / 2;
        int centerY = (bitmap.getHeight() / 2) - getCameraCenterOffset();
        Point point;

        point = new Point(centerX, centerY);
        bitmap.getPixels(pixels, 0, length,
                point.x - (length / 2),
                point.y - (length / 2),
                length,
                length);

        Bitmap croppedBitmap = Bitmap.createBitmap(pixels, 0, length,
                length,
                length,
                Bitmap.Config.ARGB_8888);
        croppedBitmap = ImageUtil.getRoundedShape(croppedBitmap, length);
        croppedBitmap.setHasAlpha(true);

        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawBitmap(bitmap, new Matrix(), null);
        canvas.drawCircle(point.x, point.y, length / 2f, paint);

        paint.setColor(Color.YELLOW);
        paint.setStrokeWidth(1);
        canvas.drawLine(0, bitmap.getHeight() / 2f,
                bitmap.getWidth() / 3f, bitmap.getHeight() / 2f, paint);
        canvas.drawLine(bitmap.getWidth() - (bitmap.getWidth() / 3f), bitmap.getHeight() / 2f,
                bitmap.getWidth(), bitmap.getHeight() / 2f, paint);

        return croppedBitmap;
    }

    public static Bitmap getGrayscale(@NonNull Bitmap src) {

        Bitmap dest = Bitmap.createBitmap(
                src.getWidth(),
                src.getHeight(),
                src.getConfig());

        Canvas canvas = new Canvas(dest);
        Paint paint = new Paint();
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(MATRIX);
        paint.setColorFilter(filter);
        canvas.drawBitmap(src, 0, 0, paint);

        return dest;
    }

    /**
     * Crop bitmap image into a round shape.
     *
     * @param bitmap   the bitmap
     * @param diameter the diameter of the resulting image
     * @return the rounded bitmap
     */
    private static Bitmap getRoundedShape(@NonNull Bitmap bitmap, int diameter) {

        Bitmap resultBitmap = Bitmap.createBitmap(diameter,
                diameter, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultBitmap);
        Path path = new Path();
        path.addCircle(((float) diameter - 1) / 2,
                ((float) diameter - 1) / 2,
                (((float) diameter) / 2),
                Path.Direction.CCW
        );

        canvas.clipPath(path);
        resultBitmap.setHasAlpha(true);
        canvas.drawBitmap(bitmap,
                new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()),
                new Rect(0, 0, diameter, diameter), null
        );
        return resultBitmap;
    }

    /**
     * load the  bytes from a file.
     *
     * @param name     the file name
     * @param fileType the file type
     * @return the loaded bytes
     */
    public static byte[] loadImageBytes(String name, FileType fileType) {
        File path = FileHelper.getFilesDir(fileType, "");
        File file = new File(path, name + ".yuv");
        if (file.exists()) {
            byte[] bytes = new byte[(int) file.length()];
            BufferedInputStream bis;
            try {
                bis = new BufferedInputStream(new FileInputStream(file));
                DataInputStream dis = new DataInputStream(bis);
                dis.readFully(bytes);
            } catch (IOException e) {
                Timber.e(e);
            }
            return bytes;
        }

        return new byte[0];
    }

    public static Bitmap rotateImage(Activity activity, @NonNull Bitmap in) {

        Display display = activity.getWindowManager().getDefaultDisplay();
        int rotation;
        switch (display.getRotation()) {
            case Surface.ROTATION_0:
                rotation = DEGREES_90;
                break;
            case Surface.ROTATION_180:
                rotation = DEGREES_270;
                break;
            case Surface.ROTATION_270:
                rotation = DEGREES_180;
                break;
            case Surface.ROTATION_90:
            default:
                rotation = 0;
                break;
        }

        Matrix mat = new Matrix();
        mat.postRotate(rotation);
        return Bitmap.createBitmap(in, 0, 0, in.getWidth(), in.getHeight(), mat, true);
    }

    /**
     * Save an image in yuv format
     *
     * @param data     the image data
     * @param fileType the folder to save in
     * @param fileName the name of the file
     */
    public static void saveYuvImage(@NonNull byte[] data, FileType fileType, String fileName) {

        File path = FileHelper.getFilesDir(fileType);

        File file = new File(path, fileName + ".yuv");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file.getPath());
            fos.write(data);
        } catch (Exception ignored) {
            // do nothing
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Timber.e(e);
                }
            }
        }
    }
}
