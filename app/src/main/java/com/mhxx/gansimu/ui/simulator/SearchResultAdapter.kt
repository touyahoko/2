package com.mhxx.gansimu.ui.simulator

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mhxx.gansimu.R
import com.mhxx.gansimu.data.models.SearchResult

class SearchResultAdapter(
    private val onItemClick: (SearchResult) -> Unit
) : ListAdapter<SearchResult, SearchResultAdapter.ViewHolder>(DIFF_CALLBACK) {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvHead: TextView = itemView.findViewById(R.id.tvHead)
        val tvBody: TextView = itemView.findViewById(R.id.tvBody)
        val tvArm: TextView = itemView.findViewById(R.id.tvArm)
        val tvWst: TextView = itemView.findViewById(R.id.tvWst)
        val tvLeg: TextView = itemView.findViewById(R.id.tvLeg)
        val tvCharm: TextView = itemView.findViewById(R.id.tvCharm)
        val tvDecos: TextView = itemView.findViewById(R.id.tvDecos)
        val tvProbability: TextView = itemView.findViewById(R.id.tvProbability)
        val tvActiveSkills: TextView = itemView.findViewById(R.id.tvActiveSkills)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val result = getItem(position)

        holder.tvHead.text = result.head?.name ?: "（なし）"
        holder.tvBody.text = result.body?.name ?: "（なし）"
        holder.tvArm.text = result.arm?.name ?: "（なし）"
        holder.tvWst.text = result.wst?.name ?: "（なし）"
        holder.tvLeg.text = result.leg?.name ?: "（なし）"

        // 護石表示
        val charmText = buildString {
            if (result.charmSlot1Family.isNotEmpty() && result.charmSlot1Points != 0) {
                append("${result.charmSlot1Family}×${result.charmSlot1Points}")
            }
            if (result.charmSlot2Family.isNotEmpty() && result.charmSlot2Points != 0) {
                if (isNotEmpty()) append(" / ")
                append("${result.charmSlot2Family}×${result.charmSlot2Points}")
            }
            if (result.charmSlots > 0) {
                if (isNotEmpty()) append(" ")
                append("[${result.charmSlots}]")
            }
        }
        holder.tvCharm.text = if (charmText.isNotEmpty()) charmText else "護石不問"

        // 装飾品表示
        val decoText = result.decorations.joinToString("\n") { usage ->
            val mainSkill = usage.decoration.skillPoints.entries.firstOrNull()
            val skillStr = if (mainSkill != null) "${mainSkill.key}" else usage.decoration.name
            "${skillStr}珠[${usage.decoration.slotSize}]×${usage.count}"
        }
        holder.tvDecos.text = if (decoText.isNotEmpty()) decoText else ""

        // 確率表示
        val probText = buildString {
            if (result.existsProbability.isNotEmpty()) {
                append(result.existsProbability)
            }
            if (result.existsProbabilityOld.isNotEmpty() && result.existsProbabilityOld != result.existsProbability) {
                if (isNotEmpty()) append(" / ")
                append(result.existsProbabilityOld)
            }
        }
        holder.tvProbability.text = probText

        // 発動スキル
        holder.tvActiveSkills.text = result.activeSkills.joinToString(", ")

        // 背景色（交互）
        if (position % 2 == 0) {
            holder.itemView.setBackgroundColor(Color.parseColor("#1a2a3a"))
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#0d1a2a"))
        }

        holder.itemView.setOnClickListener { onItemClick(result) }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SearchResult>() {
            override fun areItemsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean =
                oldItem === newItem

            override fun areContentsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean =
                oldItem == newItem
        }
    }
}
