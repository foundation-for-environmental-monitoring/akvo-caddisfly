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
package org.akvo.caddisfly.util

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.text.*
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import org.akvo.caddisfly.R
import org.akvo.caddisfly.model.TestInfo
import org.akvo.caddisfly.widget.CenteredImageSpan
import java.util.regex.Pattern

object StringUtil {
    @JvmStatic
    fun getStringResourceByName(context: Context, theKey: String): Spanned {
        val key = theKey.trim { it <= ' ' }
        val packageName = context.packageName
        val resId = context.resources.getIdentifier(key, "string", packageName)
        return if (resId == 0) {
            Spannable.Factory.getInstance().newSpannable(fromHtml(key))
        } else {
            Spannable.Factory.getInstance().newSpannable(context.getText(resId))
        }
    }

    @JvmStatic
    fun fromHtml(html: String?): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(html)
        }
    }

    @JvmStatic
    fun toInstruction(context: AppCompatActivity, testInfo: TestInfo?, text: String): SpannableStringBuilder {
        val builder = SpannableStringBuilder()
        val spanned = getStringResourceByName(context, text)
        builder.append(spanned)
        val m = Pattern.compile("\\(\\*(\\w+)\\*\\)").matcher(builder)
        while (m.find()) {
            val resId = context.resources.getIdentifier("button_" + m.group(1),
                    "drawable", context.packageName)
            if (resId > 0) {
                builder.setSpan(CenteredImageSpan(context, resId),
                        m.start(0), m.end(0), Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            }
        }
        if (testInfo != null) {
            // Set reagent in the string
            replaceReagentTags(testInfo, builder)
            // Set sample quantity in the string
            val m1 = Pattern.compile("%sampleQuantity").matcher(builder)
            while (m1.find()) {
                builder.replace(m1.start(), m1.end(), testInfo.sampleQuantity)
            }
            // Set reaction time in the string
            for (i in 1..4) {
                val m2 = Pattern.compile("%reactionTime$i").matcher(builder)
                while (m2.find()) {
                    builder.replace(m2.start(), m2.end(), testInfo.getReagent(i - 1).reactionTime.toString())
                }
            }
        }
        insertDialogLinks(context, builder)
        return builder
    }

    private fun insertDialogLinks(context: AppCompatActivity, builder: SpannableStringBuilder) {
        if (builder.toString().contains("[a topic=")) {
            val startIndex = builder.toString().indexOf("[a topic=")
            val topic: String?
            val p = Pattern.compile("\\[a topic=(.*?)]")
            val m3 = p.matcher(builder)
            if (m3.find()) {
                topic = m3.group(1)
                builder.replace(m3.start(), m3.end(), "")
                val endIndex = builder.toString().indexOf("[/a]")
                builder.replace(endIndex, endIndex + 4, "")
                val clickableSpan: ClickableSpan = object : ClickableSpan() {
                    override fun onClick(textView: View) {
                        if (topic.equals("sulfide", ignoreCase = true)) {
                            val newFragment: DialogFragment = SulfideDialogFragment()
                            newFragment.show(context.supportFragmentManager, "sulfideDialog")
                        } else {
                            val newFragment: DialogFragment = DilutionDialogFragment()
                            newFragment.show(context.supportFragmentManager, "dilutionDialog")
                        }
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.isUnderlineText = false
                        ds.color = ContextCompat.getColor(context, R.color.text_links)
                    }
                }
                builder.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                builder.setSpan(UnderlineSpan(), startIndex, endIndex, 0)
            }
        }
    }

    private fun replaceReagentTags(testInfo: TestInfo, builder: SpannableStringBuilder) {
        for (i in 1..4) {
            val m1 = Pattern.compile("%reagent$i").matcher(builder)
            while (m1.find()) {
                var name = testInfo.getReagent(i - 1).name
                val code = testInfo.getReagent(i - 1).code
                if (code.isNotEmpty()) {
                    name = String.format("%s (%s)", name, code)
                }
                builder.replace(m1.start(), m1.end(), name)
            }
        }
    }

//    fun convertToTags(text: String): String {
//        val result = StringBuilder()
//        for (element in text) {
//            result.append("(*").append(element).append("*)")
//        }
//        return result.toString()
//    }
//
//    fun getStringByName(context: Context, name: String?): String {
//        return if (name == null) {
//            ""
//        } else context.resources.getString(context.resources
//                .getIdentifier(name, "string", context.packageName))
//    }

    class SulfideDialogFragment : DialogFragment() {
        @SuppressLint("InflateParams")
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(activity!!)
            val inflater = activity!!.layoutInflater
            builder.setView(inflater.inflate(R.layout.dialog_sulfide_instruction, null)) // Add action buttons
                    .setPositiveButton(R.string.ok) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            return builder.create()
        }
    }

    class DilutionDialogFragment : DialogFragment() {
        @SuppressLint("InflateParams")
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(activity!!)
            val inflater = activity!!.layoutInflater
            builder.setView(inflater.inflate(R.layout.dialog_dilution_instruction, null)) // Add action buttons
                    .setPositiveButton(R.string.ok) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            return builder.create()
        }
    }
}