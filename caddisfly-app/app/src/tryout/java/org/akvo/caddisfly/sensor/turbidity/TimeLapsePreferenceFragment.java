package org.akvo.caddisfly.sensor.turbidity;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.akvo.caddisfly.R;

import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 */
public class TimeLapsePreferenceFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int MAX_SAMPLE_NUMBER = 100;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        String[] keys = {
                getString(R.string.colif_testIdKey),
                getString(R.string.colif_descriptionKey),
                getString(R.string.colif_brothMediaKey),
                getString(R.string.colif_volumeKey)};

        String format = "%s";
        if (Arrays.asList(keys).contains(key)) {
            Preference connectionPref = findPreference(key);
            if (key.equals(getString(R.string.colif_volumeKey))) {
                format = "%s ml";
            }
            connectionPref.setSummary(String.format(format, sharedPreferences.getString(key, "")));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_turbidity);

        final EditTextPreference sampleIntervalPreference =
                findPreference(getString(R.string.colif_intervalMinutesKey));
        if (sampleIntervalPreference != null) {

            sampleIntervalPreference.setSummary(String.format("Every %s minutes", sampleIntervalPreference.getText()));

            sampleIntervalPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                try {
                    if (Integer.parseInt(String.valueOf(newValue)) > 360) {
                        newValue = 360;
                    }

                    if (Integer.parseInt(String.valueOf(newValue)) < 1) {
                        newValue = 1;
                    }
                } catch (Exception e) {
                    newValue = 2;
                }

                sampleIntervalPreference.setText(String.valueOf(newValue));
                sampleIntervalPreference.setSummary(String.format("Every %s minutes", newValue));
                return false;
            });
        }

        final EditTextPreference samplesPreference =
                findPreference(getString(R.string.colif_numberOfSamplesKey));
        if (samplesPreference != null) {
            samplesPreference.setSummary(samplesPreference.getText());

            samplesPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                try {
                    if (Integer.parseInt(String.valueOf(newValue)) > MAX_SAMPLE_NUMBER) {
                        newValue = MAX_SAMPLE_NUMBER;
                    }

                    if (Integer.parseInt(String.valueOf(newValue)) < 1) {
                        newValue = 1;
                    }
                } catch (Exception e) {
                    newValue = 1;
                }
                samplesPreference.setText(String.valueOf(newValue));
                samplesPreference.setSummary(String.valueOf(newValue));
                return false;
            });
        }

        final EditTextPreference testIdPreference =
                findPreference(getString(R.string.colif_testIdKey));
        if (testIdPreference != null) {
            testIdPreference.setSummary(testIdPreference.getText());
        }

        final EditTextPreference testDescription =
                findPreference(getString(R.string.colif_descriptionKey));
        if (testDescription != null) {
            testDescription.setSummary(testDescription.getText());
        }

        final EditTextPreference brothPreference =
                findPreference(getString(R.string.colif_brothMediaKey));
        if (brothPreference != null) {
            brothPreference.setSummary(brothPreference.getText());
        }

        final EditTextPreference volumePreference =
                findPreference(getString(R.string.colif_volumeKey));

        if (volumePreference != null && volumePreference.getText() != null) {
            volumePreference.setSummary(String.format("%s ml", volumePreference.getText()));
        }

        final EditTextPreference notificationEmails =
                findPreference(getString(R.string.colif_emails));
        if (notificationEmails != null) {
            notificationEmails.setSummary(notificationEmails.getText());
            notificationEmails.setOnPreferenceChangeListener((preference, newValue) -> {
                notificationEmails.setText(newValue.toString().trim());
                notificationEmails.setSummary(newValue.toString().trim());
                return false;
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
