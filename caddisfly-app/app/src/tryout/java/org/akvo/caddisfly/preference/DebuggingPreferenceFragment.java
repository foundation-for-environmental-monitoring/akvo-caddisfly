package org.akvo.caddisfly.preference;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.helper.FileHelper;

/**
 * A simple {@link Fragment} subclass.
 */
public class DebuggingPreferenceFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_debugging);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.setBackgroundColor(Color.rgb(255, 240, 220));
        Preference aboutPreference = findPreference("showFolderKey");
        if (aboutPreference != null) {
            aboutPreference.setOnPreferenceClickListener(preference -> {
                if (Build.MANUFACTURER.equalsIgnoreCase("samsung")) {
                    Intent intent = getActivity().getPackageManager()
                            .getLaunchIntentForPackage("com.sec.android.app.myfiles");
                    if (intent != null) {
                        intent.setAction("samsung.myfiles.intent.action.LAUNCH_MY_FILES");
                        intent.putExtra("samsung.myfiles.intent.extra.START_PATH",
                                FileHelper.getAppFolder());
                        startActivity(intent);
                    }
                } else {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse("file:/" + FileHelper.getAppFolder()), "*/*");
                        if (intent.resolveActivityInfo(getActivity().getPackageManager(), 0) != null) {
                            startActivity(intent);
                        }
                    } catch (Exception e) {
                    }
                }
                return true;
            });
        }
    }
}
