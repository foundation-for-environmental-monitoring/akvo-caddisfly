package org.akvo.caddisfly.entity

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore

@Entity(primaryKeys = ["uid", "value"])
class Calibration : Parcelable {
    @JvmField
    var uid = ""

    @JvmField
    @ColumnInfo(name = "date")
    var date: Long = 0

    @JvmField
    @ColumnInfo(name = "value")
    var value = 0.0

    @JvmField
    @ColumnInfo(name = "color")
    var color = 0

    @JvmField
    @ColumnInfo(name = "quality")
    var quality = 0

    @JvmField
    @ColumnInfo(name = "zoom")
    var zoom = 0

    @JvmField
    @ColumnInfo(name = "resWidth")
    var resWidth = 0

    @JvmField
    @ColumnInfo(name = "resHeight")
    var resHeight = 0

    @JvmField
    @ColumnInfo(name = "centerOffset")
    var centerOffset = 0

    @JvmField
    @ColumnInfo(name = "image")
    var image: String? = null

    @JvmField
    @ColumnInfo(name = "croppedImage")
    var croppedImage: String? = null

    constructor()

    @Ignore
    constructor(value: Double, color: Int) {
        this.value = value
        this.color = color
    }

    private constructor(`in`: Parcel) {
        uid = `in`.readString()!!
        date = `in`.readLong()
        value = `in`.readDouble()
        color = `in`.readInt()
        quality = `in`.readInt()
        zoom = `in`.readInt()
        resWidth = `in`.readInt()
        resHeight = `in`.readInt()
        centerOffset = `in`.readInt()
        image = `in`.readString()
        croppedImage = `in`.readString()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(uid)
        dest.writeLong(date)
        dest.writeDouble(value)
        dest.writeInt(color)
        dest.writeInt(quality)
        dest.writeInt(zoom)
        dest.writeInt(resWidth)
        dest.writeInt(resHeight)
        dest.writeInt(centerOffset)
        dest.writeString(image)
        dest.writeString(croppedImage)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Calibration?> = object : Parcelable.Creator<Calibration?> {
            override fun createFromParcel(`in`: Parcel): Calibration {
                return Calibration(`in`)
            }

            override fun newArray(size: Int): Array<Calibration?> {
                return arrayOfNulls(size)
            }
        }
    }
}