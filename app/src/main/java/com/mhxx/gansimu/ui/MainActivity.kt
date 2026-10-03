package com.mhxx.gansimu.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mhxx.gansimu.R
import com.mhxx.gansimu.data.loader.CsvLoader
import com.mhxx.gansimu.databinding.ActivityMainBinding
import com.mhxx.gansimu.ui.charm.CharmSearchActivity
import com.mhxx.gansimu.ui.myset.MySetActivity
import com.mhxx.gansimu.ui.settings.DecorationExcludeActivity
import com.mhxx.gansimu.ui.settings.EquipmentExcludeActivity
import com.mhxx.gansimu.ui.settings.EquipmentFixActivity
import com.mhxx.gansimu.ui.simulator.SimulatorFragment
import com.mhxx.gansimu.util.AppData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        const val TAB_SIMULATOR = 0
        const val TAB_EXCLUDE_EQUIP = 1
        const val TAB_FIX_EQUIP = 2
        const val TAB_EXCLUDE_DECO = 3
        const val TAB_CHARM = 4
        const val TAB_MYSET = 5
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        // データをバックグラウンドでロード
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                AppData.load(applicationContext)
                CsvLoader.loadAll(applicationContext)
            }
            setupTabs()
        }
    }

    private fun setupTabs() {
        val tabTitles = listOf(
            getString(R.string.tab_simulator),
            getString(R.string.tab_exclude_equip),
            getString(R.string.tab_fix_equip),
            getString(R.string.tab_exclude_deco),
            getString(R.string.tab_charm_settings),
            getString(R.string.tab_myset)
        )

        val adapter = MainPagerAdapter(this)
        binding.viewPager.adapter = adapter
        binding.viewPager.offscreenPageLimit = 5

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_charm_search -> {
                startActivity(Intent(this, CharmSearchActivity::class.java))
                true
            }
            R.id.action_myset -> {
                startActivity(Intent(this, MySetActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        // 設定変更後にフラグメントを更新
        val fragment = supportFragmentManager.findFragmentByTag("f${TAB_SIMULATOR}")
        (fragment as? SimulatorFragment)?.refreshSettings()
    }

    override fun onPause() {
        super.onPause()
        AppData.save(applicationContext)
    }
}
