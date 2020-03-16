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
package org.akvo.caddisfly.entity

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["uid"])
class CalibrationDetail : Parcelable {
    @JvmField
    var uid = ""

    @JvmField
    @ColumnInfo(name = "date")
    var date: Long = 0

    @JvmField
    @ColumnInfo(name = "expiry")
    var expiry: Long = 0

    @JvmField
    @ColumnInfo(name = "batchNumber")
    var batchNumber: String? = null

    @JvmField
    @ColumnInfo(name = "cuvetteType")
    var cuvetteType: String? = null

    @JvmField
    @ColumnInfo(name = "fileName")
    var fileName: String? = null

    constructor()
    private constructor(`in`: Parcel) {
        val uuid = `in`.readString()
        uid = uuid ?: ""
        date = `in`.readLong()
        expiry = `in`.readLong()
        batchNumber = `in`.readString()
        cuvetteType = `in`.readString()
        fileName = `in`.readString()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(uid)
        dest.writeLong(date)
        dest.writeLong(expiry)
        dest.writeString(batchNumber)
        dest.writeString(cuvetteType)
        dest.writeString(fileName)
    }

    companion object {
        @JvmField
        @Suppress("unused")
        val CREATOR: Parcelable.Creator<CalibrationDetail?> = object : Parcelable.Creator<CalibrationDetail?> {
            override fun createFromParcel(`in`: Parcel): CalibrationDetail? {
                return CalibrationDetail(`in`)
            }

            override fun newArray(size: Int): Array<CalibrationDetail?> {
                return arrayOfNulls(size)
            }
        }
    }
}