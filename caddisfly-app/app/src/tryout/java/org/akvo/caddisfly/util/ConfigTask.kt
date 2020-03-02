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

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.widget.Toast
import org.akvo.caddisfly.helper.FileHelper
import org.akvo.caddisfly.ui.TestListActivity.SyncCallbackInterface
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL

class ConfigTask(context: Context, syncCallback: SyncCallbackInterface?) : AsyncTask<String?, String?, String?>() {
    private val contextRef: WeakReference<Context> = WeakReference(context)
    private val configSyncHandler: SyncCallbackInterface? = syncCallback
    private var fileType: FileHelper.FileType? = null
    private var pd: ProgressDialog? = null
    override fun onPreExecute() {
        super.onPreExecute()
        pd = ProgressDialog(contextRef.get())
        pd!!.setMessage("Please wait...")
        pd!!.setCancelable(false)
        pd!!.show()
    }

    override fun doInBackground(vararg params: String?): String? {
        var connection: HttpURLConnection? = null
        var reader: BufferedReader? = null
        try {
            val url = URL(params[0])
            connection = url.openConnection() as HttpURLConnection
            connection.connect()
            fileType = FileHelper.FileType.valueOf(params[1]!!)
            val stream = connection.inputStream
            reader = BufferedReader(InputStreamReader(stream))
            val buffer = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                buffer.append(line).append("\n")
            }
            return buffer.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            connection?.disconnect()
            try {
                reader?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        if (pd!!.isShowing) {
            pd!!.dismiss()
        }
        val path = FileHelper.getFilesDir(fileType, "")
        FileUtil.saveToFile(path, "tests.json", result)
        configSyncHandler?.onDownloadFinished()
        Toast.makeText(contextRef.get(), "Experimental tests synced", Toast.LENGTH_LONG).show()
    }
}