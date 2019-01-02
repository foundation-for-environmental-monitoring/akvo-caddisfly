package org.akvo.caddisfly.preference;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.util.ListViewUtil;

import androidx.annotation.NonNull;

/**
 * A simple {@link Fragment} subclass.
 */
public class DebuggingPreferenceFragment extends PreferenceFragment {

    private ListView list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_debugging);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.card_row, container, false);
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
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse("file:/" + FileHelper.getAppFolder()), "*/*");
                    if (intent.resolveActivityInfo(getActivity().getPackageManager(), 0) != null) {
                        startActivity(intent);
                    }
                }
                return true;
            });
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        list = view.findViewById(android.R.id.list);
        (new Handler()).postDelayed(() -> ListViewUtil
                .setListViewHeightBasedOnChildren(list, 0), 200);
    }
}
