package org.akvo.caddisfly.sensor.turbidity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.util.PreferencesUtil;

import androidx.fragment.app.DialogFragment;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AuthDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AuthDialogFragment extends DialogFragment {

    private EditText editEmail = null;
    private EditText editPassword;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AuthDialogFragment.
     */
    public static AuthDialogFragment newInstance() {
        return new AuthDialogFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Activity activity = getActivity();
        LayoutInflater i = activity.getLayoutInflater();

        @SuppressLint("InflateParams")
        View view = i.inflate(R.layout.auth_dialog, null);

        editEmail = view.findViewById(R.id.editEmail);
        editEmail.requestFocus();
        showKeyboard(activity);

        editPassword = view.findViewById(R.id.editPassword);

        AlertDialog.Builder b = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.senderEmailDetails)
                .setMessage(R.string.useDevicesEmailDetails)
                .setPositiveButton(R.string.save,
                        (dialog, whichButton) -> {
                            closeKeyboard(activity, editEmail);
                            dismiss();
                        }
                )
                .setNegativeButton(R.string.cancel,
                        (dialog, whichButton) -> {
                            closeKeyboard(activity, editEmail);
                            dismiss();
                        }
                );

        b.setView(view);

        editEmail.setText(PreferencesUtil.getString(getActivity(), "username", ""));
        editPassword.setText(PreferencesUtil.getString(getActivity(), "password", ""));

        return b.create();
    }

    private void showKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        final Context context = getActivity();

        final AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (saveDetails()) {
                        closeKeyboard(context, editEmail);
                        dismiss();
                    }
                }

                private boolean saveDetails() {

                    String email = editEmail.getText().toString().trim();
                    if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        editEmail.requestFocus();
                        editEmail.setError(getString(R.string.invalidEmailAddress));
                        return false;
                    }

                    String password = editPassword.getText().toString().trim();
                    if (password.isEmpty()) {
                        editPassword.requestFocus();
                        editPassword.setError(getString(R.string.required));
                        return false;
                    }

                    PreferencesUtil.setString(context, "username", email);
                    PreferencesUtil.setString(context, "password", password);

                    return true;
                }
            });
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

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        closeKeyboard(getActivity(), editEmail);
    }

    @Override
    public void onPause() {
        super.onPause();
        closeKeyboard(getActivity(), editEmail);
    }
}
