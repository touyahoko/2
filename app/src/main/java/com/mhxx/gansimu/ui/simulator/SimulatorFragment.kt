package com.mhxx.gansimu.ui.simulator

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.mhxx.gansimu.R
import com.mhxx.gansimu.data.loader.CsvLoader
import com.mhxx.gansimu.data.models.*
import com.mhxx.gansimu.data.search.SkillSearchEngine
import com.mhxx.gansimu.databinding.FragmentSimulatorBinding
import com.mhxx.gansimu.util.AppData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SimulatorFragment : Fragment() {

    private var _binding: FragmentSimulatorBinding? = null
    private val binding get() = _binding!!

    private val desiredSkills = mutableListOf<DesiredSkill>()
    private lateinit var resultAdapter: SearchResultAdapter
    private val searchEngine = SkillSearchEngine()

    companion object {
        private const val MAX_DESIRED_SKILLS = 10
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSimulatorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupWeaponTypeRadio()
        setupGenderRadio()
        setupSkillSelector()
        setupSearchButton()
        setupCharmSearchButton()
        setupResultList()
    }

    private fun setupWeaponTypeRadio() {
        binding.radioGroupWeaponType.setOnCheckedChangeListener { _, checkedId ->
            AppData.weaponType = when (checkedId) {
                R.id.radioSwordman -> 1
                R.id.radioGunner -> 2
                else -> 0
            }
            refreshSkillDropdown()
        }

        // 初期値設定
        when (AppData.weaponType) {
            1 -> binding.radioSwordman.isChecked = true
            2 -> binding.radioGunner.isChecked = true
            else -> binding.radioBoth.isChecked = true
        }
    }

    private fun setupGenderRadio() {
        binding.radioGroupGender.setOnCheckedChangeListener { _, checkedId ->
            AppData.gender = when (checkedId) {
                R.id.radioMale -> 1
                R.id.radioFemale -> 2
                else -> 0
            }
        }

        when (AppData.gender) {
            1 -> binding.radioMale.isChecked = true
            2 -> binding.radioFemale.isChecked = true
            else -> binding.radioBothGender.isChecked = true
        }
    }

    private fun setupSkillSelector() {
        refreshSkillDropdown()

        binding.btnAddSkill.setOnClickListener {
            if (desiredSkills.size >= MAX_DESIRED_SKILLS) {
                Toast.makeText(context, "スキルは最大${MAX_DESIRED_SKILLS}個まで", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedSkillName = binding.spinnerSkillSelect.selectedItem?.toString() ?: return@setOnClickListener
            val skill = CsvLoader.getSkills().find { it.name == selectedSkillName } ?: return@setOnClickListener

            // 重複チェック
            if (desiredSkills.any { it.familyName == skill.family }) {
                Toast.makeText(context, "同じ系統のスキルが既に選択されています", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val desired = DesiredSkill(
                skillName = skill.name,
                familyName = skill.family,
                requiredPoints = skill.points
            )
            desiredSkills.add(desired)
            addSkillChip(desired)
        }
    }

    private fun refreshSkillDropdown() {
        val weaponType = AppData.weaponType
        val allSkills = CsvLoader.getSkills().filter { skill ->
            skill.points > 0 &&
            (weaponType == 0 || skill.type == 0 || skill.type == weaponType)
        }.sortedBy { it.name }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            allSkills.map { it.name }
        )
        binding.spinnerSkillSelect.adapter = adapter
    }

    private fun addSkillChip(desired: DesiredSkill) {
        val chip = Chip(requireContext()).apply {
            text = "${desired.skillName}"
            isCloseIconVisible = true
            setOnCloseIconClickListener {
                desiredSkills.remove(desired)
                binding.skillChipGroup.removeView(this)
            }
        }
        binding.skillChipGroup.addView(chip)
    }

    private fun setupSearchButton() {
        binding.btnSearch.setOnClickListener {
            if (desiredSkills.isEmpty()) {
                Toast.makeText(context, "スキルを選択してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            performSearch()
        }

        binding.btnClear.setOnClickListener {
            desiredSkills.clear()
            binding.skillChipGroup.removeAllViews()
            resultAdapter.submitList(emptyList())
            binding.tvResultCount.text = ""
        }
    }

    private fun setupCharmSearchButton() {
        binding.btnCharmSearch.setOnClickListener {
            if (desiredSkills.isEmpty()) {
                Toast.makeText(context, "スキルを選択してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(requireContext(), CharmSearchResultActivity::class.java)
            intent.putParcelableArrayListExtra("desiredSkills", ArrayList(desiredSkills))
            startActivity(intent)
        }
    }

    private fun setupResultList() {
        resultAdapter = SearchResultAdapter { result ->
            // 結果タップで詳細表示
            val intent = Intent(requireContext(), ResultDetailActivity::class.java)
            intent.putExtra("result", result)
            startActivity(intent)
        }
        binding.recyclerResults.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerResults.adapter = resultAdapter
    }

    private fun performSearch() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSearch.isEnabled = false
        binding.tvResultCount.text = getString(R.string.searching)

        lifecycleScope.launch {
            val results = withContext(Dispatchers.Default) {
                searchEngine.search(desiredSkills, AppData.buildSearchSettings())
            }

            binding.progressBar.visibility = View.GONE
            binding.btnSearch.isEnabled = true
            binding.tvResultCount.text = getString(R.string.result_count, results.size)
            resultAdapter.submitList(results)

            if (results.isEmpty()) {
                Toast.makeText(context, getString(R.string.no_results), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun refreshSettings() {
        when (AppData.weaponType) {
            1 -> binding.radioSwordman.isChecked = true
            2 -> binding.radioGunner.isChecked = true
            else -> binding.radioBoth.isChecked = true
        }
        when (AppData.gender) {
            1 -> binding.radioMale.isChecked = true
            2 -> binding.radioFemale.isChecked = true
            else -> binding.radioBothGender.isChecked = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
