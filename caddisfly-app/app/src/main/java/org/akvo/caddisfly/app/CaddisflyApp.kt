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
package org.akvo.caddisfly.app

import android.content.Context
import android.content.pm.PackageManager
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.akvo.caddisfly.BuildConfig
import org.akvo.caddisfly.R
import org.akvo.caddisfly.updater.UpdateCheck
import timber.log.Timber
import timber.log.Timber.DebugTree

@Suppress("DEPRECATION")
class CaddisflyApp : BaseApplication() {
    override fun onCreate() {
        super.onCreate()
        app = this
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
        app = this
        UpdateCheck.setNextUpdateCheck(this, -1)
        db = Room.databaseBuilder(
            applicationContext,
            CalibrationDatabase::class.java, DATABASE_NAME
        ).allowMainThreadQueries()
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .build()
    }

    companion object {
        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Calibration" + " ADD COLUMN image TEXT")
                database.execSQL("ALTER TABLE Calibration" + " ADD COLUMN croppedImage TEXT")
            }
        }
        private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE CalibrationDetail" + " ADD COLUMN cuvetteType TEXT")
            }
        }
        private val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Calibration" + " ADD COLUMN quality INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE Calibration" + " ADD COLUMN zoom INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE Calibration" + " ADD COLUMN resWidth INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE Calibration" + " ADD COLUMN resHeight INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE Calibration" + " ADD COLUMN centerOffset INTEGER NOT NULL DEFAULT 0")
            }
        }
        private val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE CalibrationDetail" + " ADD COLUMN fileName TEXT")
            }
        }
        private const val DATABASE_NAME = "calibration"

        @JvmStatic
        var db: CalibrationDatabase? = null
            private set

        /**
         * Gets the singleton app object.
         *
         * @return the singleton app
         */
        @JvmStatic
        var app // Singleton
                : CaddisflyApp? = null
            private set


        /**
         * Gets the app version.
         *
         * @return The version name and number
         */
        @JvmStatic
        fun getAppVersion(isDiagnostic: Boolean): String {
            var version = ""
            try {
                val context: Context? = app
                val packageInfo = context!!.packageManager.getPackageInfo(context.packageName, 0)
                version = if (isDiagnostic) {
                    String.format("%s (Build %s)", packageInfo.versionName, packageInfo.versionCode)
                } else {
                    String.format(
                        "%s %s", context.getString(R.string.version),
                        packageInfo.versionName
                    )
                }
            } catch (ignored: PackageManager.NameNotFoundException) { // do nothing
            }
            return version
        }
    }
}