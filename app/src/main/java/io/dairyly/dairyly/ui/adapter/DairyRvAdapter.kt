package io.dairyly.dairyly.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import io.dairyly.dairyly.R
import io.dairyly.dairyly.data.models.DiaryEntry
import io.dairyly.dairyly.databinding.CardNormalDiaryBinding
import java.text.SimpleDateFormat


class DairyRvAdapter : ListAdapter<DiaryEntry, DairyRvAdapter.DairyViewHolder>(
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

        println(item)
        holder.itemViewBinding.apply {
            detailTextView.apply {
                // text = item.info.toString()
                if(item.blockInfo.isNotEmpty()) {
                    text = item.blockInfo[0].content
                }
            }

            overlineTextView.apply {
                val dateFormat = SimpleDateFormat("hh:mm", this.context.resources.configuration.locales[0])
                text = dateFormat.format(item.info.timeCreated)
            }

            tagChipGroup.apply {
                for(tag in item.tags){
                    val inflater = LayoutInflater.from(context)

                    val chip = inflater.inflate(R.layout.chip_diary, null, false) as Chip
                    chip.apply {
                        setChipDrawable(ChipDrawable.createFromAttributes(context, null, 0, R.style.Widget_MaterialComponents_Chip_Action))
                        text = tag.string
                    }
                    this.addView(chip)
                }
            }

        }
    }

    class DairyViewHolder(val itemViewBinding: CardNormalDiaryBinding) : RecyclerView.ViewHolder(itemViewBinding.root)

}

class DiaryDiffCallback : DiffUtil.ItemCallback<DiaryEntry>(){
    override fun areItemsTheSame(oldItem: DiaryEntry, newItem: DiaryEntry): Boolean {
        return  oldItem.info.entryId == newItem.info.entryId
    }

    override fun areContentsTheSame(oldItem: DiaryEntry, newItem: DiaryEntry): Boolean {
        return oldItem == newItem
    }
}
