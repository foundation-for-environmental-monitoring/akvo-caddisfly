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
import java.util.*

class Instruction : Parcelable, Cloneable {
    @SerializedName("section")
    @Expose
    val section: ArrayList<String>?

    @JvmField
    @SerializedName("testStage")
    @Expose
    val testStage: Int
    var index = 0

    private constructor(instruction: Instruction) {
        index = instruction.index
        section = ArrayList(instruction.section!!)
        testStage = instruction.testStage
    }

    private constructor(`in`: Parcel) {
        section = `in`.createStringArrayList()
        testStage = `in`.readInt()
    }

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): Instruction {
        val clone = super.clone() as Instruction
        return Instruction(clone)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeStringList(section)
        parcel.writeInt(testStage)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Instruction?> = object : Parcelable.Creator<Instruction?> {
            override fun createFromParcel(`in`: Parcel): Instruction {
                return Instruction(`in`)
            }

            override fun newArray(size: Int): Array<Instruction?> {
                return arrayOfNulls(size)
            }
        }
    }
}