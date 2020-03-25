package org.akvo.caddisfly.sensor.striptest.utils

/**
 * Various constants used for configuration and keys.
 */
object Constants {
    const val MAX_LUM_LOWER = 210.0
    const val MAX_LUM_UPPER = 240.0
    const val SHADOW_PERCENTAGE_LIMIT = 90.0
    const val CONTRAST_DEVIATION_FRACTION = 0.1
    const val CONTRAST_MAX_DEVIATION_FRACTION = 0.20
    const val CROP_FINDER_PATTERN_FACTOR = 0.75
    const val MAX_TILT_DIFF = 0.05f
    const val MAX_CLOSER_DIFF = 0.15f
    const val COUNT_QUALITY_CHECK_LIMIT = 15
    const val PIXEL_PER_MM = 5
    const val SKIP_MM_EDGE = 1
    const val CALIBRATION_PERCENTAGE_LIMIT = 95
    const val MEASURE_TIME_COMPENSATION_MILLIS = 3000
    const val STRIP_WIDTH_FRACTION = 0.5f
    const val GET_READY_SECONDS = 12
    const val MIN_SHOW_TIMER_SECONDS = 5
    const val MAX_COLOR_DISTANCE = 23
}