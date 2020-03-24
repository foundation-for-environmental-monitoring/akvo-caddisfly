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

import android.app.Activity
import android.graphics.*
import android.view.Surface
import org.akvo.caddisfly.common.Constants.DEGREES_180
import org.akvo.caddisfly.common.Constants.DEGREES_270
import org.akvo.caddisfly.common.Constants.DEGREES_90
import org.akvo.caddisfly.helper.FileHelper.getFilesDir
import org.akvo.caddisfly.helper.FileType
import org.akvo.caddisfly.preference.AppPreferences.cameraCenterOffset
import timber.log.Timber
import java.io.*

/**
 * Set of utility functions to manipulate images.
 */
object ImageUtil {
    //Custom color matrix to convert to GrayScale
    private val MATRIX = floatArrayOf(
            0.3f, 0.59f, 0.11f, 0f, 0f,
            0.3f, 0.59f, 0.11f, 0f, 0f,
            0.3f, 0.59f, 0.11f, 0f, 0f, 0f, 0f, 0f, 1f, 0f)

    /**
     * Decode bitmap from byte array.
     *
     * @param bytes the byte array
     * @return the bitmap
     */
    fun getBitmap(bytes: ByteArray): Bitmap {
        val options = BitmapFactory.Options()
        options.inMutable = true
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
    }

    /**
     * Crop a bitmap to a square shape with  given length.
     *
     * @param bitmap the bitmap to crop
     * @param length the length of the sides
     * @return the cropped bitmap
     */
    fun getCroppedBitmap(bitmap: Bitmap, length: Int): Bitmap {
        val pixels = IntArray(length * length)
        val centerX = bitmap.width / 2
        val centerY = bitmap.height / 2 - cameraCenterOffset
        val point: Point
        point = Point(centerX, centerY)
        bitmap.getPixels(pixels, 0, length,
                point.x - length / 2,
                point.y - length / 2,
                length,
                length)
        var croppedBitmap = Bitmap.createBitmap(pixels, 0, length,
                length,
                length,
                Bitmap.Config.ARGB_8888)
        croppedBitmap = getRoundedShape(croppedBitmap, length)
        croppedBitmap.setHasAlpha(true)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.isAntiAlias = true
        paint.color = Color.GREEN
        paint.strokeWidth = 1f
        paint.style = Paint.Style.STROKE
        canvas.drawBitmap(bitmap, Matrix(), null)
        canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), length / 2f, paint)
        paint.color = Color.YELLOW
        paint.strokeWidth = 1f
        canvas.drawLine(0f, bitmap.height / 2f,
                bitmap.width / 3f, bitmap.height / 2f, paint)
        canvas.drawLine(bitmap.width - bitmap.width / 3f, bitmap.height / 2f,
                bitmap.width.toFloat(), bitmap.height / 2f, paint)
        return croppedBitmap
    }

    fun getGrayscale(src: Bitmap): Bitmap {
        val dest = Bitmap.createBitmap(
                src.width,
                src.height,
                src.config)
        val canvas = Canvas(dest)
        val paint = Paint()
        val filter = ColorMatrixColorFilter(MATRIX)
        paint.colorFilter = filter
        canvas.drawBitmap(src, 0f, 0f, paint)
        return dest
    }

    /**
     * Crop bitmap image into a round shape.
     *
     * @param bitmap   the bitmap
     * @param diameter the diameter of the resulting image
     * @return the rounded bitmap
     */
    private fun getRoundedShape(bitmap: Bitmap, diameter: Int): Bitmap {
        val resultBitmap = Bitmap.createBitmap(diameter,
                diameter, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        val path = Path()
        path.addCircle((diameter.toFloat() - 1) / 2,
                (diameter.toFloat() - 1) / 2,
                diameter.toFloat() / 2,
                Path.Direction.CCW
        )
        canvas.clipPath(path)
        resultBitmap.setHasAlpha(true)
        canvas.drawBitmap(bitmap,
                Rect(0, 0, bitmap.width, bitmap.height),
                Rect(0, 0, diameter, diameter), null
        )
        return resultBitmap
    }

    /**
     * load the  bytes from a file.
     *
     * @param name     the file name
     * @param fileType the file type
     * @return the loaded bytes
     */
    @JvmStatic
    fun loadImageBytes(name: String, fileType: FileType?): ByteArray {
        val path = getFilesDir(fileType, "")
        val file = File(path, "$name.yuv")
        if (file.exists()) {
            val bytes = ByteArray(file.length().toInt())
            val bis: BufferedInputStream
            try {
                bis = BufferedInputStream(FileInputStream(file))
                val dis = DataInputStream(bis)
                dis.readFully(bytes)
            } catch (e: IOException) {
                Timber.e(e)
            }
            return bytes
        }
        return ByteArray(0)
    }

    fun rotateImage(activity: Activity, `in`: Bitmap): Bitmap {
        val display = activity.windowManager.defaultDisplay
        val rotation: Int
        rotation = when (display.rotation) {
            Surface.ROTATION_0 -> DEGREES_90
            Surface.ROTATION_180 -> DEGREES_270
            Surface.ROTATION_270 -> DEGREES_180
            Surface.ROTATION_90 -> 0
            else -> 0
        }
        val mat = Matrix()
        mat.postRotate(rotation.toFloat())
        return Bitmap.createBitmap(`in`, 0, 0, `in`.width, `in`.height, mat, true)
    }

    /**
     * Save an image in yuv format
     *
     * @param data     the image data
     * @param fileType the folder to save in
     * @param fileName the name of the file
     */
    @JvmStatic
    fun saveYuvImage(data: ByteArray, fileType: FileType?, fileName: String) {
        val path = getFilesDir(fileType)
        val file = File(path, "$fileName.yuv")
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file.path)
            fos.write(data)
        } catch (ignored: Exception) {
            // do nothing
        } finally {
            if (fos != null) {
                try {
                    fos.close()
                } catch (e: IOException) {
                    Timber.e(e)
                }
            }
        }
    }
}