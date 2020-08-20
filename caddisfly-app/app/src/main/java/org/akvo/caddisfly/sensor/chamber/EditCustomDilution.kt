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
package org.akvo.caddisfly.sensor.chamber

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import org.akvo.caddisfly.R
import org.akvo.caddisfly.sensor.chamber.EditCustomDilution.OnCustomDilutionListener

/**
 * Activities that contain this fragment must implement the
 * [OnCustomDilutionListener] interface
 * to handle interaction events.
 * Use the [EditCustomDilution.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditCustomDilution : DialogFragment() {
    private var mListener: OnCustomDilutionListener? = null
    private var editDilutionFactor: EditText? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity: Activity? = activity
        @SuppressLint("InflateParams") val view =
            requireActivity().layoutInflater.inflate(R.layout.edit_custom_dilution, null)
        editDilutionFactor = view.findViewById(R.id.editDilutionFactor)
        editDilutionFactor?.requestFocus()
        val b = AlertDialog.Builder(activity)
            .setTitle(R.string.custom_dilution)
                .setPositiveButton(R.string.ok
                ) { _: DialogInterface?, _: Int ->
                    closeKeyboard(editDilutionFactor)
                    dismiss()
                }
                .setNegativeButton(R.string.cancel
                ) { _: DialogInterface?, _: Int ->
                    closeKeyboard(editDilutionFactor)
                    dismiss()
                }
        b.setView(view)
        return b.create()
    }

    override fun onStart() {
        super.onStart()
        val d = dialog as AlertDialog?
        if (d != null) {
            val positiveButton = d.getButton(Dialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    if (formEntryValid() && editDilutionFactor!!.text.toString().trim { it <= ' ' }
                            .isNotEmpty()) {
                        if (mListener != null) {
                            mListener!!.onCustomDilution(
                                editDilutionFactor!!.text.toString().toInt()
                            )
                        }
                        closeKeyboard(editDilutionFactor)
                        dismiss()
                    }
                }

                private fun formEntryValid(): Boolean {
                    if (editDilutionFactor!!.text.toString().trim { it <= ' ' }.isEmpty()) {
                        editDilutionFactor!!.error = getString(R.string.required)
                        return false
                    }
                    return true
                }
            })
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = if (context is OnCustomDilutionListener) {
            context
        } else {
            throw IllegalArgumentException(context.toString()
                    + " must implement OnResultListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onResume() {
        super.onResume()
        editDilutionFactor!!.post {
            editDilutionFactor!!.requestFocus()
            val imm = editDilutionFactor!!.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editDilutionFactor, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    /**
     * Hides the keyboard.
     *
     * @param input the EditText for which the keyboard is open
     */
    private fun closeKeyboard(input: EditText?) {
        val imm = requireContext().getSystemService(
            Context.INPUT_METHOD_SERVICE
        ) as InputMethodManager
        imm.hideSoftInputFromWindow(input!!.windowToken, 0)
    }

    interface OnCustomDilutionListener {
        fun onCustomDilution(value: Int)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         */
        @JvmStatic
        fun newInstance(): EditCustomDilution {
            val fragment = EditCustomDilution()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}