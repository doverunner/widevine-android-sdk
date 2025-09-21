package com.doverunner.castsample

import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.SurfaceView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastStateListener
import com.google.common.util.concurrent.MoreExecutors
import com.doverunner.castsample.databinding.ActivityPlayerBinding
import com.doverunner.widevine.exception.WvException
import com.doverunner.widevine.model.ContentData
import com.doverunner.widevine.model.DownloadState
import com.doverunner.widevine.sdk.DrWvSDK

@UnstableApi
class PlayerActivity : CastStateListener, AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private var playerView: PlayerView? = null
    private var castContext: CastContext? = null
    private var wvSDK: DrWvSDK? = null
    private var playerManager: PlayerManager? = null

    companion object {
        const val CONTENT = "CONTENT_ITEM"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CastContext.getSharedInstance(this, MoreExecutors.directExecutor())
            .addOnCompleteListener {
            castContext = it.result
            castContext?.addCastStateListener(this)
            buildSample()
        }

        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO : Set Sercurity API to protect media recording by screen recorder
        val view = binding.exoplayerView.videoSurfaceView as SurfaceView
        playerView = binding.exoplayerView
        if (Build.VERSION.SDK_INT >= 17) {
            view.setSecure(true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu, menu)
        CastButtonFactory.setUpMediaRouteButton(this, menu, R.id.media_route_menu_item)
        return true
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return super.dispatchKeyEvent(event) || playerManager?.dispatchKeyEvent(event) == true
    }

    private fun buildSample() {

        if (intent.hasExtra(CONTENT) && wvSDK == null) {
            val content: ContentData? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13 (TIRAMISU) 이상에서는 Class 타입을 명시합니다.
                intent.getParcelableExtra(CONTENT, ContentData::class.java)
            } else {
                // 이전 버전에서는 기존 deprecated 메서드를 사용하고 경고를 억제합니다.
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(CONTENT)
            }

            // content가 null이 아니고 content.url도 null이 아닌 경우 DrWvSDK를 초기화합니다.
            // content!! 대신 null 체크 후 안전하게 접근하는 것이 좋습니다.
            if (content != null && content.url != null) {
                // wvSDK 변수에 DrWvSDK 객체 할당
                wvSDK = DrWvSDK.createWvSDK(
                    this,
                    content
                )
            }
        }

        try {
            playerManager?.createPlayers(castContext, wvSDK!!)
            val drmInfo = wvSDK?.getDrmInformation()
            drmInfo?.let {
                if ((it.licenseDuration <= 0 || it.playbackDuration <= 0) &&
                    wvSDK?.getDownloadState() == DownloadState.COMPLETED) {
                    Toast.makeText(applicationContext, "Expired license", Toast.LENGTH_LONG)
                        .show()
                }
                // mediaItem = wvSDK?.getMediaItem()
                wvSDK?.getMediaItem()?.let { media ->
                    playerManager?.addItem(media)
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
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        playerManager = PlayerManager(
            this,
            object: PlayerManager.Listener {
                override fun onQueuePositionChanged(previousIndex: Int, newIndex: Int) {
                    print("onQueuePositionChanged")
                }

                override fun onUnsupportedTrack(trackType: Int) {
                    print("onUnsupportedTrack")
                }

            },
            playerView!!
        )
    }

    override fun onStop() {
        super.onStop()
        if (isFinishing) {
            releasePlayer()
        }
    }

    private fun releasePlayer() {
        playerManager?.release()
    }

    override fun onCastStateChanged(p0: Int) {
        print(p0);
    }
}