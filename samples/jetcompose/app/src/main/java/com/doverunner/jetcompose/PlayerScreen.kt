package com.doverunner.jetcompose

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.doverunner.widevine.exception.WvException
import com.doverunner.widevine.exception.WvLicenseServerException
import com.doverunner.widevine.model.ContentData
import com.doverunner.widevine.model.WvEventListener
import com.doverunner.widevine.sdk.DrWvSDK
import com.doverunner.widevine.model.ContentData as DrContentData

@Composable
fun PlayerScreen(drContentData: DrContentData, navController: NavController) {
    val context = LocalContext.current
    val mediaSource: MediaSource? = remember {
        getMediaSourceUsingSDK(context, drContentData)
    }

    Surface(
        content = {
            mediaSource?.let {
                VideoPlayer(mediaSource = it)
            }
        }
    )
}

@Composable
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
fun VideoPlayer(mediaSource: MediaSource) {
    val context = LocalContext.current
    val playbackPosition = rememberSaveable {
        mutableStateOf(0L)
    }

    val exoPlayer = remember(context, mediaSource) {
        ExoPlayer.Builder(context).build().apply {
            addMediaSource(mediaSource)
            addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    var message = error.message
                    if (error.errorCode == PlaybackException.ERROR_CODE_DRM_LICENSE_EXPIRED) {
                        message = "License Expired"
                    }

                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                }

                override fun onEvents(player: Player, events: Player.Events) {
                    super.onEvents(player, events)
                    if (events.size() > 0 &&
                        events[0] == Player.EVENT_SURFACE_SIZE_CHANGED) {
                        playbackPosition.value = currentPosition
                    }
                }
            })

            seekTo(playbackPosition.value)

            prepare()
            play()
        }
    }

    DisposableEffect(exoPlayer) { onDispose { exoPlayer.release() } }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            PlayerView(context).apply {
                player = exoPlayer
                useController = true
            }
        }
    )
}

fun getMediaSourceUsingSDK(context: Context, drContentData: DrContentData): MediaSource? {
    val wvmAgent = DrWvSDK.createWvSDK(context, drContentData)

    val wvEventListener: WvEventListener = object : WvEventListener {
        override fun onCompleted(contentData: ContentData) {

        }

        override fun onProgress(contentData: ContentData, percent: Float, downloadedBytes: Long) {

        }

        override fun onStopped(contentData: ContentData) {

        }

        override fun onRestarting(contentData: ContentData) {

        }

        override fun onRemoved(contentData: ContentData) {

        }

        override fun onPaused(contentData: ContentData) {

        }

        override fun onFailed(contentData: ContentData, e: WvException?) {
            if (e is WvException.WvLicenseCipherException) {
                // Ignore the error except when using the LicenseCipher function.
            } else {
                Toast.makeText(context, e?.msg ?: "DrException", Toast.LENGTH_LONG).show()
            }
        }

        override fun onFailed(contentData: ContentData, e: WvLicenseServerException?) {
            val message = "${e?.errorCode()}, ${e?.body()}"
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
    DrWvSDK.addWvEventListener(wvEventListener)
    return try {
        wvmAgent.getMediaSource()
    } catch (e: WvException.ContentDataException) {
        e.printStackTrace()
        Toast.makeText(context, e?.message, Toast.LENGTH_LONG).show()
        null
    } catch (e: WvException.DetectedDeviceTimeModifiedException) {
        e.printStackTrace()
        Toast.makeText(context, e?.message, Toast.LENGTH_LONG).show()
        null
    }
}