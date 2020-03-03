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
@file:Suppress("DEPRECATION")

package org.akvo.caddisfly.util

import android.hardware.Camera

object TheCamera {
    /**
     * A safe way to get an instance of the Camera object.
     */
    @JvmStatic
    val cameraInstance: Camera?
        get() {
            var c: Camera? = null
            try {
                c = Camera.open() // attempt to get a Camera instance
            } catch (e: Exception) { // Camera is not available (in use or does not exist)
            }
            return c // returns null if camera is unavailable
        }
}