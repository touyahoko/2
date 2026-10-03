package com.mhxx.gansimu.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mhxx.gansimu.ui.charm.CharmSettingsFragment
import com.mhxx.gansimu.ui.myset.MySetFragment
import com.mhxx.gansimu.ui.settings.DecorationExcludeFragment
import com.mhxx.gansimu.ui.settings.EquipmentExcludeFragment
import com.mhxx.gansimu.ui.settings.EquipmentFixFragment
import com.mhxx.gansimu.ui.simulator.SimulatorFragment

class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 6

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            MainActivity.TAB_SIMULATOR -> SimulatorFragment()
            MainActivity.TAB_EXCLUDE_EQUIP -> EquipmentExcludeFragment()
            MainActivity.TAB_FIX_EQUIP -> EquipmentFixFragment()
            MainActivity.TAB_EXCLUDE_DECO -> DecorationExcludeFragment()
            MainActivity.TAB_CHARM -> CharmSettingsFragment()
            MainActivity.TAB_MYSET -> MySetFragment()
            else -> SimulatorFragment()
        }
    }
}
