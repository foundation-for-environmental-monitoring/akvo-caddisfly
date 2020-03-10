package org.akvo.caddisfly.common

object ConstantKey {
    private const val NAMESPACE_PREFIX = "org.akvo.caddisfly."
    const val TOP_LEFT = NAMESPACE_PREFIX + "top_left"
    const val TOP_RIGHT = NAMESPACE_PREFIX + "top_right"
    const val BOTTOM_LEFT = NAMESPACE_PREFIX + "bottom_left"
    const val BOTTOM_RIGHT = NAMESPACE_PREFIX + "bottom_right"
    const val TEST_INFO = NAMESPACE_PREFIX + "testInfo"
    const val RUN_TEST = NAMESPACE_PREFIX + "runTest"
    const val IS_INTERNAL = NAMESPACE_PREFIX + "internal"
    const val START_MEASURE = "start_measure"
    const val TEST_STAGE = NAMESPACE_PREFIX + "testStage"
    const val TYPE = NAMESPACE_PREFIX + "type"
    const val SAMPLE_TYPE = NAMESPACE_PREFIX + "sampleType"
    const val NEXT_UPDATE_CHECK = NAMESPACE_PREFIX + "lastUpdateCheck"
    const val VALUE = NAMESPACE_PREFIX + "value"
}