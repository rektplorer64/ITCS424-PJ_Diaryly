package io.dairyly.dairyly.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.PrecomputedTextCompat
import androidx.core.widget.TextViewCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import io.dairyly.dairyly.R
import io.dairyly.dairyly.data.models.DiaryDateHolder
import io.dairyly.dairyly.data.models.DiaryEntry
import io.dairyly.dairyly.databinding.CardNormalDiaryBinding
import io.dairyly.dairyly.screens.entry.EntryActivityArgs
import java.text.SimpleDateFormat


class DairyRvAdapter(val diaryDateHolder: DiaryDateHolder) : ListAdapter<DiaryEntry, DairyRvAdapter.DairyViewHolder>(
        DiaryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DairyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemBinding = CardNormalDiaryBinding.inflate(layoutInflater, parent, false)
        return DairyViewHolder(
                itemBinding)
    }

    @SuppressLint("InflateParams")
    override fun onBindViewHolder(holder: DairyViewHolder, position: Int) {
        val item = getItem(position)

        holder.itemViewBinding.apply {

            root.setOnClickListener {
                val b = EntryActivityArgs(item.info.entryId, item.info.userId, diaryDateHolder)
                it.findNavController().navigate(R.id.moreEntryDetailAction, b.toBundle())
            }

            detailTextView.apply {
                // text = item.info.toString()
                if(item.blockInfo.isNotEmpty()) {
                    // val precomputedTextParams: PrecomputedTextCompat.Params = TextViewCompat.getTextMetricsParams(this)
                    // val precomputedText = PrecomputedTextCompat.create(item.blockInfo[0].content, precomputedTextParams)
                    // TextViewCompat.setPrecomputedText(this, precomputedText)

                    this.setTextFuture(PrecomputedTextCompat.getTextFuture(
                            item.blockInfo[0].content,
                            TextViewCompat.getTextMetricsParams(this) /* Do not change TextView property after this line */,
                            /*optional custom executor*/ null))
                }
            }

            overlineTextView.apply {
                val dateFormat = SimpleDateFormat("hh:mm dd/MM/YYYY",
                                                  this.context.resources.configuration.locales[0])
                text = dateFormat.format(item.info.timeCreated)
            }

            tagChipGroup.apply {
                for(element in item.tags) {
                    this.addView((LayoutInflater
                            .from(context)
                            .inflate(R.layout.chip_tag, null, false) as Chip)
                                         .apply {
                                             setChipDrawable(
                                                     ChipDrawable
                                                             .createFromAttributes(context,
                                                                                   null, 0,
                                                                                   R.style.Widget_MaterialComponents_Chip_Action))
                                             text = element.string
                                         })
                }
            }

        }
    }

    class DairyViewHolder(val itemViewBinding: CardNormalDiaryBinding) :
            RecyclerView.ViewHolder(itemViewBinding.root)

    private class DiaryDiffCallback : DiffUtil.ItemCallback<DiaryEntry>() {
        override fun areItemsTheSame(oldItem: DiaryEntry, newItem: DiaryEntry): Boolean {
            return oldItem.info.entryId == newItem.info.entryId
        }

        override fun areContentsTheSame(oldItem: DiaryEntry, newItem: DiaryEntry): Boolean {
            return oldItem == newItem
        }
    }
}


