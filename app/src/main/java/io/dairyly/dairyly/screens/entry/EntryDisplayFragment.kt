package io.dairyly.dairyly.screens.entry

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import io.dairyly.dairyly.R
import io.dairyly.dairyly.models.FirebaseStorageRepository.getImageStorageReference
import io.dairyly.dairyly.models.data.DiaryEntry
import io.dairyly.dairyly.models.data.DiaryImage
import io.dairyly.dairyly.ui.components.RylyTabEntryDelegate
import io.dairyly.dairyly.ui.components.RylyToolbarView
import io.dairyly.dairyly.viewmodels.EntryActivityViewModel
import kotlinx.android.synthetic.main.fragment_show_entry.*
import kotlinx.android.synthetic.main.item_carousel_image.view.*


class ShowEntryFragment : Fragment() {

    private val LOG_TAG = this::class.java.simpleName

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModel = ViewModelProvider(activity!!).get(EntryActivityViewModel::class.java)

        val entryTabs = view.findViewById<RylyToolbarView<DiaryEntry>>(
                R.id.entryTabs)!!.apply {
            behaviorBehaviorDelegate = RylyTabEntryDelegate()
        }

        viewModel.dailyDiary.observe(this){
            if(it.data.isNullOrEmpty()){
                return@observe
            }
            Log.d(LOG_TAG, "${it.data.size} entries received for displaying tabs")
            entryTabs.postDataUpdate(it.data)

            val fragmentAdapter = DiaryEntryViewPagerAdapter(
                    it.data.map(DiaryEntry::id), this)

            diaryEntryViewPager.apply {
                isUserInputEnabled = false
                adapter = fragmentAdapter

                TabLayoutMediator(entryTabs.tabLayoutWrapper.calendarTab, this) { tab, _ ->
                    this.setCurrentItem(tab.position, true)
                }.attach()
                offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT

                if(viewModel.isFirstTime) {
                    scrollTab(viewModel.getSelectedDiaryEntryIndex())
                    viewModel.isFirstTime = false
                }
            }
            entryTabs.postDataUpdate(it.data)
        }
    }

    private fun scrollTab(index: Int){
        diaryEntryViewPager.post {
            Log.d(LOG_TAG, "Scrolling to the ${index}th tab")
            entryTabs.tabLayoutWrapper.scrollToTab(index)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_show_entry, container, false)
    }
}

class DiaryEntryViewPagerAdapter(private val entryIds: List<String>, fragment: Fragment) :
        FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return entryIds.size
    }

    override fun createFragment(position: Int): Fragment {
        return DiaryContentDisplayFragment.newInstance(entryIds[position])
    }
}

class ImageCarouselRvAdapter : ListAdapter<DiaryImage, ImageCarouselRvAdapter.ViewHolder>(DiaryImageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context).inflate(R.layout.item_carousel_image, parent, false)
        return ViewHolder(layoutInflater)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)!!
        holder.imageView.apply {
            Glide.with(context!!).load(item.getImageStorageReference()).into(this)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.carouselImageView
    }

    private class DiaryImageDiffCallback : DiffUtil.ItemCallback<DiaryImage>() {
        override fun areItemsTheSame(oldItem: DiaryImage, newItem: DiaryImage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DiaryImage, newItem: DiaryImage): Boolean {
            return oldItem == newItem
        }
    }
}

