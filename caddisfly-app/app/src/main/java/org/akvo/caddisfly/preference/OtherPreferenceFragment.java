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

package org.akvo.caddisfly.preference;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.entity.Calibration;
import org.akvo.caddisfly.helper.SwatchHelper;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestSampleType;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.ui.AboutActivity;
import org.akvo.caddisfly.util.ListViewUtil;
import org.akvo.caddisfly.viewmodel.TestListViewModel;

import java.lang.ref.WeakReference;
import java.util.List;

public class OtherPreferenceFragment extends PreferenceFragment {

    static StringBuilder message = new StringBuilder();
    private ListView list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_other);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.card_row, container, false);

        Preference aboutPreference = findPreference("about");
        if (aboutPreference != null) {
            aboutPreference.setSummary(CaddisflyApp.getAppVersion(AppPreferences.isDiagnosticMode()));
            aboutPreference.setOnPreferenceClickListener(preference -> {
                final Intent intent = new Intent(getActivity(), AboutActivity.class);
                getActivity().startActivity(intent);
                return true;
            });
        }

        Preference emailSupportPreference = findPreference("emailSupport");
        if (emailSupportPreference != null) {
            emailSupportPreference.setSummary(R.string.send_details_to_support);
            emailSupportPreference.setOnPreferenceClickListener(preference -> {

                message.setLength(0);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.emailSupport);
                builder.setMessage(getString(R.string.if_you_need_assistance) + "\n\n" +
                        getString(R.string.select_email_app))
                        .setCancelable(false)
                        .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
                        .setPositiveButton(R.string.create_support_email, (dialog, id) -> {

                            dialog.dismiss();

                            final ProgressDialog progressDialog =
                                    new ProgressDialog(getActivity(), android.R.style.Theme_DeviceDefault_Light_Dialog);

                            // START AsyncTask
                            GenerateMessageAsyncTask generateMessageAsyncTask = new GenerateMessageAsyncTask(this);
                            generateMessageAsyncTask.setListener(value -> {
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }

                                sendEmail(getActivity(), message.toString());

                            });

                            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            progressDialog.setIndeterminate(true);
                            progressDialog.setTitle(R.string.creatingMessage);
                            progressDialog.setMessage(getString(R.string.just_a_moment));
                            progressDialog.setCancelable(false);

                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && progressDialog.getWindow() != null) {
                                progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            }
                            progressDialog.show();

                            generateMessageAsyncTask.execute();

                        }).show();

                return true;
            });
        }

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        list = view.findViewById(android.R.id.list);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ListViewUtil.setListViewHeightBasedOnChildren(list, 0);
    }

    public void sendEmail(Context context, String message) {
        try {
            String email = "devices@ternup.com";
            String subject = "Support request";

            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri data = Uri.parse("mailto:?to=" + email + "&subject=" + subject + "&body=" + message);
            intent.setData(data);
            startActivity(intent);

        } catch (Throwable t) {
            Toast.makeText(context, "Request failed try again: " + t.toString(), Toast.LENGTH_LONG).show();
        }
    }

    static class GenerateMessageAsyncTask extends AsyncTask<Void, Void, Integer> {
        private ExampleAsyncTaskListener listener;

        private WeakReference<OtherPreferenceFragment> activityReference;

        // only retain a weak reference to the activity
        GenerateMessageAsyncTask(OtherPreferenceFragment fragment) {
            activityReference = new WeakReference<>(fragment);
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            final TestListViewModel viewModel =
                    ViewModelProviders.of((FragmentActivity) activityReference.get()
                            .getActivity()).get(TestListViewModel.class);

            List<TestInfo> testList = viewModel.getTests(TestType.CHAMBER_TEST, TestSampleType.ALL);


            try {
                for (TestInfo testInfo : testList) {

                    if (testInfo.getIsGroup()) {
                        continue;
                    }

                    testInfo = viewModel.getTestInfo(testInfo.getUuid());

                    boolean calibrated = false;
                    for (Calibration calibration :
                            testInfo.getCalibrations()) {
                        if (calibration.color != Color.TRANSPARENT &&
                                calibration.color != Color.BLACK) {
                            calibrated = true;
                            break;
                        }
                    }

                    if (calibrated) {
                        message.append(SwatchHelper.generateCalibrationFile(activityReference.get().getActivity(), testInfo, false));

                        message.append("\n");

                        message.append("-------------------------------------------------");

                        message.append("\n");
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (message.toString().isEmpty()) {
                message.append("No calibrations found");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Integer value) {
            super.onPostExecute(value);
            if (listener != null) {
                listener.onExampleAsyncTaskFinished(value);
            }
        }

        public void setListener(ExampleAsyncTaskListener listener) {
            this.listener = listener;
        }

        public interface ExampleAsyncTaskListener {
            void onExampleAsyncTaskFinished(Integer value);
        }
    }
}
