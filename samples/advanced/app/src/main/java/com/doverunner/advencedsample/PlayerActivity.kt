package com.doverunner.advencedsample

import android.os.Build
import android.os.Bundle
import android.view.SurfaceView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import com.doverunner.widevine.sdk.DrWvSDK

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private var playerView: PlayerView? = null
    private var exoPlayer: ExoPlayer? = null
    private var wvSDK: DrWvSDK? = null

    companion object {
        const val CONTENT = "CONTENT_ITEM"
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
        try {
            // Step 1: get ContentData from Intent
            val content = getContentFromIntent() ?: return showError("No content data")

            // Step 2: DRM SDK initialization
            wvSDK = DrWvSDK.createWvSDK(this, content)

            // Step 3: get MediaSource
            val mediaSource = wvSDK?.getMediaSource() ?: return showError("No media source")

            // Step 4: check license validity
            checkLicense()

            // Step 5: player initialization and start playback
            createAndStartPlayer(mediaSource)

            wvSDK?.setPlayer(exoPlayer as Player)
        } catch (e: WvException.DrmException) {
            showError("DrmException")
        } catch (e: WvException.DetectedDeviceTimeModifiedException) {
            showError("DeviceTimeModified")
        } catch (e: Exception) {
            showError("Failed initialize: ${e.message}")
        }
    }

    private fun getContentFromIntent(): ContentData? {
        if (!intent.hasExtra(CONTENT)) return null

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(CONTENT, ContentData::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(CONTENT)
        }
    }

    private fun checkLicense() {
        val drmInfo = wvSDK?.getDrmInformation()
        val isDownloadCompleted = wvSDK?.getDownloadState() == DownloadState.COMPLETED

        if (isDownloadCompleted && drmInfo != null) {
            val isExpired = drmInfo.licenseDuration <= 0 ||
                    drmInfo.playbackDuration <= 0

            if (isExpired) {
                showError("Expired license")
            }
        }
    }

    private fun createAndStartPlayer(mediaSource: MediaSource) {
        // using mediaItem
        /*
         ExoPlayer.Builer(this).setMediaSourceFactory(
                DefaultMediaSourceFactory(this)
                setDataSourceFactory(wvSDK!!.getDataSourceFactory())).build()
        */
        exoPlayer = ExoPlayer.Builder(this).build().apply {
            binding.exoplayerView.player = this
            setMediaSource(mediaSource)
            prepare()
            playWhenReady = true

            // 간단한 에러 처리
            addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    if (error.errorCode ==
                            PlaybackException.ERROR_CODE_DRM_LICENSE_EXPIRED) {
                        showError("License Expired")
                    } else {
                        showError("Player Error: ${error.message}")
                    }
                }
            })
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
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