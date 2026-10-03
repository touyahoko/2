package com.mhxx.gansimu.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.mhxx.gansimu.R
import com.mhxx.gansimu.data.loader.CsvLoader
import com.mhxx.gansimu.databinding.FragmentDecorationExcludeBinding
import com.mhxx.gansimu.util.AppData

class DecorationExcludeFragment : Fragment() {

    private var _binding: FragmentDecorationExcludeBinding? = null
    private val binding get() = _binding!!
    private lateinit var excludedAdapter: ArrayAdapter<String>
    private val excludedList = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDecorationExcludeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDecoSpinner()
        setupButtons()
        setupExcludedList()
        refreshExcludedList()
    }

    private fun setupDecoSpinner() {
        val decos = CsvLoader.getDecorations().map { it.name }.sorted()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, decos)
        binding.spinnerExcludeDeco.adapter = adapter
    }

    private fun setupButtons() {
        binding.btnAddExcludeDeco.setOnClickListener {
            val deco = binding.spinnerExcludeDeco.selectedItem?.toString() ?: return@setOnClickListener
            if (deco !in AppData.excludedDecorations) {
                AppData.excludedDecorations.add(deco)
                AppData.save(requireContext())
                refreshExcludedList()
            }
        }

        binding.btnClearExcludeDeco.setOnClickListener {
            AppData.excludedDecorations.clear()
            AppData.save(requireContext())
            refreshExcludedList()
            Toast.makeText(requireContext(), getString(R.string.deco_excluded_cleared), Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupExcludedList() {
        excludedList.addAll(AppData.excludedDecorations)
        excludedAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, excludedList)
        binding.listExcludedDecos.adapter = excludedAdapter
        binding.listExcludedDecos.setOnItemLongClickListener { _, _, position, _ ->
            val name = excludedList[position]
            AppData.excludedDecorations.remove(name)
            AppData.save(requireContext())
            refreshExcludedList()
            true
        }
    }

    private fun refreshExcludedList() {
        excludedList.clear()
        excludedList.addAll(AppData.excludedDecorations.sorted())
        excludedAdapter.notifyDataSetChanged()
        binding.tvDecoExcludeCount.text = getString(R.string.excluded_count, excludedList.size)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
