package com.doverunner.advencedsample

import android.app.Notification
import android.content.Context
import androidx.media3.common.util.NotificationUtil
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.scheduler.PlatformScheduler
import androidx.media3.exoplayer.scheduler.Scheduler
import com.doverunner.widevine.R
import com.doverunner.widevine.service.DrmDownloadService

class DemoDownloadService constructor(
    private val JOB_ID: Int = 1,
    private val FOREGROUND_NOTIFICATION_ID: Int = 1,
) : DrmDownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    ObjectSingleton.getInstance().downloadChannel,
    R.string.exo_download_notification_channel_name,
    0
) {

    override fun onDestroy() {
        clearDownloadManagerHelpers()
        super.onDestroy()
    }

    override fun getDownloadManager(): DownloadManager {
        val manager = ObjectSingleton.getInstance().getDownloadManager()
        val downloadNotificationHelper = ObjectSingleton.getInstance().getDownloadNotificationHelper()

        manager.addListener(
            TerminalStateNotificationHelper(
                this,
                downloadNotificationHelper,
                FOREGROUND_NOTIFICATION_ID + 1
            )
        )
        return manager
    }

    override fun getScheduler(): Scheduler? {
        return if (Util.SDK_INT >= 21) PlatformScheduler(this, JOB_ID) else null
    }

    override fun getForegroundNotification(
        downloads: List<Download>,
        notMetRequirements: Int,
    ): Notification {
        val downloadNotificationHelper = ObjectSingleton.getInstance().getDownloadNotificationHelper()

        return downloadNotificationHelper
            .buildProgressNotification(
                this,
                R.drawable.dr_ic_baseline_arrow_downward_24,
                null,  /* message= */
                null,
                downloads,
                notMetRequirements
            )
    }

    private class TerminalStateNotificationHelper(
        context: Context, notificationHelper: DownloadNotificationHelper, firstNotificationId: Int,
    ) :
        DownloadManager.Listener {
        private val context: Context
        private val notificationHelper: DownloadNotificationHelper
        private var nextNotificationId: Int
        override fun onDownloadChanged(
            downloadManager: DownloadManager, download: Download, finalException: Exception?,
        ) {
            val notification = when (download.state) {
                Download.STATE_COMPLETED -> {
                    notificationHelper.buildDownloadCompletedNotification(
                        context,
                        R.drawable.dr_ic_baseline_done_24,  /* contentIntent= */
                        null,
                        Util.fromUtf8Bytes(download.request.data)
                    )
                }
                Download.STATE_FAILED -> {
                    notificationHelper.buildDownloadFailedNotification(
                        context,
                        R.drawable.dr_ic_baseline_done_24,  /* contentIntent= */
                        null,
                        Util.fromUtf8Bytes(download.request.data)
                    )
                }
                else -> {
                    return
                }
            }
            NotificationUtil.setNotification(context, nextNotificationId++, notification)
        }

        init {
            this.context = context.applicationContext
            this.notificationHelper = notificationHelper
            nextNotificationId = firstNotificationId
        }
    }
}