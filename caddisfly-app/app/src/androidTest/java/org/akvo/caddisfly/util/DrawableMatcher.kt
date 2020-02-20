package org.akvo.caddisfly.util

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import org.hamcrest.Description
import org.hamcrest.Matcher

//https://medium.com/@felipegi91_89910/thanks-daniele-bottillo-b57caf823e34
internal class DrawableMatcher private constructor(private val expectedId: Int) : org.hamcrest.TypeSafeMatcher<View>(View::class.java) {
    private var resourceName: String? = null
    override fun matchesSafely(target: View): Boolean {
        var backgroundBitmap: Bitmap? = null
        var resourceBitmap: Bitmap? = null
        val clazz: Class<*> = target.javaClass
        if (clazz == AppCompatImageView::class.java) {
            val image = target as AppCompatImageView
            if (expectedId == ANY) {
                return image.drawable != null
            }
            if (expectedId < 0) {
                return image.background == null
            }
            resourceBitmap = drawableToBitmap(image.drawable)
            backgroundBitmap = drawableToBitmap(image.background)
        }
        if (clazz == ImageView::class.java) {
            val image = target as ImageView
            if (expectedId == ANY) {
                return image.drawable != null
            }
            if (expectedId < 0) {
                return image.background == null
            }
            resourceBitmap = drawableToBitmap(image.drawable)
            backgroundBitmap = drawableToBitmap(image.background)
        }
        val resources: Resources = target.context.resources
        val expectedDrawable: Drawable? = resources.getDrawable(expectedId)
        resourceName = resources.getResourceEntryName(expectedId)
        if (expectedDrawable == null) {
            return false
        }
        val otherBitmap = drawableToBitmap(expectedDrawable)
        return resourceBitmap != null && resourceBitmap.sameAs(otherBitmap) ||
                backgroundBitmap != null && backgroundBitmap.sameAs(otherBitmap)
    }

//    private Bitmap getBitmap(Drawable drawable) {
//        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap);
//        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
//        drawable.draw(canvas);
//        return bitmap;
//    }


    override fun describeTo(description: Description) {
        description.appendText("with drawable from resource id: ")
        description.appendValue(expectedId)
        if (resourceName != null) {
            description.appendText("[")
            description.appendText(resourceName)
            description.appendText("]")
        }
    }

    companion object {
        //    private static final int EMPTY = -1;
        private const val ANY = -2

        fun hasDrawable(): Matcher<View?> {
            return DrawableMatcher(ANY)
        }

        private fun drawableToBitmap(drawable: Drawable): Bitmap {
            val bitmap: Bitmap
            if (drawable is BitmapDrawable) {
                val bitmapDrawable = drawable
                if (bitmapDrawable.bitmap != null) {
                    return bitmapDrawable.bitmap
                }
            }
            bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
                Bitmap.createBitmap(1, 1, Config.ARGB_8888)
            } else {
                Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Config.ARGB_8888)
            }
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }
    }

}