package org.akvo.caddisfly.util

import org.akvo.caddisfly.app.CaddisflyApp
import java.util.*


fun String.toLocalString(): String {
    val value = this.toLowerCase(Locale.ROOT)
        .replace(")", "")
        .replace("(", "")
        .replace("- ", "")
        .replace(" ", "_")
    val resourceId = CaddisflyApp.app!!.resources
        .getIdentifier(
            value, "string",
            CaddisflyApp.app!!.packageName
        )
    return if (resourceId > 0) {
        CaddisflyApp.app!!.getString(resourceId)
    } else {
        this
    }
}
