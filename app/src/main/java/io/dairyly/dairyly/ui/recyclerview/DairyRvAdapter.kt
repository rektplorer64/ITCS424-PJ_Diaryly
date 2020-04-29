package io.dairyly.dairyly.ui.recyclerview

import android.annotation.SuppressLint
import android.text.format.DateUtils
import android.text.format.DateUtils.FORMAT_ABBREV_ALL
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.PrecomputedTextCompat
import androidx.core.widget.TextViewCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.dairyly.dairyly.R
import io.dairyly.dairyly.data.models.DiaryDateHolder
import io.dairyly.dairyly.databinding.CardNormalDiaryNewBinding
import io.dairyly.dairyly.models.FirebaseStorageRepository.getImageStorageReference
import io.dairyly.dairyly.models.data.DiaryEntry
import io.dairyly.dairyly.screens.entry.EntryDisplayActivityArgs
import io.dairyly.dairyly.utils.populateTags
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min


class DairyRvAdapter(private val diaryDateHolder: DiaryDateHolder? = null) :
        ListAdapter<DiaryEntry, DairyRvAdapter.DairyViewHolder>(DiaryDiffCallback()) {

    private val LOG_TAG: String = this::class.java.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DairyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemBinding = CardNormalDiaryNewBinding.inflate(layoutInflater, parent, false)
        return DairyViewHolder(
                itemBinding)
    }

    @SuppressLint("InflateParams")
    override fun onBindViewHolder(holder: DairyViewHolder, position: Int) {
        val item = getItem(position)
        val context = holder.itemViewBinding.root.context

        holder.itemViewBinding.cardImageView.apply {
            try {
                Glide.with(context!!).load(item.images?.get(0)?.getImageStorageReference())
                        .into(this)
            } catch(e: Exception) {
                visibility = View.GONE
            }
        }

        holder.itemViewBinding.root.setOnClickListener {
            val dateHolder = diaryDateHolder ?: DiaryDateHolder(item.timeCreated, 0)
            val b = EntryDisplayActivityArgs(item.id, dateHolder)
            Log.d(LOG_TAG, "Clicked on item ID = ${item.id}")
            try {
                it.findNavController().navigate(R.id.moreEntryDetailAction, b.toBundle())
            } catch(e: IllegalArgumentException) {
                it.findNavController().navigate(R.id.searchMoreEntryDetailAction, b.toBundle())
            }
        }

        holder.itemViewBinding.titleTextView.apply {
            // text = item.info.toString()
            if(item.title.isNotEmpty()) {
                // val precomputedTextParams: PrecomputedTextCompat.Params = TextViewCompat.getTextMetricsParams(this)
                // val precomputedText = PrecomputedTextCompat.create(item.blockInfo[0].content, precomputedTextParams)
                // TextViewCompat.setPrecomputedText(this, precomputedText)
                this.setTextFuture(PrecomputedTextCompat.getTextFuture(
                        item.title,
                        TextViewCompat.getTextMetricsParams(
                                this) /* Do not change TextView property after this line */,
                        /*optional custom executor*/ null))
            }
        }

        holder.itemViewBinding.overlineTextView.apply {
            val dateFormat = SimpleDateFormat("hh:mm",
                                              this.context.resources.configuration.locales[0])

            val relativeTime = DateUtils.getRelativeTimeSpanString(item.timeCreated.time, Calendar.getInstance().time.time, DateUtils.HOUR_IN_MILLIS, FORMAT_ABBREV_ALL)
            text = context.getString(R.string.template_dot_2_messages, dateFormat.format(item.timeCreated), relativeTime)
        }

        holder.itemViewBinding.supportingTextView.apply {
            if(item.title.isNotEmpty()) {
                // val precomputedTextParams: PrecomputedTextCompat.Params = TextViewCompat.getTextMetricsParams(this)
                // val precomputedText = PrecomputedTextCompat.create(item.blockInfo[0].content, precomputedTextParams)
                // TextViewCompat.setPrecomputedText(this, precomputedText)

                this.setTextFuture(PrecomputedTextCompat.getTextFuture(
                        item.content.substring(0, min(item.content.length, 400)),
                        TextViewCompat.getTextMetricsParams(
                                this) /* Do not change TextView property after this line */,
                        /*optional custom executor*/ null))
            } else {
                visibility = View.GONE
            }
        }

        holder.itemViewBinding.goodBadView.apply {
            setNumber(item.goodBadScore)
            setIconBackgroundColor(item.color)
        }

        holder.itemViewBinding.subtitleTextView.apply {
            text = item.subtitle
        }

        holder.itemViewBinding.chipGroupView.apply {
            populateTags(item)
        }
    }

    class DairyViewHolder(
            val itemViewBinding: CardNormalDiaryNewBinding) :
            RecyclerView.ViewHolder(itemViewBinding.root)

    private class DiaryDiffCallback :
            DiffUtil.ItemCallback<DiaryEntry>() {
        override fun areItemsTheSame(oldItem: DiaryEntry,
                                     newItem: DiaryEntry): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DiaryEntry,
                                        newItem: DiaryEntry): Boolean {
            return oldItem == newItem
        }
    }
}


