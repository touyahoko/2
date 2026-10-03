package com.mhxx.gansimu.ui.charm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.mhxx.gansimu.R
import com.mhxx.gansimu.data.loader.CsvLoader
import com.mhxx.gansimu.databinding.FragmentCharmSettingsBinding
import com.mhxx.gansimu.util.AppData

/**
 * お守り設定フラグメント
 * ユーザーが持っている護石の情報を設定する
 */
class CharmSettingsFragment : Fragment() {

    private var _binding: FragmentCharmSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCharmSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFamilySpinners()
        setupPointsInputs()
        setupSlotsSpinner()
        setupRaritySpinner()
        setupSaveButton()
        setupClearButton()
        loadCurrentCharm()
    }

    private fun setupFamilySpinners() {
        val families = CsvLoader.getSkillFamilies().map { it.name }.sorted()
        val familiesWithEmpty = listOf("（なし）") + families

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            familiesWithEmpty
        )

        binding.spinnerCharmFamily1.adapter = adapter
        binding.spinnerCharmFamily2.adapter = adapter
    }

    private fun setupPointsInputs() {
        // ポイント範囲: -10 to +10
        val points = (-10..10).map { it.toString() }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            points
        )
        binding.spinnerCharmPoints1.adapter = adapter
        binding.spinnerCharmPoints2.adapter = adapter

        // デフォルト: 0
        binding.spinnerCharmPoints1.setSelection(points.indexOf("0"))
        binding.spinnerCharmPoints2.setSelection(points.indexOf("0"))
    }

    private fun setupSlotsSpinner() {
        val slots = listOf("0", "1", "2", "3")
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            slots
        )
        binding.spinnerCharmSlots.adapter = adapter
    }

    private fun setupRaritySpinner() {
        val rarities = (1..10).map { "レア${it}" }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            rarities
        )
        binding.spinnerCharmRarity.adapter = adapter
    }

    private fun loadCurrentCharm() {
        // 護石系統1
        val families = CsvLoader.getSkillFamilies().map { it.name }.sorted()
        val familiesWithEmpty = listOf("（なし）") + families

        if (AppData.charmFamily1.isNotEmpty()) {
            val idx = familiesWithEmpty.indexOf(AppData.charmFamily1)
            if (idx >= 0) binding.spinnerCharmFamily1.setSelection(idx)
        }
        if (AppData.charmFamily2.isNotEmpty()) {
            val idx = familiesWithEmpty.indexOf(AppData.charmFamily2)
            if (idx >= 0) binding.spinnerCharmFamily2.setSelection(idx)
        }

        val points = (-10..10).map { it.toString() }
        binding.spinnerCharmPoints1.setSelection(points.indexOf(AppData.charmPoints1.toString()))
        binding.spinnerCharmPoints2.setSelection(points.indexOf(AppData.charmPoints2.toString()))
        binding.spinnerCharmSlots.setSelection(AppData.charmSlots)
        binding.spinnerCharmRarity.setSelection(maxOf(0, AppData.charmRarity - 1))
    }

    private fun setupSaveButton() {
        binding.btnSaveCharm.setOnClickListener {
            val familiesWithEmpty = listOf("（なし）") + CsvLoader.getSkillFamilies().map { it.name }.sorted()
            val points = (-10..10).map { it.toString() }

            val f1 = familiesWithEmpty[binding.spinnerCharmFamily1.selectedItemPosition]
            val f2 = familiesWithEmpty[binding.spinnerCharmFamily2.selectedItemPosition]
            val p1 = points[binding.spinnerCharmPoints1.selectedItemPosition].toIntOrNull() ?: 0
            val p2 = points[binding.spinnerCharmPoints2.selectedItemPosition].toIntOrNull() ?: 0

            AppData.charmFamily1 = if (f1 == "（なし）") "" else f1
            AppData.charmFamily2 = if (f2 == "（なし）") "" else f2
            AppData.charmPoints1 = p1
            AppData.charmPoints2 = p2
            AppData.charmSlots = binding.spinnerCharmSlots.selectedItemPosition
            AppData.charmRarity = binding.spinnerCharmRarity.selectedItemPosition + 1

            AppData.save(requireContext())
            Toast.makeText(requireContext(), getString(R.string.charm_saved), Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClearButton() {
        binding.btnClearCharm.setOnClickListener {
            AppData.charmFamily1 = ""
            AppData.charmFamily2 = ""
            AppData.charmPoints1 = 0
            AppData.charmPoints2 = 0
            AppData.charmSlots = 0
            AppData.charmRarity = 1

            binding.spinnerCharmFamily1.setSelection(0)
            binding.spinnerCharmFamily2.setSelection(0)
            val points = (-10..10).map { it.toString() }
            binding.spinnerCharmPoints1.setSelection(points.indexOf("0"))
            binding.spinnerCharmPoints2.setSelection(points.indexOf("0"))
            binding.spinnerCharmSlots.setSelection(0)
            binding.spinnerCharmRarity.setSelection(0)

            AppData.save(requireContext())
            Toast.makeText(requireContext(), getString(R.string.charm_cleared), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
