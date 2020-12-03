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

class Swatch : Cloneable, Parcelable {
    val value: Double
    private val defaultColor: Int
    var color: Int

    constructor(value: Double, color: Int, defaultColor: Int) {
        this.value = value
        this.color = color
        this.defaultColor = defaultColor
    }

    private constructor(`in`: Parcel) {
        value = `in`.readDouble()
        defaultColor = `in`.readInt()
        color = `in`.readInt()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeDouble(value)
        dest.writeInt(defaultColor)
        dest.writeInt(color)
    }

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): Any {
        return super.clone()
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Swatch> = object : Parcelable.Creator<Swatch> {
            override fun createFromParcel(`in`: Parcel): Swatch {
                return Swatch(`in`)
            }

            override fun newArray(size: Int): Array<Swatch?> {
                return arrayOfNulls(size)
            }
        }
    }
}