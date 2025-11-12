package com.doverunner.advencedsample.dialog

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class TrackPagerAdapter(
    fragment: DialogFragment
): FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        var count = 0
        if (TrackSelectUtil.tracks.video.size > 0) {
            count++
        }

        if (TrackSelectUtil.tracks.audio.size > 0) {
            count++
        }

        if (TrackSelectUtil.tracks.text.size > 0) {
            count++
        }

        return count
    }

    override fun createFragment(position: Int): Fragment {
        var currentPos = 0

        if (TrackSelectUtil.tracks.video.isNotEmpty()) {
            if (position == currentPos) return OptionFragment()
            currentPos++
        }

        if (TrackSelectUtil.tracks.audio.isNotEmpty()) {
            if (position == currentPos) return CheckFragment(true)
            currentPos++
        }

        if (TrackSelectUtil.tracks.text.isNotEmpty()) {
            if (position == currentPos) return CheckFragment(false)
            currentPos++
        }

        return OptionFragment()
    }

}