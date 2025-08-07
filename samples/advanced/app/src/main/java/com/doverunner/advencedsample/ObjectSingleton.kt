package com.doverunner.advencedsample

import android.content.Context
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.upstream.CmcdConfiguration
import com.doverunner.widevine.model.DownloadState
import com.doverunner.widevine.model.DrmConfigration
import com.doverunner.widevine.sdk.DrWvSDK
import java.io.File

class ObjectSingleton {
    val contents = mutableListOf<ContentData>()
    val downloadChannel = "download_channel"
    var context: Context? = null

    companion object {
        private var instance: ObjectSingleton? = null

        fun getInstance(): ObjectSingleton {
            return instance ?: synchronized(this) {
                instance ?: ObjectSingleton().also {
                    instance = it
                }
            }
        }

        fun release() {
            instance = null
        }
    }

    fun updateContentData(index: Int, subTitle: String, status: DownloadState) {
        contents[index].subTitle = subTitle
        contents[index].status = status
    }

    fun createContents(context: Context) {
        this.context = context

        val fi = context.getExternalFilesDir(null) ?: context.filesDir
        val localPath = File(fi, "downloads").toString()
        DrWvSDK.setDownloadDirectory(context, localPath)
        DrWvSDK.setDownloadService(DemoDownloadService::class.java)
        DrWvSDK.setCmcdConfigurationFactory(CmcdConfiguration.Factory.DEFAULT)

        val config = DrmConfigration(
            "DEMO",
            "eyJkcm1fdHlwZSI6IldpZGV2aW5lIiwic2l0ZV9pZCI6IkRFTU8iLCJ1c2VyX2lkIjoidGVzdFVzZXIiLCJjaWQiOiJkZW1vLWJiYi1zaW1wbGUiLCJwb2xpY3kiOiI5V3FJV2tkaHB4VkdLOFBTSVljbkp1dUNXTmlOK240S1ZqaTNpcEhIcDlFcTdITk9uYlh6QS9pdTdSa0Vwbk85c0YrSjR6R000ZkdCMzVnTGVORGNHYWdPY1Q4Ykh5c3k0ZHhSY2hYV2tUcDVLdXFlT0ljVFFzM2E3VXBnVVdTUCIsInJlc3BvbnNlX2Zvcm1hdCI6Im9yaWdpbmFsIiwia2V5X3JvdGF0aW9uIjpmYWxzZSwidGltZXN0YW1wIjoiMjAyMi0wOS0xOVQwNzo0Mjo0MFoiLCJoYXNoIjoiNDBDb1RuNEpFTnpZUHZrT1lTMHkvK2VIN1dHK0ZidUIvcThtR3VoaHVNRT0ifQ=="
        )
        val data = com.doverunner.widevine.model.ContentData(
            "demo-bbb-simple",
            "https://drm-contents.doverunner.com/DEMO/app/big_buck_bunny/dash/stream.mpd",
            config
        )
        val wvSDK = DrWvSDK.createWvSDK(
            context,
            data
        )

        val state = wvSDK.getDownloadState()
        contents.add(
            ContentData(
                "basic content",
                state,
                state.toString(),
                data,
                wvSDK,
                null,
                "demo-bbb-simple",
                "demo-bbb-simple"
            )
        )

        val config2 = DrmConfigration(
            "DEMO",
            "eyJrZXlfcm90YXRpb24iOmZhbHNlLCJyZXNwb25zZV9mb3JtYXQiOiJvcmlnaW5hbCIsInVzZXJfaWQiOiJwYWxseWNvbiIsImRybV90eXBlIjoid2lkZXZpbmUiLCJzaXRlX2lkIjoiREVNTyIsImhhc2giOiJkNTBDSVVUS1RwRDl6T3dGaU9DSysrXC83Q3pLOStZN3NkcHFhUUppdDJWQT0iLCJjaWQiOiJUZXN0UnVubmVyIiwicG9saWN5IjoiOVdxSVdrZGhweFZHSzhQU0lZY25Kc2N2dUE5c3hndWJMc2QrYWp1XC9ib21RWlBicUkreGFlWWZRb2Nja3Z1RWZBYXFkVzVoWGdKTmdjU1MzZlM3bzhNczB3QXNuN05UbmJIUmtwWDFDeTEyTkhwMlZPN1pMeFJvZDhVdkUwZnBFbUpYOUpuRDh6ZktkdE9RWk9UYXljK280RzNCT0xmU29OaFpWbkIwUGxEbW1rVk5jbXpndko2YloxdXBudjFcLzJFM2lXZXd3eklTNFVOQlhTS21zVUFCZnBRQjg4Q2VJYlZSM0hKZWJvcEpwZG1DTFFvRmtCT09DQU9qWElBOUVHIiwidGltZXN0YW1wIjoiMjAyMi0xMC0xMVQwNzowMToxN1oifQ=="
        )
        val data2 = com.doverunner.widevine.model.ContentData(
            contentId = "TestRunner_User",
            url = "https://drm-contents.doverunner.com/TEST/PACKAGED_CONTENT/TEST_SIMPLE/dash/stream.mpd",
            drmConfig = config2
        )
        val wvSDK2 = DrWvSDK.createWvSDK(
            context,
            data2
        )

        val state2 = wvSDK2.getDownloadState()
        contents.add(
            ContentData(
                "short duration content",
                state2,
                state2.toString(),
                data2,
                wvSDK2,
                null,
                "TestRunner_User1",
                "TestRunner_User1"
            )
        )

        val config3 = DrmConfigration(
            "DEMO",
            "eyJrZXlfcm90YXRpb24iOmZhbHNlLCJyZXNwb25zZV9mb3JtYXQiOiJvcmlnaW5hbCIsInVzZXJfaWQiOiJUZXN0UnVubmVyIiwiZHJtX3R5cGUiOiJ3aWRldmluZSIsInNpdGVfaWQiOiJERU1PIiwiaGFzaCI6IjdqcjNOb2w4N1l1U29hNlk2RXJCMFVoMkNIM1pWR2VBUVNtMTh5YkZheFU9IiwiY2lkIjoiVGVzdFJ1bm5lciIsInBvbGljeSI6IjlXcUlXa2RocHhWR0s4UFNJWWNuSnNjdnVBOXN4Z3ViTHNkK2FqdVwvYm9tUVpQYnFJK3hhZVlmUW9jY2t2dUVmQWFxZFc1aFhnSk5nY1NTM2ZTN284TnNqd3N6ak11dnQrMFF6TGtaVlZObXgwa2VmT2Uyd0NzMlRJVGdkVTRCdk45YWJoZDByUWtNSXJtb0llb0pIcUllSGNSdlZmNlQxNFJtVEFERXBDWTdQSGZQZlwvVkZZXC9WYlh1eFhcL1dUdFZTRkpTSDlzeHB3UUlRWHI1QjZSK0FhYWZTZlZYU0trNG1WRmxlMlBcL3Byamg1OCtiT2hidFU0NDRseDlvcmVHNSIsInRpbWVzdGFtcCI6IjIwMjMtMDUtMTlUMDI6MTM6NTlaIn0="
        )
        val data3 = com.doverunner.widevine.model.ContentData(
            contentId = "TestRunner_HLS",
            url = "https://drm-contents.doverunner.com/TEST/PACKAGED_CONTENT/TEST_SIMPLE/cmaf/master.m3u8",
            drmConfig = config3
        )
        val wvSDK3 = DrWvSDK.createWvSDK(
            context,
            data3
        )
        val state3 = wvSDK3.getDownloadState()
        contents.add(
            ContentData(
                "hls",
                state3,
                state3.toString(),
                data3,
                wvSDK3,
                null,
                "TestRunner",
                "TestRunner"
            )
        )

        val config4 = DrmConfigration(
            siteId = "DEMO",
            token = "eyJrZXlfcm90YXRpb24iOmZhbHNlLCJyZXNwb25zZV9mb3JtYXQiOiJvcmlnaW5hbCIsInVzZXJfaWQiOiJ0ZXN0VXNlciIsImRybV90eXBlIjoid2lkZXZpbmUiLCJzaXRlX2lkIjoiREVNTyIsImhhc2giOiJjQlZFT0RWalI3UjN2cDJzc3VXUVwvc05Pb3RvQWJKZEttcExcLzNlc1k1bmc9IiwiY2lkIjoiZGVtby1iYmItc2ltcGxlIiwicG9saWN5IjoiOVdxSVdrZGhweFZHSzhQU0lZY25Kc2N2dUE5c3hndWJMc2QrYWp1XC9ib2x5QTB6THVJT0VFNll4MlRiWVdCTG9OaGFjU0ViN0F4c0FVQ3JBQ3VTOFVUSzVIaGo1QXZ4VHhUREhsdFRcL0lvdTJ6XC95QUZCZE5CbDVkK1ByYWxXc2djNlRocEhsRFNHWFYwYWx5cG5iR2UyYkZuQ1wvRGZpbHlUTHVFcFdKZXNMdXd1R05RNHhOVndNT0o1WWxcL3pOc1FkMWY1YyttTWFYRXdleEZrUXRVUG5mdUk2WFdDVlBHWjZcL1NoZFBtbzlWMWRubVhoSktVZjhrOWFJOTdzVUV6VVwvSjJUYVpTYVU1M2FlN2lsc0NkekNIMG1PODVXS2ttYWI0Y2tBN295TjI3V2VjQ21Hck5DdHcrOTA4Zm85c2g0RE91NTJabXRZajJNZTRQaUNnS0lqOVwvdkN4TTdOVDdocGh1WTVHa0pQS2xrZXJlcVNTdmNiQnZWR0tcL0J3czdDUGg3RWptSDdoXC9LZUFFRHpTZlwvalBDQVJEcVd1dWg2bGRFRGQxN05XbnNsRk5EcFFZKzBpdGhER0dxQnJjd0IxcTQyNVVRUzROWG1zSVQ4cU9uRlMrUWtQSU9UbVJkVCtRVmNLV2p0Zjg1TT0iLCJ0aW1lc3RhbXAiOiIyMDI1LTAzLTEyVDAwOjU3OjE5WiJ9",
            licenseCipherPath = "plc-kt-DEMO.bin"
        )
        val data4 = com.doverunner.widevine.model.ContentData(
            contentId = "LicenseCipher",
            url = "https://drm-contents.doverunner.com/DEMO/app/big_buck_bunny/dash/stream.mpd",
            drmConfig = config4
        )
        val wvSDK4 = DrWvSDK.createWvSDK(
            context,
            data4
        )
        val state4 = wvSDK4.getDownloadState()
        contents.add(
            ContentData(
                "licensecipher",
                state4,
                state4.toString(),
                data4,
                wvSDK4,
                null,
                "licensecipher",
                "licensecipher"
            )
        )
    }

    fun getDownloadNotificationHelper(): DownloadNotificationHelper {
        return DownloadNotificationHelper(this.context!!, downloadChannel)
    }

    fun getDownloadManager(): DownloadManager {
        return DrWvSDK.getDownloadManager(this.context!!)
    }
}