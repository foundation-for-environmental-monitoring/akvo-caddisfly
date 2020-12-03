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

class Reagent : Parcelable {
    @SerializedName("name")
    @Expose
    var name: String? = ""

    @SerializedName("code")
    @Expose
    var code: String? = ""

    @SerializedName("reactionTime")
    @Expose
    var reactionTime: Int? = null

    private constructor(`in`: Parcel) {
        name = `in`.readValue(String::class.java.classLoader) as String?
        code = `in`.readValue(String::class.java.classLoader) as String?
        reactionTime = `in`.readValue(Int::class.java.classLoader) as Int?
    }

    constructor()

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(name)
        dest.writeValue(code)
        dest.writeValue(reactionTime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Reagent?> = object : Parcelable.Creator<Reagent?> {
            override fun createFromParcel(`in`: Parcel): Reagent {
                return Reagent(`in`)
            }

            override fun newArray(size: Int): Array<Reagent?> {
                return arrayOfNulls(size)
            }
        }
    }
}