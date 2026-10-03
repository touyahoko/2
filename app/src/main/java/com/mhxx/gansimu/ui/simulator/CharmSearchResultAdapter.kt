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
import com.mhxx.gansimu.data.models.CharmSearchResult

class CharmSearchResultAdapter(
    private val onItemClick: (CharmSearchResult) -> Unit
) : ListAdapter<CharmSearchResult, CharmSearchResultAdapter.ViewHolder>(DIFF_CALLBACK) {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCharmSkills: TextView = itemView.findViewById(R.id.tvCharmSkills)
        val tvCharmSlots: TextView = itemView.findViewById(R.id.tvCharmSlots)
        val tvDecoReq: TextView = itemView.findViewById(R.id.tvDecoReq)
        val tvHead: TextView = itemView.findViewById(R.id.tvHead)
        val tvBody: TextView = itemView.findViewById(R.id.tvBody)
        val tvArm: TextView = itemView.findViewById(R.id.tvArm)
        val tvWst: TextView = itemView.findViewById(R.id.tvWst)
        val tvLeg: TextView = itemView.findViewById(R.id.tvLeg)
        val tvProbMH4G: TextView = itemView.findViewById(R.id.tvProbMH4G)
        val tvProbOld: TextView = itemView.findViewById(R.id.tvProbOld)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_charm_search_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val result = getItem(position)

        // 護石スキル表示
        val charmSkillText = buildString {
            if (result.charmFamily1.isNotEmpty() && result.charmPoints1 != 0) {
                append("${result.charmFamily1}+${result.charmPoints1}")
            } else {
                append("護石スキル不問")
            }
            if (result.charmFamily2.isNotEmpty() && result.charmPoints2 != 0) {
                append("\n${result.charmFamily2}+${result.charmPoints2}")
            }
        }
        holder.tvCharmSkills.text = charmSkillText

        // 護石スロット
        holder.tvCharmSlots.text = if (result.charmSlots > 0) "スロット${result.charmSlots}" else "スロット不問"

        // 装飾品要件
        if (result.decoFamily.isNotEmpty()) {
            holder.tvDecoReq.text = "${result.decoFamily}珠[${result.decoSize}]"
            holder.tvDecoReq.visibility = View.VISIBLE
        } else {
            holder.tvDecoReq.visibility = View.GONE
        }

        // 防具
        holder.tvHead.text = result.head?.name ?: "（なし）"
        holder.tvBody.text = result.body?.name ?: "（なし）"
        holder.tvArm.text = result.arm?.name ?: "（なし）"
        holder.tvWst.text = result.wst?.name ?: "（なし）"
        holder.tvLeg.text = result.leg?.name ?: "（なし）"

        // 確率
        holder.tvProbMH4G.text = "(風 ${result.existsProbabilityMH4G})"
        holder.tvProbOld.text = "(古 ${result.existsProbabilityOld})"

        // 背景色（交互）
        holder.itemView.setBackgroundColor(
            if (position % 2 == 0) Color.parseColor("#1a2a3a") else Color.parseColor("#0d1a2a")
        )

        holder.itemView.setOnClickListener { onItemClick(result) }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CharmSearchResult>() {
            override fun areItemsTheSame(a: CharmSearchResult, b: CharmSearchResult) = a === b
            override fun areContentsTheSame(a: CharmSearchResult, b: CharmSearchResult) = a == b
        }
    }
}
