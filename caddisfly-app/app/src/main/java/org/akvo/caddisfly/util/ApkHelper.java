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

package org.akvo.caddisfly.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.akvo.caddisfly.AppConfig;
import org.akvo.caddisfly.R;

import java.util.GregorianCalendar;

public final class ApkHelper {

    private static final String INSTALLER_ADB = "adb";

    private ApkHelper() {
    }

    public static boolean isAppVersionExpired(final Activity activity) {
        if (!isStoreVersion(activity)) {
            final Uri marketUrl = Uri.parse("market://details?id=" + activity.getPackageName());
            GregorianCalendar now = new GregorianCalendar();
            if (now.after(AppConfig.APP_EXPIRY_DATE)) {

                String message = String.format("%s%n%n%s", activity.getString(R.string.thisVersionHasExpired),
                        activity.getString(R.string.uninstallAndInstallFromStore));

                AlertDialog.Builder builder;
                builder = new AlertDialog.Builder(activity);

                builder.setTitle(R.string.versionExpired)
                        .setMessage(message)
                        .setCancelable(false);

                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        activity.startActivity(new Intent(Intent.ACTION_VIEW, marketUrl));
                        activity.finish();
                    }
                });

                final AlertDialog alertDialog = builder.create();
                alertDialog.show();

                return true;
            }
        }

        return false;
    }

    public static boolean isStoreVersion(@NonNull Context context) {
        boolean result = false;

        try {
            String installer = context.getPackageManager()
                    .getInstallerPackageName(context.getPackageName());
            result = installer != null && !TextUtils.isEmpty(installer);
        } catch (Exception ignored) {
        }

        return result;
    }


}
