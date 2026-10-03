package com.mhxx.gansimu.ui.simulator

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.mhxx.gansimu.R
import com.mhxx.gansimu.data.models.SearchResult
import com.mhxx.gansimu.databinding.ActivityResultDetailBinding
import com.mhxx.gansimu.util.AppData

class ResultDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.result_detail_title)

        val result = intent.getParcelableExtra<SearchResult>("result") ?: return
        displayResult(result)

        binding.btnSaveToMySet.setOnClickListener {
            saveToMySet(result)
        }
    }

    private fun displayResult(result: SearchResult) {
        binding.tvHeadDetail.text = "頭: ${result.head?.name ?: "（なし）"}"
        binding.tvBodyDetail.text = "胴: ${result.body?.name ?: "（なし）"}"
        binding.tvArmDetail.text = "腕: ${result.arm?.name ?: "（なし）"}"
        binding.tvWstDetail.text = "腰: ${result.wst?.name ?: "（なし）"}"
        binding.tvLegDetail.text = "脚: ${result.leg?.name ?: "（なし）"}"

        // 護石
        val charmText = buildString {
            if (result.charmSlot1Family.isNotEmpty()) {
                append("${result.charmSlot1Family}+${result.charmSlot1Points}")
            }
            if (result.charmSlot2Family.isNotEmpty()) {
                if (isNotEmpty()) append(", ")
                append("${result.charmSlot2Family}+${result.charmSlot2Points}")
            }
            if (result.charmSlots > 0) {
                if (isNotEmpty()) append(" ")
                append("[${result.charmSlots}]")
            }
            if (isEmpty()) append("護石不問")
        }
        binding.tvCharmDetail.text = "護石: $charmText"

        // 装飾品
        val decoText = result.decorations.joinToString("\n") { usage ->
            "  ${usage.decoration.name} ×${usage.count}"
        }
        binding.tvDecosDetail.text = if (decoText.isNotEmpty()) "装飾品:\n$decoText" else "装飾品: なし"

        // 発動スキル
        binding.tvActiveSkillsDetail.text = "発動スキル:\n" + result.activeSkills.joinToString("\n") { "  $it" }

        // 確率
        if (result.existsProbability.isNotEmpty() || result.existsProbabilityOld.isNotEmpty()) {
            binding.tvProbabilityDetail.text = "入手難度: ${result.existsProbability} / ${result.existsProbabilityOld}"
        } else {
            binding.tvProbabilityDetail.text = ""
        }

        // 防具の詳細ステータス
        val headDef = result.head?.defenseInit ?: 0
        val bodyDef = result.body?.defenseInit ?: 0
        val armDef = result.arm?.defenseInit ?: 0
        val wstDef = result.wst?.defenseInit ?: 0
        val legDef = result.leg?.defenseInit ?: 0
        binding.tvDefenseDetail.text = "防御力合計: ${headDef + bodyDef + armDef + wstDef + legDef}"
    }

    private fun saveToMySet(result: SearchResult) {
        val setName = "セット${AppData.mySets.size + 1}"
        val mySet = com.mhxx.gansimu.data.models.MySet(
            name = setName,
            head = result.head?.name ?: "",
            body = result.body?.name ?: "",
            arm = result.arm?.name ?: "",
            wst = result.wst?.name ?: "",
            leg = result.leg?.name ?: "",
            charmFamily1 = result.charmSlot1Family,
            charmPoints1 = result.charmSlot1Points,
            charmFamily2 = result.charmSlot2Family,
            charmPoints2 = result.charmSlot2Points,
            charmSlots = result.charmSlots
        )
        AppData.mySets.add(mySet)
        AppData.save(applicationContext)
        android.widget.Toast.makeText(this, "$setName を保存しました", android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
