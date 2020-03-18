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
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
import org.akvo.caddisfly.R
import org.akvo.caddisfly.util.ApiUtil.startInstalledAppDetailsActivity

/**
 * Utility functions to show alert messages.
 */
object AlertUtil {
    private const val SNACK_BAR_LINE_SPACING = 1.4f

    /**
     * Displays an alert dialog.
     *
     * @param context the context
     * @param title   the title
     * @param message the message
     */
    fun showMessage(context: Context, @StringRes title: Int, @StringRes message: Int) {
        showAlert(context, title, message, null, null, null)
    }

    fun askQuestion(context: Context, @StringRes title: Int, @StringRes message: Int,
                    @StringRes okButtonText: Int, @StringRes cancelButtonText: Int,
                    isDestructive: Boolean,
                    positiveListener: DialogInterface.OnClickListener?,
                    cancelListener: DialogInterface.OnClickListener?) {
        showAlert(context, context.getString(title), context.getString(message), okButtonText,
                cancelButtonText, true, isDestructive, positiveListener,
                cancelListener
                        ?: DialogInterface.OnClickListener { dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss() },
                null)
    }

    fun showAlert(context: Context, @StringRes title: Int, message: String,
                  @StringRes okButtonText: Int,
                  positiveListener: DialogInterface.OnClickListener?,
                  negativeListener: DialogInterface.OnClickListener?,
                  cancelListener: DialogInterface.OnCancelListener?): AlertDialog {
        return showAlert(context, context.getString(title), message, okButtonText, R.string.cancel,
                true, isDestructive = false, positiveListener = positiveListener,
                negativeListener = negativeListener, cancelListener = cancelListener)
    }

    @JvmStatic
    fun showAlert(context: Context, @StringRes title: Int, @StringRes message: Int,
                  @StringRes okButtonText: Int,
                  positiveListener: DialogInterface.OnClickListener?,
                  negativeListener: DialogInterface.OnClickListener?,
                  cancelListener: DialogInterface.OnCancelListener?): AlertDialog {
        return showAlert(context, context.getString(title), context.getString(message), okButtonText,
                R.string.cancel, true, isDestructive = false, positiveListener = positiveListener,
                negativeListener = negativeListener, cancelListener = cancelListener)
    }

    @Suppress("SameParameterValue")
    private fun showAlert(context: Context, @StringRes title: Int, @StringRes message: Int,
                          positiveListener: DialogInterface.OnClickListener?,
                          negativeListener: DialogInterface.OnClickListener?,
                          cancelListener: DialogInterface.OnCancelListener?) {
        showAlert(context, context.getString(title), context.getString(message), R.string.ok, R.string.cancel,
                cancelable = true, isDestructive = false, positiveListener = positiveListener,
                negativeListener = negativeListener, cancelListener = cancelListener)
    }

    /**
     * Displays an alert dialog.
     *
     * @param context          the context
     * @param title            the title
     * @param message          the message
     * @param okButtonText     ok button text
     * @param positiveListener ok button listener
     * @param negativeListener cancel button listener
     * @return the alert dialog
     */
    private fun showAlert(context: Context, title: String, message: String,
                          @StringRes okButtonText: Int, @StringRes cancelButtonText: Int,
                          cancelable: Boolean, isDestructive: Boolean,
                          positiveListener: DialogInterface.OnClickListener?,
                          negativeListener: DialogInterface.OnClickListener?,
                          cancelListener: DialogInterface.OnCancelListener?): AlertDialog {
        val builder: AlertDialog.Builder
        builder = if (isDestructive) {
            val a = context.obtainStyledAttributes(R.styleable.BaseActivity)
            val style = a.getResourceId(R.styleable.BaseActivity_dialogDestructiveButton, 0)
            a.recycle()
            AlertDialog.Builder(context, style)
        } else {
            AlertDialog.Builder(context)
        }
        builder.setTitle(title)
                .setMessage(message)
                .setCancelable(cancelable)
        if (positiveListener != null) {
            builder.setPositiveButton(okButtonText, positiveListener)
        } else if (negativeListener == null) {
            builder.setNegativeButton(okButtonText) { dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss() }
        }
        if (negativeListener != null) {
            builder.setNegativeButton(cancelButtonText, negativeListener)
        }
        builder.setOnCancelListener(cancelListener)
        val alertDialog = builder.create()
        alertDialog.show()
        return alertDialog
    }

    /**
     * Displays an alert with error layout.
     *
     * @param context          the context
     * @param title            the title
     * @param message          the message
     * @param bitmap           a bitmap to show along with message
     * @param okButtonText     ok button text
     * @param positiveListener ok button listener
     * @param negativeListener cancel button listener
     * @return the alert dialog
     */
    @JvmStatic
    @SuppressLint("InflateParams")
    fun showError(context: Context, @StringRes title: Int,
                  message: String, bitmap: Bitmap?,
                  @StringRes okButtonText: Int,
                  positiveListener: DialogInterface.OnClickListener?,
                  negativeListener: DialogInterface.OnClickListener?,
                  cancelListener: DialogInterface.OnCancelListener?): AlertDialog {
        if (bitmap == null) {
            return showAlert(context, context.getString(title), message, okButtonText,
                    R.string.cancel, cancelable = false, isDestructive = false,
                    positiveListener = positiveListener, negativeListener = negativeListener,
                    cancelListener = cancelListener)
        }
        val alertDialog: AlertDialog
        val alertView: View
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        alertView = inflater.inflate(R.layout.dialog_error, null, false)
        builder.setView(alertView)
        builder.setTitle(R.string.error)
        builder.setMessage(message)
        val image = alertView.findViewById<ImageView>(R.id.imageSample)
        image.setImageBitmap(bitmap)
        if (positiveListener != null) {
            builder.setPositiveButton(okButtonText, positiveListener)
        }
        if (negativeListener == null) {
            builder.setNegativeButton(R.string.cancel, null)
        } else {
            var buttonText = R.string.cancel
            if (positiveListener == null) {
                buttonText = okButtonText
            }
            builder.setNegativeButton(buttonText, negativeListener)
        }
        builder.setCancelable(false)
        alertDialog = builder.create()
        if (context is Activity) {
            alertDialog.setOwnerActivity(context)
        }
        alertDialog.show()
        return alertDialog
    }

    /**
     * Displays snackbar with settings button and {@param message}
     *
     * @param rootView The root view of the activity.
     * @param message  The text to show.
     */
    fun showSettingsSnackbar(activity: Activity?, rootView: View?, message: String) {
        val snackbar = Snackbar
                .make(rootView!!, message.trim { it <= ' ' }, Snackbar.LENGTH_LONG)
                .setAction("SETTINGS") { startInstalledAppDetailsActivity(activity) }
        val snackbarView = snackbar.view
        val textView = snackbarView.findViewById<TextView>(R.id.snackbar_text)
        textView.setTextColor(Color.WHITE)
        textView.setLineSpacing(0f, SNACK_BAR_LINE_SPACING)
        snackbar.setActionTextColor(Color.YELLOW)
        snackbar.show()
    }


}