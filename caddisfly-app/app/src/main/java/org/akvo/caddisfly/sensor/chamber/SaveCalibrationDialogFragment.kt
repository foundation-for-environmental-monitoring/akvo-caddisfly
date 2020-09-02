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
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import org.akvo.caddisfly.R
import org.akvo.caddisfly.app.CaddisflyApp.Companion.db
import org.akvo.caddisfly.helper.FileHelper.getFilesDir
import org.akvo.caddisfly.helper.FileType
import org.akvo.caddisfly.helper.SwatchHelper.generateCalibrationFile
import org.akvo.caddisfly.model.TestInfo
import org.akvo.caddisfly.preference.AppPreferences.isDiagnosticMode
import org.akvo.caddisfly.util.AlertUtil
import org.akvo.caddisfly.util.FileUtil.saveToFile
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Use the [SaveCalibrationDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SaveCalibrationDialogFragment : DialogFragment() {
    private val calendar = Calendar.getInstance()
    private var testInfo: TestInfo? = null
    private var editName: EditText? = null
    private var editExpiryDate: EditText? = null
    private var isEditing = false

    //    private Spinner spinnerCuvette;
    //    private TextView textError;
    private var mListener: OnCalibrationDetailsSavedListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            testInfo = requireArguments().getParcelable(ARG_TEST_INFO)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity: Activity? = activity
        val i = activity?.layoutInflater
        @SuppressLint("InflateParams") val view =
            i?.inflate(R.layout.fragment_save_calibration, null)
        editExpiryDate = view?.findViewById(R.id.editExpiryDate)
        val calibrationDetail = db?.calibrationDao()!!.getCalibrationDetails(testInfo!!.uuid)
        if (calibrationDetail!!.expiry > Date().time) {
            if (calibrationDetail.expiry >= 0) {
                calendar.timeInMillis = calibrationDetail.expiry
                editExpiryDate?.setText(
                    SimpleDateFormat("dd-MMM-yyyy", Locale.US)
                        .format(Date(calibrationDetail.expiry))
                )
            }
        }
        setupDatePicker()
        editName = view?.findViewById(R.id.editName)
        if (!isEditing && isDiagnosticMode()) {
            editName?.requestFocus()
            showKeyboard()
        } else {
            editName?.visibility = View.GONE
        }
        val b = AlertDialog.Builder(getActivity())
            .setTitle(R.string.calibration_details)
            .setPositiveButton(
                R.string.save
            ) { _: DialogInterface?, _: Int ->
                closeKeyboard(editName)
                dismiss()
            }
            .setNegativeButton(
                R.string.cancel
            ) { _: DialogInterface?, _: Int ->
                closeKeyboard(editName)
                dismiss()
            }
        b.setView(view)
        return b.create()
    }

    private fun showKeyboard() {
        val imm = requireContext().getSystemService(
            Context.INPUT_METHOD_SERVICE
        ) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun setupDatePicker() {
        val onDateSetListener =
            OnDateSetListener { _: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                calendar[Calendar.YEAR] = year
                calendar[Calendar.MONTH] = monthOfYear
                calendar[Calendar.DAY_OF_MONTH] = dayOfMonth
                val date = SimpleDateFormat("dd MMM yyyy", Locale.US).format(calendar.time)
                editExpiryDate!!.setText(date)
            }
        val datePickerDialog = DatePickerDialog(
            requireContext(), onDateSetListener,
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]
        )
        val date = Calendar.getInstance()
        date.add(Calendar.DATE, 1)
        date[Calendar.HOUR_OF_DAY] = date.getMinimum(Calendar.HOUR_OF_DAY)
        date[Calendar.MINUTE] = date.getMinimum(Calendar.MINUTE)
        date[Calendar.SECOND] = date.getMinimum(Calendar.SECOND)
        date[Calendar.MILLISECOND] = date.getMinimum(Calendar.MILLISECOND)
        datePickerDialog.datePicker.minDate = date.timeInMillis
        if (testInfo!!.monthsValid != null) {
            date.add(Calendar.MONTH, testInfo!!.monthsValid!!)
            date[Calendar.HOUR_OF_DAY] = date.getMaximum(Calendar.HOUR_OF_DAY)
            date[Calendar.MINUTE] = date.getMaximum(Calendar.MINUTE)
            date[Calendar.SECOND] = date.getMaximum(Calendar.SECOND)
            date[Calendar.MILLISECOND] = date.getMaximum(Calendar.MILLISECOND)
            datePickerDialog.datePicker.maxDate = date.timeInMillis
        }
        editExpiryDate!!.onFocusChangeListener = OnFocusChangeListener { _: View?, b: Boolean ->
            if (b) {
                closeKeyboard(editName)
                datePickerDialog.show()
            }
        }
        editExpiryDate!!.setOnClickListener {
            closeKeyboard(editName)
            datePickerDialog.show()
        }
    }

    override fun onStart() {
        super.onStart()
        val context: Context = requireActivity()
        val d = dialog as AlertDialog?
        if (d != null) {
            val positiveButton = d.getButton(Dialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    if (formEntryValid()) {
                        if (editName!!.text.toString().trim { it <= ' ' }.isNotEmpty()) {
                            val path = getFilesDir(FileType.CALIBRATION, testInfo!!.uuid)
                            val file = File(path, editName!!.text.toString())
                            if (file.exists()) {
                                AlertUtil.askQuestion(
                                    context,
                                    R.string.fileAlreadyExists,
                                    R.string.doYouWantToOverwrite,
                                    R.string.overwrite,
                                    R.string.cancel,
                                    true,
                                    { _: DialogInterface?, _: Int ->
                                        saveDetails(testInfo!!.uuid, file.name)
                                        saveCalibrationDetails(path)
                                        closeKeyboard(editName)
                                        dismiss()
                                    },
                                    null
                                )
                            } else {
                                saveDetails(testInfo!!.uuid, file.name)
                                saveCalibrationDetails(path)
                                closeKeyboard(editName)
                                dismiss()
                            }
                        } else {
                            saveDetails(testInfo!!.uuid, "")
                            closeKeyboard(editExpiryDate)
                            dismiss()
                        }
                    }
                }

                fun saveDetails(uuid: String?, fileName: String) {
                    val dao = db!!.calibrationDao()
                    val calibrationDetail = dao!!.getCalibrationDetails(uuid)
                    calibrationDetail!!.uid = uuid!!
                    calibrationDetail.date = Calendar.getInstance().timeInMillis
                    calibrationDetail.expiry = calendar.timeInMillis
                    if (fileName.isNotEmpty()) {
                        calibrationDetail.fileName = fileName
                    }
                    dao.insert(calibrationDetail)
                    mListener!!.onCalibrationDetailsSaved()
                }

                private fun formEntryValid(): Boolean {
                    if (!isEditing && isDiagnosticMode()
                        && editName!!.text.toString().trim { it <= ' ' }.isEmpty()
                    ) {
                        editName!!.error = getString(R.string.saveInvalidFileName)
                        return false
                    }
                    if (editExpiryDate!!.text.toString().trim { it <= ' ' }.isEmpty()) {
                        editExpiryDate!!.error = getString(R.string.required)
                        return false
                    }
                    return true
                }
            })
        }
    }

    private fun saveCalibrationDetails(path: File) {
        val context = context
        val calibrationDetails = generateCalibrationFile(context, testInfo!!, true)
        saveToFile(path, editName!!.text.toString().trim { it <= ' ' }, calibrationDetails)
        Toast.makeText(context, R.string.fileSaved, Toast.LENGTH_SHORT).show()
    }

    /**
     * Hides the keyboard.
     *
     * @param input the EditText for which the keyboard is open
     */
    private fun closeKeyboard(input: EditText?) {
        try {
            val imm = requireContext().getSystemService(
                Context.INPUT_METHOD_SERVICE
            ) as InputMethodManager
            imm.hideSoftInputFromWindow(input!!.windowToken, 0)
            if (activity != null) {
                val view = requireActivity().currentFocus
                if (view != null) {
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        closeKeyboard(editName)
    }

    override fun onPause() {
        super.onPause()
        closeKeyboard(editName)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = if (context is OnCalibrationDetailsSavedListener) {
            context
        } else {
            throw IllegalArgumentException(
                context.toString()
                        + " must implement OnCalibrationDetailsSavedListener"
            )
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface OnCalibrationDetailsSavedListener {
        fun onCalibrationDetailsSaved()
    }

    companion object {
        private const val ARG_TEST_INFO = "testInfo"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment SaveCalibrationDialogFragment.
         */
        fun newInstance(testInfo: TestInfo?, isEdit: Boolean): SaveCalibrationDialogFragment {
            val fragment = SaveCalibrationDialogFragment()
            fragment.isEditing = isEdit
            val args = Bundle()
            args.putParcelable(ARG_TEST_INFO, testInfo)
            fragment.arguments = args
            return fragment
        }
    }
}