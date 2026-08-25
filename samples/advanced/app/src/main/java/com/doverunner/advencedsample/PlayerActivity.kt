package com.doverunner.advencedsample

import android.os.Build
import android.os.Bundle
import android.view.SurfaceView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.ui.PlayerView
import com.doverunner.advencedsample.databinding.ActivityPlayerBinding
import com.doverunner.widevine.exception.WvException
import com.doverunner.widevine.model.ContentData
import com.doverunner.widevine.model.DownloadState
import com.doverunner.widevine.model.PlaybackOptions
import com.doverunner.widevine.sdk.DrWvSDK

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private var playerView: PlayerView? = null
    private var exoPlayer: ExoPlayer? = null
    private var wvSDK: DrWvSDK? = null

    companion object {
        const val CONTENT = "CONTENT_ITEM"
        const val FORCE_STREAMING = "FORCE_STREAMING"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO : Set Sercurity API to protect media recording by screen recorder
        val view = binding.exoplayerView.videoSurfaceView as SurfaceView
        playerView = binding.exoplayerView
        playerView?.setShowSubtitleButton(true);
        if (Build.VERSION.SDK_INT >= 17) {
            view.setSecure(true)
        }
    }

    private fun initializePlayer() {
        var content: ContentData? = null
        var mediaSource: MediaSource? = null

        // Playback policy the app carries itself. The SDK keeps no state for it: it is passed at
        // the getMediaSource() / getMediaItem() call, so the same DrWvSDK instance can hand out an
        // offline MediaSource and a streaming one without being released and recreated.
        val forceStreaming = intent.getBooleanExtra(FORCE_STREAMING, false)

        try {
            if (intent.hasExtra(CONTENT) && wvSDK == null) {
                content = intent.getParcelableExtra(CONTENT)
                if (content != null && content!!.url != null) {
                    wvSDK = DrWvSDK.createWvSDK(
                        this,
                        content!!
                    )
                }
            }

            val playbackOptions = PlaybackOptions.Builder()
                .setForceStreaming(forceStreaming)
                .build()

            wvSDK?.getMediaSource(playbackOptions)?.let { media ->
                mediaSource = media

                // Offline license check. Only meaningful when this playback actually uses the
                // downloaded offline license. With forceStreaming the license comes fresh from the
                // license server, and getDrmInformation() would still report the (untouched,
                // possibly expired) offline license sitting on disk -- reporting it here would be
                // a false alarm.
                if (!forceStreaming) {
                    val drmConfiguration = media.mediaItem.localConfiguration?.drmConfiguration
                    if (drmConfiguration != null &&
                        drmConfiguration.scheme != C.CLEARKEY_UUID) {
                        wvSDK?.getDrmInformation()?.let {
                            if ((it.licenseDuration <= 0 || it.playbackDuration <= 0) &&
                                wvSDK?.getDownloadState() == DownloadState.COMPLETED) {
                                Toast.makeText(
                                    applicationContext,
                                    "Expired license",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
            }
        } catch (e: WvException.DrmException) {
            print(e)
            Toast.makeText(applicationContext, "DrmException", Toast.LENGTH_LONG)
                .show()
        } catch (e: WvException.DetectedDeviceTimeModifiedException) {
            print(e)
            Toast.makeText(applicationContext, "DeviceTimeModified", Toast.LENGTH_LONG)
                .show()
        } catch (e: Exception) {
            print(e)
            Toast.makeText(applicationContext, "Exception", Toast.LENGTH_LONG)
                .show()
        }

        // Alternative: using getMediaItem() instead of getMediaSource().
        //
        // getMediaSource() hands back a fully wired MediaSource. getMediaItem() only returns the
        // MediaItem out of it, so ExoPlayer rebuilds the MediaSource with its own defaults and the
        // wiring below is NOT optional -- you have to supply it yourself:
        //
        //   - setDataSourceFactory      : without it, downloaded (offline) content cannot be read
        //   - setDrmSessionManagerProvider : without it, ExoPlayer uses its own default DRM callback,
        //                                    so License Cipher and the license-server error callbacks
        //                                    (WvLicenseServerException -> onFailed) are silently lost
        //
        // Pass the same PlaybackOptions to getMediaItem(), getDataSourceFactory() and
        // getDrmSessionManager() so all three agree on offline vs streaming: with
        // forceStreaming=false downloaded content plays from the download cache and decrypts with
        // the downloaded offline license (works with no network), with forceStreaming=true the
        // content streams from the remote URL and a fresh license is requested from the server.
        //
        // import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
        /*
        val playbackOptions = PlaybackOptions.Builder()
            .setForceStreaming(forceStreaming)
            .build()

        // getMediaItem() throws the same exceptions as getMediaSource() (e.g.
        // WvException.DetectedDeviceTimeModifiedException), so keep the SDK calls inside
        // try/catch just like the getMediaSource() path above.
        try {
            val mediaItem = wvSDK!!.getMediaItem(playbackOptions)

            ExoPlayer.Builder(this)
                .setMediaSourceFactory(
                    DefaultMediaSourceFactory(this)
                        .setDataSourceFactory(wvSDK!!.getDataSourceFactory(playbackOptions))
                        .setDrmSessionManagerProvider { wvSDK!!.getDrmSessionManager(playbackOptions) }
                )
                .build()
                .also { player ->
                    wvSDK?.setPlayer(player)
                    player.setMediaItem(mediaItem)
                    player.prepare()
                    player.playWhenReady = true
                }
        } catch (e: WvException.DrmException) {
            Toast.makeText(applicationContext, "DrmException", Toast.LENGTH_LONG).show()
        } catch (e: WvException.DetectedDeviceTimeModifiedException) {
            Toast.makeText(applicationContext, "DeviceTimeModified", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(applicationContext, "Exception", Toast.LENGTH_LONG).show()
        }
        */

        if (mediaSource == null) {
            return
        }

        ExoPlayer.Builder(this)
//            .setRenderersFactory(DefaultRenderersFactory(this)
//                .setEnableDecoderFallback(true))
            .build()
            .also { player ->
                exoPlayer = player
                binding.exoplayerView.player = player
//                exoPlayer?.setVideoSurfaceView(binding.surfaceView)
//                exoPlayer?.setVideoSurface(binding.surfaceView.holder.surface)
                wvSDK?.setPlayer(player)
                exoPlayer?.setMediaSource(mediaSource!!)
                exoPlayer?.prepare()
                exoPlayer?.playWhenReady = true
                exoPlayer?.addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        super.onPlayerError(error)
                        if (error.errorCode ==
                            PlaybackException.ERROR_CODE_DRM_LICENSE_EXPIRED) {
                            Toast.makeText(applicationContext, "License Expired", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        super.onIsPlayingChanged(isPlaying)
                        if (isPlaying && exoPlayer != null) {
//                            viewModel.setDuration(exoPlayer!!.duration)
                        }
                    }
                })
            }
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 24 || exoPlayer != null) {
            exoPlayer?.playWhenReady = true
        }
    }

    override fun onResume() {
        super.onResume()
        exoPlayer?.playWhenReady = true
        initializePlayer()
    }

    override fun onStop() {
        super.onStop()
        exoPlayer?.playWhenReady = false
        if (isFinishing) {
            releasePlayer()
        }
    }

    private fun releasePlayer() {
        exoPlayer?.release()
    }
}