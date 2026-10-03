package com.mhxx.gansimu.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.mhxx.gansimu.R
import com.mhxx.gansimu.data.loader.CsvLoader
import com.mhxx.gansimu.data.models.EquipSlot
import com.mhxx.gansimu.databinding.FragmentEquipmentExcludeBinding
import com.mhxx.gansimu.util.AppData

class EquipmentExcludeFragment : Fragment() {

    private var _binding: FragmentEquipmentExcludeBinding? = null
    private val binding get() = _binding!!
    private lateinit var excludedAdapter: ArrayAdapter<String>
    private val excludedList = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEquipmentExcludeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSlotSpinner()
        setupEquipSpinner()
        setupButtons()
        setupExcludedList()
        refreshExcludedList()
    }

    private fun setupSlotSpinner() {
        val slots = listOf("頭", "胴", "腕", "腰", "脚")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, slots)
        binding.spinnerExcludeSlot.adapter = adapter
        binding.spinnerExcludeSlot.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                refreshEquipSpinner(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun refreshEquipSpinner(slotIdx: Int) {
        val slot = when (slotIdx) {
            0 -> EquipSlot.HEAD
            1 -> EquipSlot.BODY
            2 -> EquipSlot.ARM
            3 -> EquipSlot.WST
            4 -> EquipSlot.LEG
            else -> EquipSlot.HEAD
        }
        val equips = (CsvLoader.getAllEquipments()[slot] ?: emptyList()).map { it.name }.sorted()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, equips)
        binding.spinnerExcludeEquip.adapter = adapter
    }

    private fun setupEquipSpinner() {
        refreshEquipSpinner(0)
    }

    private fun setupButtons() {
        binding.btnAddExclude.setOnClickListener {
            val equip = binding.spinnerExcludeEquip.selectedItem?.toString() ?: return@setOnClickListener
            if (equip !in AppData.excludedEquipments) {
                AppData.excludedEquipments.add(equip)
                AppData.save(requireContext())
                refreshExcludedList()
            }
        }

        binding.btnClearExclude.setOnClickListener {
            AppData.excludedEquipments.clear()
            AppData.save(requireContext())
            refreshExcludedList()
            Toast.makeText(requireContext(), getString(R.string.excluded_cleared), Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupExcludedList() {
        excludedList.addAll(AppData.excludedEquipments)
        excludedAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, excludedList)
        binding.listExcluded.adapter = excludedAdapter
        binding.listExcluded.setOnItemLongClickListener { _, _, position, _ ->
            val name = excludedList[position]
            AppData.excludedEquipments.remove(name)
            AppData.save(requireContext())
            refreshExcludedList()
            true
        }
    }

    private fun refreshExcludedList() {
        excludedList.clear()
        excludedList.addAll(AppData.excludedEquipments.sorted())
        excludedAdapter.notifyDataSetChanged()
        binding.tvExcludeCount.text = getString(R.string.excluded_count, excludedList.size)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
