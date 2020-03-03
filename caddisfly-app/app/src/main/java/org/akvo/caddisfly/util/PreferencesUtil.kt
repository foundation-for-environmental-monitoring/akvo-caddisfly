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
package org.akvo.caddisfly.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.preference.PreferenceManager

/**
 * Various utility functions to get/set values from/to SharedPreferences.
 */
object PreferencesUtil {
    private const val KEY_FORMAT = "%s_%s"

    /**
     * Gets a preference key from strings
     *
     * @param context the context
     * @param keyId   the key id
     * @return the string key
     */
    private fun getKey(context: Context, @StringRes keyId: Int): String {
        return context.getString(keyId)
    }

    /**
     * Gets a boolean value from preferences.
     *
     * @param context      the context
     * @param keyId        the key id
     * @param defaultValue the default value
     * @return the stored boolean value
     */
    @JvmStatic
    fun getBoolean(context: Context, @StringRes keyId: Int, defaultValue: Boolean): Boolean {
        val sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context)
        return sharedPreferences.getBoolean(getKey(context, keyId), defaultValue)
    }

    @JvmStatic
    fun getBoolean(context: Context?, keyId: String?, defaultValue: Boolean): Boolean {
        val sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context)
        return sharedPreferences.getBoolean(keyId, defaultValue)
    }

    /**
     * Sets a boolean value to preferences.
     *
     * @param context the context
     * @param keyId   the key id
     * @param value   the value
     */
    @JvmStatic
    fun setBoolean(context: Context, @StringRes keyId: Int, value: Boolean) {
        val sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putBoolean(getKey(context, keyId), value)
        editor.apply()
    }

    @Suppress("unused")
    @JvmStatic
    fun setBoolean(context: Context?, keyId: String?, value: Boolean) {
        val sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putBoolean(keyId, value)
        editor.apply()
    }

    /**
     * Gets an integer value from preferences.
     *
     * @param context      the context
     * @param key          the key id
     * @param defaultValue the default value
     * @return stored int value
     */
    @JvmStatic
    fun getInt(context: Context?, key: String?, defaultValue: Int): Int {
        val sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context)
        return sharedPreferences.getInt(key, defaultValue)
    }

    /**
     * Gets a long value from preferences.
     *
     * @param context the context
     * @param keyId   the key id
     * @return the stored long value
     */
    @JvmStatic
    fun getInt(context: Context, @StringRes keyId: Int, defaultValue: Int): Int {
        return getInt(context, getKey(context, keyId), defaultValue)
    }

    @JvmStatic
    fun setInt(context: Context, @StringRes keyId: Int, value: Int) {
        setInt(context, getKey(context, keyId), value)
    }

    /**
     * Sets an integer value to preferences.
     *
     * @param context the context
     * @param key     the key id
     * @param value   the value to set
     */
    @JvmStatic
    fun setInt(context: Context?, key: String?, value: Int) {
        val sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    /**
     * Gets a long value from preferences.
     *
     * @param context the context
     * @param keyId   the key id
     * @return the stored long value
     */
    @JvmStatic
    fun getLong(context: Context, code: String?, @StringRes keyId: Int): Long {
        val key = String.format(KEY_FORMAT, code, getKey(context, keyId))
        return getLong(context, key)
    }

    /**
     * Gets a long value from preferences.
     *
     * @param context the context
     * @param key     the key id
     * @return the stored long value
     */
    @JvmStatic
    fun getLong(context: Context?, key: String?): Long {
        val sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context)
        return sharedPreferences.getLong(key, -1L)
    }

//    fun setLong(context: Context, code: String?, @StringRes keyId: Int, value: Long) {
//        val key = String.format(KEY_FORMAT, code, getKey(context, keyId))
//        setLong(context, key, value)
//    }
//
//    /**
//     * Sets a long value to preferences.
//     *
//     * @param context the context
//     * @param keyId   the key id
//     * @param value   the value
//     */
//    fun setLong(context: Context, @StringRes keyId: Int, value: Long) {
//        setLong(context, getKey(context, keyId), value)
//    }

    /**
     * Sets a long value to preferences.
     *
     * @param context the context
     * @param key     the int key id
     */
    @JvmStatic
    fun setLong(context: Context?, key: String?, value: Long) {
        val sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    /**
     * Gets a string value from preferences.
     *
     * @param context      the context
     * @param keyId        the key id
     * @param defaultValue default value
     * @return the stored string value
     */
    @JvmStatic
    fun getString(context: Context, @StringRes keyId: Int, defaultValue: String?): String? {
        val sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context)
        return sharedPreferences.getString(getKey(context, keyId), defaultValue)
    }

    /**
     * Gets a string value from preferences.
     *
     * @param context      the context
     * @param keyId        the key id
     * @param defaultValue default value
     * @return the stored string value
     */
    @JvmStatic
    fun getString(context: Context?, keyId: String?, defaultValue: String?): String? {
        val sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context)
        return sharedPreferences.getString(keyId, defaultValue)
    }

    @JvmStatic
    fun getString(context: Context, code: String?, @StringRes keyId: Int, defaultValue: String?): String? {
        val key = String.format(KEY_FORMAT, code, getKey(context, keyId))
        return getString(context, key, defaultValue)
    }

    /**
     * Sets a string value to preferences.
     *
     * @param context the context
     * @param keyId   the key id
     */
    @JvmStatic
    fun setString(context: Context, @StringRes keyId: Int, value: String?) {
        val sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putString(getKey(context, keyId), value)
        editor.apply()
    }

//    fun setString(context: Context, code: String?, @StringRes keyId: Int, value: String?) {
//        val key = String.format(KEY_FORMAT, code, getKey(context, keyId))
//        setString(context, key, value)
//    }

    /**
     * Sets a string value to preferences.
     *
     * @param context the context
     * @param keyId   the key id
     */
    @Suppress("unused")
    @JvmStatic
    fun setString(context: Context?, keyId: String?, value: String?) {
        val sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putString(keyId, value)
        editor.apply()
    }

//    fun removeKey(context: Context, @StringRes keyId: Int) {
//        removeKey(context, getKey(context, keyId))
//    }
//
//    /**
//     * Removes the key from the preferences.
//     *
//     * @param context the context
//     * @param key     the key id
//     */
//    private fun removeKey(context: Context, key: String) {
//        val sharedPreferences = PreferenceManager
//                .getDefaultSharedPreferences(context)
//        val editor = sharedPreferences.edit()
//        editor.remove(key)
//        editor.apply()
//    }

    /**
     * Checks if the key is already saved in the preferences.
     *
     * @param context the context
     * @param keyId   the key id
     * @return true if found
     */
    @JvmStatic
    fun containsKey(context: Context, @StringRes keyId: Int): Boolean {
        val sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context)
        return sharedPreferences.contains(getKey(context, keyId))
    }
}