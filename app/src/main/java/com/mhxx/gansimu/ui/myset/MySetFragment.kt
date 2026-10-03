package com.mhxx.gansimu.ui.myset

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.mhxx.gansimu.R
import com.mhxx.gansimu.data.models.MySet
import com.mhxx.gansimu.databinding.FragmentMysetBinding
import com.mhxx.gansimu.util.AppData

class MySetFragment : Fragment() {

    private var _binding: FragmentMysetBinding? = null
    private val binding get() = _binding!!
    private lateinit var setAdapter: ArrayAdapter<String>
    private val setNames = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMysetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupList()
        refreshList()
    }

    private fun setupList() {
        setAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, setNames)
        binding.listMySets.adapter = setAdapter

        binding.listMySets.setOnItemClickListener { _, _, position, _ ->
            showSetDetail(AppData.mySets[position])
        }

        binding.listMySets.setOnItemLongClickListener { _, _, position, _ ->
            val set = AppData.mySets[position]
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("削除確認")
                .setMessage("「${set.name}」を削除しますか？")
                .setPositiveButton("削除") { _, _ ->
                    AppData.mySets.removeAt(position)
                    AppData.save(requireContext())
                    refreshList()
                }
                .setNegativeButton("キャンセル", null)
                .show()
            true
        }
    }

    private fun refreshList() {
        setNames.clear()
        setNames.addAll(AppData.mySets.map { it.name })
        setAdapter.notifyDataSetChanged()
        binding.tvMySetCount.text = getString(R.string.myset_count, setNames.size)
    }

    private fun showSetDetail(set: MySet) {
        val detail = buildString {
            appendLine("【${set.name}】")
            if (set.head.isNotEmpty()) appendLine("頭: ${set.head}")
            if (set.body.isNotEmpty()) appendLine("胴: ${set.body}")
            if (set.arm.isNotEmpty()) appendLine("腕: ${set.arm}")
            if (set.wst.isNotEmpty()) appendLine("腰: ${set.wst}")
            if (set.leg.isNotEmpty()) appendLine("脚: ${set.leg}")
            if (set.charmFamily1.isNotEmpty()) {
                appendLine("護石: ${set.charmFamily1}+${set.charmPoints1}")
                if (set.charmFamily2.isNotEmpty()) append(" / ${set.charmFamily2}+${set.charmPoints2}")
                if (set.charmSlots > 0) appendLine(" [${set.charmSlots}]") else appendLine()
            }
            if (set.memo.isNotEmpty()) appendLine("メモ: ${set.memo}")
        }

        android.app.AlertDialog.Builder(requireContext())
            .setTitle(set.name)
            .setMessage(detail)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
