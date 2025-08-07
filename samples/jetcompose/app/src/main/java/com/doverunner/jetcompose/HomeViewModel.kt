package com.doverunner.jetcompose

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.annotation.DoNotInline
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.doverunner.widevine.exception.WvException
import com.doverunner.widevine.exception.WvLicenseServerException
import com.doverunner.widevine.model.DownloadState
import com.doverunner.widevine.model.DrmInformation
import com.doverunner.widevine.model.WvEventListener
import com.doverunner.widevine.sdk.DrWvSDK
import com.doverunner.widevine.track.DownloaderTracks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {
    var context: Context? = null
    val scope = CoroutineScope(Dispatchers.Main)

    private val _contents = MutableStateFlow(ObjectSingleton.getInstance().getContentDatas())
    val contents: StateFlow<List<ContentData>> = _contents.asStateFlow()
    val notificationPermissionToastShown = MutableStateFlow(false)

    private val wvEventListener: WvEventListener = object : WvEventListener {
        override fun onCompleted(contentData: com.doverunner.widevine.model.ContentData) {
            updateContentDataFromListners(contentData.url, "COMPLETED", DownloadState.COMPLETED)
        }

        override fun onProgress(
            contentData: com.doverunner.widevine.model.ContentData,
            percent: Float,
            downloadedBytes: Long
        ) {
            val copyList = contents.value.toMutableList()
            val index = copyList.indexOfFirst { it.content == contentData }
            if (index != -1) {
                var status = copyList[index].status
                if (copyList[index].status != DownloadState.COMPLETED) {
                    status = DownloadState.DOWNLOADING
                }
                copyList[index] = copyList[index].copy(
                    subTitle = "Downloading.. %" + String.format("%.0f", percent),
                    status = status
                )
                modifyContentDataList(copyList)
            }
        }

        override fun onStopped(contentData: com.doverunner.widevine.model.ContentData) {
            updateContentDataFromListners(contentData.url, "Stoped", DownloadState.STOPPED)
        }

        override fun onRestarting(contentData: com.doverunner.widevine.model.ContentData) {
            updateContentDataFromListners(contentData.url, "Restart", DownloadState.RESTARTING)
        }

        override fun onRemoved(contentData: com.doverunner.widevine.model.ContentData) {
            updateContentDataFromListners(contentData.url, "Not", DownloadState.NOT)
        }

        override fun onPaused(contentData: com.doverunner.widevine.model.ContentData) {
            val copyList = contents.value.toMutableList()
            copyList.forEachIndexed { index, contentData ->
                var state = copyList[index].wvSDK.getDownloadState()
                if (state == DownloadState.DOWNLOADING) {
                    copyList[index] = copyList[index].copy(
                        subTitle = "Paused",
                        status = DownloadState.PAUSED
                    )
                }
            }
            modifyContentDataList(copyList)
        }

        override fun onFailed(
            contentData: com.doverunner.widevine.model.ContentData,
            e: WvException?
        ) {
            var subTitle: String
            when (e) {
                is WvException.DrmException -> {
                    subTitle = "Drm Error"
                }
                is WvException.DownloadException -> {
                    subTitle = "Download Error"
                }
                is WvException.DetectedDeviceTimeModifiedException -> {
                    subTitle = "Device time modified Error"
                }
                is WvException.NetworkConnectedException -> {
                    subTitle = "Network Error"
                }
                is WvException.WvLicenseCipherException -> {
                    // Ignore the error except when using the LicenseCipher function.
                    return
                }
                else -> {
                    subTitle = "Failed"
                }
            }
            updateContentDataFromListners(contentData.url, subTitle, DownloadState.FAILED)

            e?.let { e ->
                scope.launch(Dispatchers.Main) {
                    context?.let { context ->
                        Toast.makeText(context, "${e.msg}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        override fun onFailed(
            contentData: com.doverunner.widevine.model.ContentData,
            e: WvLicenseServerException?
        ) {
            updateContentDataFromListners(contentData.url, "Failed", DownloadState.FAILED)

            if (e != null && e.errorCode() != 7127) {
                scope.launch(Dispatchers.Main) {
                    context?.let { context ->
                        Toast.makeText(context, "Server Error - ${e!!.errorCode()}, ${e!!.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun initialize(context: Context) {
        this.context = context
        if (ObjectSingleton.getInstance().contents.isEmpty()) {
            DrWvSDK.addWvEventListener(wvEventListener)
            ObjectSingleton.getInstance().createContents(context)
        }
        modifyContentDataList(ObjectSingleton.getInstance().getContentDatas())
    }

    fun prepare() {
        for (i in 0 until (contents.value.size)) {
            prepareForIndex(i)
        }
    }

    private fun migrateForContentData(contentData: ContentData) {
        try {
            val sdk = contentData.wvSDK
            if (sdk.needsMigrateDownloadedContent()
            ) {
                val isSuccess = contentData.wvSDK.migrateDownloadedContent(
                    contentName = "", // content's name which will be used for the name of downloaded content's folder
                    downloadedFolderName = null // content download folder name
                )

                if (!isSuccess) {
                    print("failed migrate downloaded content")
                }
            }
        } catch (e: WvException.ContentDataException) {
            print(e)
        } catch (e: WvException.MigrationException) {
            print(e)
        } catch (e: WvException.MigrationLocalPathException) {
            // you have to change localPath
            // ex) val localPath = File(fi, "downloads_v2").toString()
            print(e)
        }
    }

    private fun prepareForIndex(index: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            // migration is required in advance.
            val copyList = contents.value.toMutableList()
            migrateForContentData(copyList[index])

            val state = copyList[index].wvSDK.getDownloadState()
            if (state != DownloadState.COMPLETED) {
                copyList[index].wvSDK.getContentTrackInfo({ tracks ->
                    updateContentData(index, copyList[index].copy(
                        downloadTracks = tracks,
                        status = state,
                        subTitle =  state.toString()
                    ))
                }, { e ->
                    e.printStackTrace()
                })
            } else {
                copyList[index] = copyList[index].copy(
                    status = state,
                    subTitle =  state.toString()
                )
                updateContentData(index, copyList[index].copy(
                    status = state,
                    subTitle =  state.toString()
                ))
            }
        }
    }

    fun remove(contentData: ContentData) {
        contentData.wvSDK.remove()
        val index = contents.value.indexOf(contentData)
        prepareForIndex(index)
    }

    fun getDrmInformation(contentData: ContentData): DrmInformation {
        return try {
            contentData.wvSDK.getDrmInformation()
        } catch (e: WvException.DrmException) {
            DrmInformation(0, 0)
        }
    }

    fun pauseAll() {
        contents.value.first().wvSDK.pauseAll()
    }

    fun resumeAll() {
        contents.value.first().wvSDK.resumeAll()
    }

    fun download(contentData: ContentData, track: DownloaderTracks) {
        try {
            contentData.wvSDK.download(track)
        } catch (e: WvException.ContentDataException) {
            print(e)
        } catch (e: WvException.DownloadException) {
            print(e)
        }
    }

    fun modifyContentDataList(newData: List<ContentData>) {
        _contents.value = newData
    }

    fun updateContentData(index: Int, contentData: ContentData) {
        val copyList = _contents.value.toMutableList()
        copyList[index] = contentData
        modifyContentDataList(copyList)
    }

    fun updateContentDataFromListners(url: String?, subTitle: String, status: DownloadState) {
        val copyList = contents.value.toMutableList()
        val index = copyList.indexOfFirst { it.content.url == url }
        if (index != -1) {
            copyList[index] = copyList[index].copy(
                subTitle = subTitle,
                status = status
            )
        }
        modifyContentDataList(copyList)
    }

    companion object {
        private const val POST_NOTIFICATION_PERMISSION_REQUEST_CODE = 100

        @RequiresApi(33)
        object Api33 {
            @get:DoNotInline
            val postNotificationPermissionString: String
                get() = Manifest.permission.POST_NOTIFICATIONS
        }
    }
}