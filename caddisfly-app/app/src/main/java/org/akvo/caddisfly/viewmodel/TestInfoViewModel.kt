/*
 * Copyright 2017, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.akvo.caddisfly.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import org.akvo.caddisfly.BuildConfig
import org.akvo.caddisfly.R
import org.akvo.caddisfly.common.Constants
import org.akvo.caddisfly.model.Instruction
import org.akvo.caddisfly.model.TestInfo
import org.akvo.caddisfly.model.TestType
import org.akvo.caddisfly.util.StringUtil.getStringResourceByName
import org.akvo.caddisfly.util.StringUtil.toInstruction
import org.akvo.caddisfly.widget.RowView
import java.io.IOException
import java.io.InputStream
import java.util.regex.Pattern

class TestInfoViewModel(application: Application) : AndroidViewModel(application) {
    @JvmField
    val test = ObservableField<TestInfo>()
    fun setTest(testInfo: TestInfo?) {
        test.set(testInfo)
        Companion.testInfo = testInfo
    }

    companion object {
        private var testInfo: TestInfo? = null

        /**
         * Sets the content of the view with formatted string.
         *
         * @param linearLayout the layout
         * @param instruction  the instruction key
         */
        @JvmStatic
        @BindingAdapter("content")
        fun setContent(linearLayout: LinearLayout, instruction: Instruction?) {
            if (instruction?.section == null) {
                return
            }
            val context = linearLayout.context
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val size = Point()
            windowManager.defaultDisplay?.getRealSize(size)
            val displayMetrics = context.resources.displayMetrics
            for (i in instruction.section.indices) {
                var text = instruction.section[i]
                val tag = Pattern.compile("~.*?~").matcher(text)
                when {
                    tag.find() -> {
                        return
                    }
                    text.contains("image:") -> {
                        insertImage(linearLayout, context, size, displayMetrics, i, text)
                    }
                    else -> {
                        val rowView = RowView(context)
                        val m = Pattern.compile("^(\\d*?[a-zA-Z]{1,3}\\.\\s*)(.*)").matcher(text)
                        val m1 = Pattern.compile("^(\\d+?\\.\\s*)(.*)").matcher(text)
                        val m2 = Pattern.compile("^(\\.\\s*)(.*)").matcher(text)


                        when {
                            m.find() -> {
                                rowView.setNumber(m.group(1)?.trim { it <= ' ' })
                                text = m.group(2)?.trim { it <= ' ' }.toString()
                            }
                            m1.find() -> {
                                rowView.setNumber(m1.group(1)?.trim { it <= ' ' })
                                text = m1.group(2)?.trim { it <= ' ' }.toString()
                            }
                            m2.find() -> {
                                rowView.setNumber("   ")
                                text = m2.group(2)?.trim { it <= ' ' }.toString()
                            }
                        }

                        val sentences = "$text. ".split("\\.\\s+".toPattern()).toTypedArray()
                        val labelView = LinearLayout(context)
                        for (j in sentences.indices) {

                            if (sentences[j].isEmpty()) {
                                continue
                            }

                            if (j > 0) {
                                rowView.append(SpannableString(" "))
                            }

                            rowView.append(
                                toInstruction((context as AppCompatActivity),
                                    testInfo, sentences[j].trim { it <= ' ' })
                            )
                            val sentence = getStringResourceByName(context, sentences[j]).toString()
                            if (sentence.contains("[/a]")) {
                                rowView.enableLinks()
                            }
                        }

                        // set an id for the view to be able to find it for unit testing
                        rowView.id = i
                        linearLayout.addView(rowView)
                        linearLayout.addView(labelView)
                    }
                }
            }
        }

        @JvmStatic
        @BindingAdapter("testSubtitle")
        fun setSubtitle(view: TextView, testInfo: TestInfo) {
            if (testInfo.subtype == TestType.TITRATION) {
                view.text = view.context.getString(R.string.titration)
            } else {
                var subTitle = testInfo.minMaxRange
                if (testInfo.minMaxRange.isNotEmpty()) {
                    val matcher =
                        Pattern.compile("<dilutionRange>(.*?)</dilutionRange>").matcher(subTitle)
                    if (matcher.find()) {
                        subTitle = matcher.replaceAll(
                            String.format(
                                view.resources
                                    .getString(R.string.up_to_with_dilution), matcher.group(1)
                            )
                        )
                    }
                }
                view.text = subTitle
            }
        }

        private fun insertImage(
            linearLayout: LinearLayout, context: Context, size: Point,
            displayMetrics: DisplayMetrics, i: Int, text: String
        ) {
            val imageName = text.substring(text.indexOf(":") + 1)
            val resourceId = context.resources.getIdentifier(
                "drawable/in_$imageName",
                "id", BuildConfig.APPLICATION_ID
            )
            if (resourceId > 0) {
                var divisor = 3.0
                if (displayMetrics.densityDpi > 250) {
                    divisor = 2.4
                }
                if (size.y > displayMetrics.heightPixels) {
                    divisor += 0.3
                }
                val llp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (displayMetrics.heightPixels / divisor).toInt()
                )
                llp.setMargins(0, 0, 0, 20)
                val imageView = AppCompatImageView(context)
                imageView.setImageResource(resourceId)
                imageView.layoutParams = llp
                imageView.contentDescription = imageName

                // set an id for the view to be able to find it for unit testing
                imageView.id = i
                linearLayout.addView(imageView)
            } else {
                val image = Constants.ILLUSTRATION_PATH + imageName + ".webp"
                var ims: InputStream? = null
                try {
                    ims = context.assets.open(image)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                if (ims != null) {
                    val imageView = ImageView(linearLayout.context)
                    imageView.scaleType = ImageView.ScaleType.FIT_CENTER
                    imageView.setImageDrawable(Drawable.createFromStream(ims, null))
                    var divisor = 3.1
                    if (displayMetrics.densityDpi > 250) {
                        divisor = 2.7
                    }
                    if (size.y > displayMetrics.heightPixels) {
                        divisor += 0.3
                    }
                    val llp = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        (displayMetrics.heightPixels / divisor).toInt()
                    )
                    llp.setMargins(0, 0, 0, 20)
                    imageView.layoutParams = llp
                    imageView.contentDescription = imageName

                    // set an id for the view to be able to find it for unit testing
                    imageView.id = i
                    linearLayout.addView(imageView)
                }
            }
        }

        /**
         * Sets the image scale.
         *
         * @param imageView the image view
         * @param scaleType the scale type
         */
        @JvmStatic
        @BindingAdapter("imageScale")
        fun setImageScale(imageView: ImageView, scaleType: String?) {
            if (scaleType != null) {
                imageView.scaleType =
                    if ("fitCenter" == scaleType) ImageView.ScaleType.FIT_CENTER else ImageView.ScaleType.CENTER_CROP
            } else {
                imageView.scaleType = ImageView.ScaleType.FIT_CENTER
            }
        }

        @JvmStatic
        @BindingAdapter("imageUrl")
        fun setImageUrl(imageView: ImageView, name: String) {
            setImage(imageView, Constants.BRAND_IMAGE_PATH + name + ".webp")
        }

        private fun setImage(imageView: ImageView, theName: String?) {
            if (theName != null) {
                val context = imageView.context
                try {
                    val name = theName.replace(" ", "-")
                    val ims = context.assets.open(name)
                    imageView.setImageDrawable(Drawable.createFromStream(ims, null))
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}