package com.mhxx.gansimu.ui.simulator

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mhxx.gansimu.R
import com.mhxx.gansimu.data.models.DesiredSkill
import com.mhxx.gansimu.data.search.CharmSearchEngine
import com.mhxx.gansimu.databinding.ActivityCharmSearchResultBinding
import com.mhxx.gansimu.util.AppData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * スキルから要求護石を検索する Activity
 * 頑シミュの主要特徴機能
 */
class CharmSearchResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCharmSearchResultBinding
    private val charmSearchEngine = CharmSearchEngine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCharmSearchResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.charm_search_result_title)

        val desiredSkills = intent.getParcelableArrayListExtra<DesiredSkill>("desiredSkills")
            ?: return

        // スキル一覧を表示
        binding.tvDesiredSkills.text = getString(R.string.desired_skills_label) +
                desiredSkills.joinToString(", ") { it.skillName }

        setupResultList()
        performCharmSearch(desiredSkills)
    }

    private fun setupResultList() {
        val adapter = CharmSearchResultAdapter { result ->
            // マイセットに追加するなどの操作
        }
        binding.recyclerCharmResults.layoutManager = LinearLayoutManager(this)
        binding.recyclerCharmResults.adapter = adapter
    }

    private fun performCharmSearch(desiredSkills: List<DesiredSkill>) {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvResultCount.text = getString(R.string.searching)

        lifecycleScope.launch {
            val results = withContext(Dispatchers.Default) {
                charmSearchEngine.searchRequiredCharms(desiredSkills, AppData.buildSearchSettings())
            }

            binding.progressBar.visibility = View.GONE
            binding.tvResultCount.text = getString(R.string.result_count, results.size)

            (binding.recyclerCharmResults.adapter as? CharmSearchResultAdapter)?.submitList(results)

            if (results.isEmpty()) {
                Toast.makeText(this@CharmSearchResultActivity, getString(R.string.no_results), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
