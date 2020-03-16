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
package org.akvo.caddisfly.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ColorItem : Parcelable {
    @SerializedName("value")
    @Expose
    var value: Double?
        private set

    //    public void setValue(Double value) {
    //        this.value = value;
    //    }
    @SerializedName("lab")
    @Expose
    var lab: List<Double> = emptyList()
        private set

    @SerializedName("rgb")
    @Expose
    var rgb: List<Int> = emptyList()
        private set

    @JvmField
    var rgbInt = 0

    private constructor(`in`: Parcel) {
        value = if (`in`.readByte().toInt() == 0x00) null else `in`.readDouble()
        if (`in`.readByte().toInt() == 0x01) {
            lab = ArrayList()
            `in`.readList(lab, Double::class.java.classLoader)
        } else {
            lab = emptyList()
        }
        if (`in`.readByte().toInt() == 0x01) {
            rgb = ArrayList()
            `in`.readList(rgb, Int::class.java.classLoader)
        } else {
            rgb = emptyList()
        }
        rgbInt = `in`.readInt()
    }

    constructor(value: Double) {
        this.value = value
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        if (value == null) {
            dest.writeByte(0x00.toByte())
        } else {
            dest.writeByte(0x01.toByte())
            dest.writeDouble(value!!)
        }
        dest.writeByte(0x01.toByte())
        dest.writeList(lab)
        dest.writeByte(0x01.toByte())
        dest.writeList(rgb)
        dest.writeInt(rgbInt)
    }

    companion object CREATOR : Parcelable.Creator<ColorItem> {
        override fun createFromParcel(parcel: Parcel): ColorItem {
            return ColorItem(parcel)
        }

        override fun newArray(size: Int): Array<ColorItem?> {
            return arrayOfNulls(size)
        }
    }
}