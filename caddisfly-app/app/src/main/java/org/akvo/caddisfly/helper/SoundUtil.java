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

package org.akvo.caddisfly.helper;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

import org.akvo.caddisfly.common.AppConfig;
import org.akvo.caddisfly.preference.AppPreferences;

import androidx.annotation.RawRes;

import static android.content.Context.AUDIO_SERVICE;

/**
 * Sound utils.
 */
public class SoundUtil {
    /**
     * Play a short sound effect.
     *
     * @param resourceId the
     */
    public static void playShortResource(Context context, @RawRes int resourceId) {

        //play sound if the sound is not turned off in the preference
        if (AppPreferences.isSoundOn()) {
            final AudioManager audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
            final int originalVolume;
            if (audioManager != null) {
                originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                //noinspection ConstantConditions
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                        AppConfig.SOUND_ON ? audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) : 1,
                        0);
                MediaPlayer mp = MediaPlayer.create(context, resourceId);
                mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mp.start();
                mp.setOnCompletionListener(mediaPlayer -> {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
                    mp.release();
                });
            }
        }
    }
}
