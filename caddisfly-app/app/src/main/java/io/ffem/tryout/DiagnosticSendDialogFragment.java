package io.ffem.tryout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import org.akvo.caddisfly.R;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import timber.log.Timber;

public class DiagnosticSendDialogFragment extends DialogFragment {

    private OnDetailsSavedListener mListener;
    private EditText editComment;

    public DiagnosticSendDialogFragment() {
        // Required empty public constructor
    }

    public static DiagnosticSendDialogFragment newInstance() {
        DiagnosticSendDialogFragment fragment = new DiagnosticSendDialogFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Activity activity = getActivity();
        LayoutInflater i = Objects.requireNonNull(activity).getLayoutInflater();

        @SuppressLint("InflateParams")
        View view = i.inflate(R.layout.fragment_diagnostic_send_dialog, null);

        editComment = view.findViewById(R.id.comment);

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle("Details")
                .setPositiveButton(R.string.sendData, null) //Set to null. We override the onclick
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view1 -> {
                closeKeyboard(getActivity(), editComment);
                mListener.onDetailsSaved(editComment.getText().toString());
                dismiss();
            });
        });
        dialog.show();

        editComment.requestFocus();
        showKeyboard(activity);

        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDetailsSavedListener) {
            mListener = (OnDetailsSavedListener) context;
        } else {
            throw new IllegalArgumentException(context.toString()
                    + " must implement OnDetailsSavedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnDetailsSavedListener {
        void onDetailsSaved(String s);
    }

    private void showKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    /**
     * Hides the keyboard.
     *
     * @param input the EditText for which the keyboard is open
     */
    private void closeKeyboard(Context context, EditText input) {
        try {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                if (getActivity() != null) {
                    View view = getActivity().getCurrentFocus();
                    if (view != null) {
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }
}
