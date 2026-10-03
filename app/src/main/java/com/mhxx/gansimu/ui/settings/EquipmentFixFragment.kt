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
import com.mhxx.gansimu.databinding.FragmentEquipmentFixBinding
import com.mhxx.gansimu.util.AppData

class EquipmentFixFragment : Fragment() {

    private var _binding: FragmentEquipmentFixBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEquipmentFixBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSlotSpinners()
        setupButtons()
        loadFixedEquipments()
    }

    private fun setupSlotSpinners() {
        val slots = mapOf(
            EquipSlot.HEAD to binding.spinnerFixHead,
            EquipSlot.BODY to binding.spinnerFixBody,
            EquipSlot.ARM to binding.spinnerFixArm,
            EquipSlot.WST to binding.spinnerFixWst,
            EquipSlot.LEG to binding.spinnerFixLeg
        )
        for ((slot, spinner) in slots) {
            val items = listOf("（固定なし）") + (CsvLoader.getAllEquipments()[slot] ?: emptyList()).map { it.name }.sorted()
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, items)
            spinner.adapter = adapter
        }
    }

    private fun loadFixedEquipments() {
        setSpinnerSelection(binding.spinnerFixHead, EquipSlot.HEAD)
        setSpinnerSelection(binding.spinnerFixBody, EquipSlot.BODY)
        setSpinnerSelection(binding.spinnerFixArm, EquipSlot.ARM)
        setSpinnerSelection(binding.spinnerFixWst, EquipSlot.WST)
        setSpinnerSelection(binding.spinnerFixLeg, EquipSlot.LEG)
    }

    private fun setSpinnerSelection(spinner: Spinner, slot: EquipSlot) {
        val fixed = AppData.fixedEquipments[slot] ?: return
        val adapter = spinner.adapter
        for (i in 0 until adapter.count) {
            if (adapter.getItem(i).toString() == fixed) {
                spinner.setSelection(i)
                break
            }
        }
    }

    private fun setupButtons() {
        binding.btnSaveFix.setOnClickListener {
            saveFixedEquipment(binding.spinnerFixHead, EquipSlot.HEAD)
            saveFixedEquipment(binding.spinnerFixBody, EquipSlot.BODY)
            saveFixedEquipment(binding.spinnerFixArm, EquipSlot.ARM)
            saveFixedEquipment(binding.spinnerFixWst, EquipSlot.WST)
            saveFixedEquipment(binding.spinnerFixLeg, EquipSlot.LEG)
            AppData.save(requireContext())
            Toast.makeText(requireContext(), getString(R.string.fixed_saved), Toast.LENGTH_SHORT).show()
        }

        binding.btnClearFix.setOnClickListener {
            AppData.fixedEquipments.clear()
            AppData.save(requireContext())
            binding.spinnerFixHead.setSelection(0)
            binding.spinnerFixBody.setSelection(0)
            binding.spinnerFixArm.setSelection(0)
            binding.spinnerFixWst.setSelection(0)
            binding.spinnerFixLeg.setSelection(0)
            Toast.makeText(requireContext(), getString(R.string.fixed_cleared), Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveFixedEquipment(spinner: Spinner, slot: EquipSlot) {
        val selected = spinner.selectedItem?.toString() ?: return
        if (selected == "（固定なし）") {
            AppData.fixedEquipments.remove(slot)
        } else {
            AppData.fixedEquipments[slot] = selected
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
