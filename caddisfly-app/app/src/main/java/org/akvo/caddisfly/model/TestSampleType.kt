package org.akvo.caddisfly.model

import com.google.gson.annotations.SerializedName

enum class TestSampleType {
    @SerializedName("all")
    ALL,

    @SerializedName("soil")
    SOIL,

    @SerializedName("water")
    WATER
}