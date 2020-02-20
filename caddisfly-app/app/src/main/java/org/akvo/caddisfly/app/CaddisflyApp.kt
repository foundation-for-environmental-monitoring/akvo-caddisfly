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
import android.os.Handler
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.akvo.caddisfly.BuildConfig
import org.akvo.caddisfly.R
import org.akvo.caddisfly.updater.UpdateCheck
import org.akvo.caddisfly.util.PreferencesUtil
import timber.log.Timber
import timber.log.Timber.DebugTree
import java.util.*

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
        db = Room.databaseBuilder(applicationContext,
                CalibrationDatabase::class.java, DATABASE_NAME)
                .allowMainThreadQueries()
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .build()
    }

    /**
     * Sets the language of the app on start. The language can be one of system language, language
     * set in the app preferences or language requested via the languageCode parameter
     *
     * @param languageCode If null uses language from app preferences else uses this value
     */
    fun setAppLanguage(languageCode: String?, isExternal: Boolean, handler: Handler?) {
        try {
            val locale: Locale
            var code = languageCode
            //the languages supported by the app
            val supportedLanguages = resources.getStringArray(R.array.language_codes)
            //the current system language set in the device settings
            val currentSystemLanguage = Locale.getDefault().language.substring(0, 2)
            //the language the system was set to the last time the app was run
            val previousSystemLanguage = PreferencesUtil.getString(this, R.string.systemLanguageKey, "")
            //if the system language was changed in the device settings then set that as the app language
            if (previousSystemLanguage != currentSystemLanguage
                    && listOf(*supportedLanguages).contains(currentSystemLanguage)) {
                PreferencesUtil.setString(this, R.string.systemLanguageKey, currentSystemLanguage)
                PreferencesUtil.setString(this, R.string.languageKey, currentSystemLanguage)
            }
            if (code == null || !listOf(*supportedLanguages).contains(code)) { //if requested language code is not supported then use language from preferences
                code = PreferencesUtil.getString(this, R.string.languageKey, "")
                if (!listOf(*supportedLanguages).contains(code)) { //no language was selected in the app settings so use the system language
                    val currentLanguage = resources.configuration.locale.language
                    code = when {
                        currentLanguage == currentSystemLanguage -> { //app is already set to correct language
                            return
                        }
                        listOf(*supportedLanguages).contains(currentSystemLanguage) -> { //set to system language
                            currentSystemLanguage
                        }
                        else -> { //no supported languages found just default to English
                            "en"
                        }
                    }
                }
            }
            val res = resources
            val dm = res.displayMetrics
            val config = res.configuration
            locale = Locale(code!!, Locale.getDefault().country)
            //if the app language is not already set to languageCode then set it now
            if (!config.locale.language.substring(0, 2).equals(code, ignoreCase = true)
                    || !config.locale.country.equals(Locale.getDefault().country, ignoreCase = true)) {
                config.locale = locale
                config.setLayoutDirection(locale)
                res.updateConfiguration(config, dm)
                //if this session was launched from an external app then do not restart this app
                if (!isExternal && handler != null) {
                    val msg = handler.obtainMessage()
                    handler.sendMessage(msg)
                }
            }
        } catch (ignored: Exception) { // do nothing
        }
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
                    String.format("%s %s", context.getString(R.string.version),
                            packageInfo.versionName)
                }
            } catch (ignored: PackageManager.NameNotFoundException) { // do nothing
            }
            return version
        }
    }
}