package com.gitaradistortion

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class PanelAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PedalBoardFragment()   // ✅ PANEL 1 — IKINABIT NA PEDAL
            1 -> CabinetFragment()      // ✅ PANEL 2 — LAHAT NG PEDAL
            else -> PedalBoardFragment()
        }
    }
}
