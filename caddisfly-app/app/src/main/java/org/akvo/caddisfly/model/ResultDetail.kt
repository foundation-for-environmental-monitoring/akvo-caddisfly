package org.akvo.caddisfly.model

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class ResultDetail(var result: Double, val color: Int, var quality: Int,
                   var matchedColor: Int = 0,
                   var distance: Double = 0.0,
                   var dilution: Int = 1,
                   var calibrationSteps: Int = 0,
                   var croppedBitmap: Bitmap? = null,
                   var bitmap: Bitmap? = null,
                   var image: String? = null
) : Parcelable