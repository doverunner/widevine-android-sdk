package com.doverunner.jetcompose

import com.doverunner.widevine.model.DownloadState
import com.doverunner.widevine.sdk.DrWvSDK
import com.doverunner.widevine.track.DownloaderTracks
import com.doverunner.widevine.model.ContentData as DrContentData

data class ContentData(
    val title : String,
    var status : DownloadState,
    var subTitle: String,
    val content : DrContentData,
    val wvSDK: DrWvSDK,
    var downloadTracks: DownloaderTracks?,
    val cid: String, // for migration
    val name: String // for migration
)
