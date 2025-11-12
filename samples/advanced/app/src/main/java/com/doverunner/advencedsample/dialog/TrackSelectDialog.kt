package com.doverunner.advencedsample.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.doverunner.advencedsample.databinding.DialogTrackSelectBinding
import com.doverunner.widevine.track.DownloaderTracks

class TrackSelectDialog(
    tracks: DownloaderTracks,
    private val onOk: ((DownloaderTracks) -> Unit)?
) : DialogFragment() {
    private lateinit var binding: DialogTrackSelectBinding
    private val tabTitleArray: MutableList<String> = mutableListOf()
//    var tabLayout: TabLayout? = null
//    var viewPager: ViewPager2? = null
//    var pagerAdapter: TrackPagerAdapter? = null

    init {
        TrackSelectUtil.tracks = tracks
        // 실제 존재하는 트랙만 탭에 추가
        if (tracks.video.isNotEmpty()) {
            tabTitleArray.add("video")
        }
        if (tracks.audio.isNotEmpty()) {
            tabTitleArray.add("audio")
        }
        if (tracks.text.isNotEmpty()) {
            tabTitleArray.add("text")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogTrackSelectBinding.inflate(inflater, container, false)
        val pagerAdapter = TrackPagerAdapter(this)
        val viewPager = binding.trackSelectionDialogViewPager
        viewPager.adapter = pagerAdapter
        val tabLayout = binding.trackSelectionDialogTabLayout

        binding.trackSelectionDialogOkButton.setOnClickListener {
            onOk?.invoke(TrackSelectUtil.tracks)
            this.dismiss()
        }

        binding.trackSelectionDialogCancelButton.setOnClickListener {
            this.dismiss()
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitleArray[position]
        }.attach()

        return binding.root
    }

    companion object {
        const val TAG = "TrackSelectDialog"
    }
}