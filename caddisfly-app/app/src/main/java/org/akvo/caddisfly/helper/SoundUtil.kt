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
package org.akvo.caddisfly.helper

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import androidx.annotation.RawRes
import org.akvo.caddisfly.BuildConfig
import org.akvo.caddisfly.preference.AppPreferences


/**
 * Sound utils.
 */
object SoundUtil {
    /**
     * Play a short sound effect.
     *
     * @param resourceId the
     */
    @JvmStatic
    fun playShortResource(context: Context, @RawRes resourceId: Int) {
        //play sound if the sound is not turned off in the preference
        if (AppPreferences.isSoundOn) {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val originalVolume: Int = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    if (BuildConfig.TEST_RUNNING) 1 else audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                    0)
            val mp = MediaPlayer.create(context, resourceId)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mp.setAudioAttributes(
                        AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build())
            } else {
                @Suppress("DEPRECATION")
                mp.setAudioStreamType(AudioManager.STREAM_MUSIC)
            }
            mp.start()
            mp.setOnCompletionListener {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0)
                mp.release()
            }
        }
    }
}