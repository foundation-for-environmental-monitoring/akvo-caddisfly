package org.akvo.caddisfly.ui

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.android.AndroidInjection
import org.akvo.caddisfly.R
import org.akvo.caddisfly.preference.AppPreferences.isAppUpdateCheckRequired
import java.util.concurrent.Executor
import javax.inject.Inject

private const val IMMEDIATE_UPDATE_REQUEST_CODE = 1000

/**
 * The base activity for activities where app update has to be checked
 * based on sample: https://github.com/malvinstn/FakeAppUpdateManagerSample
 */
abstract class AppUpdateActivity : BaseActivity() {

    @Inject
    lateinit var appUpdateManager: AppUpdateManager

    @Inject
    lateinit var playServiceExecutor: Executor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)

        appUpdateManager.registerListener(listener)

        if (isAppUpdateCheckRequired()) {
            checkInAppUpdate()
        }
    }

    override fun onResume() {
        super.onResume()

        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener(playServiceExecutor, { appUpdateInfo ->
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    popupSnackbarForCompleteUpdate()
                }
            })
    }


    private val listener = { state: InstallState ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            popupSnackbarForCompleteUpdate()
        } else if (state.installStatus() == InstallStatus.FAILED) {
            popupSnackbarForRetryUpdate()
        }
    }

    private fun checkInAppUpdate() {
        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener(playServiceExecutor, { appUpdateInfo ->
                when (appUpdateInfo.updateAvailability()) {
                    UpdateAvailability.UPDATE_AVAILABLE -> when {
                        appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> startImmediateUpdate(
                            appUpdateInfo
                        )
                        else -> {
                            // No update is allowed
                        }
                    }
                    else -> {
                        // No op
                    }
                }
            })
    }

    private fun startImmediateUpdate(appUpdateInfo: AppUpdateInfo?) {
        appUpdateManager.startUpdateFlowForResult(
            appUpdateInfo!!,
            AppUpdateType.IMMEDIATE,
            this,
            IMMEDIATE_UPDATE_REQUEST_CODE
        )

        // saveLastAppUpdateCheck()
    }

    private fun popupSnackbarForCompleteUpdate() {
        Snackbar.make(
            findViewById(R.id.mainLayout),
            R.string.update_ready,
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction(R.string.restart) { appUpdateManager.completeUpdate() }
            val textView: TextView = view.findViewById(R.id.snackbar_text)
            textView.setTextColor(Color.WHITE)
            show()
        }
    }

    private fun popupSnackbarForRetryUpdate() {
        Snackbar.make(
            findViewById(R.id.mainLayout),
            R.string.update_download_fail,
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction(R.string.retry) { checkInAppUpdate() }
            val textView: TextView = view.findViewById(R.id.snackbar_text)
            textView.setTextColor(Color.WHITE)
            show()
        }
    }

    override fun onDestroy() {
        appUpdateManager.unregisterListener(listener)
        super.onDestroy()
    }
}