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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import es.dmoral.toasty.Toasty
import io.dairyly.dairyly.R
import io.dairyly.dairyly.models.FirebaseStorageRepository.getImageStorageReference
import io.dairyly.dairyly.models.FirebaseStorageRepository.getProfileImageStorageReference
import io.dairyly.dairyly.models.data.DiaryEntry
import io.dairyly.dairyly.models.data.DiaryImage
import io.dairyly.dairyly.models.data.Resource.Status
import io.dairyly.dairyly.ui.components.RylyTabEntryDelegate
import io.dairyly.dairyly.ui.components.RylyToolbarView
import io.dairyly.dairyly.viewmodels.EntryActivityViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
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

        viewModel.userProfile.observe(viewLifecycleOwner){
            Glide.with(entryTabs.context).load(it.getProfileImageStorageReference()).into(entryTabs.circleImageView)
        }


        viewModel.dailyDiary.observe(this) { list ->

            Log.d(LOG_TAG, "dailyDiary LiveData Emits -> $list")
            if(list.status == Status.ERROR){
                activity!!.finish()
                return@observe
            }

            if(list.status == Status.ERROR && list.data.isNullOrEmpty()) {
                activity!!.finish()
                return@observe
            }

            if(list.status == Status.SUCCESS && list.data.isNullOrEmpty()) {
                activity!!.finish()
                return@observe
            }

            if(list.data.isNullOrEmpty()) {
                return@observe
            }

            Log.d(LOG_TAG, "${list.data.size} entries received for displaying tabs")
            // entryTabs.postDataUpdate(list.data)

            val fragmentAdapter = DiaryEntryViewPagerAdapter(
                    list.data.map(DiaryEntry::id), this)

            if(viewModel.isFirstTime) {
                diaryEntryViewPager.apply {
                    isUserInputEnabled = false
                    adapter = fragmentAdapter

                    TabLayoutMediator(entryTabs.tabLayoutWrapper.calendarTab, this) { tab, _ ->
                        if(viewModel.isFirstTime) {
                            this.setCurrentItem(tab.position, true)
                        }
                    }.attach()

                    offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
                    scrollTab(viewModel.getSelectedDiaryEntryIndex())
                    viewModel.isFirstTime = false
                }
            }
            entryTabs.postDataUpdate(list.data)

            btmAppBar.setNavigationOnClickListener {
                activity!!.onBackPressed()
            }

            btmAppBar.apply {
                menu.findItem(R.id.diaryEntryDelete).setOnMenuItemClickListener {
                    MaterialDialog(context!!).show {

                        title(res = R.string.dialog_delete_entry_title)
                        message(res = R.string.dialog_delete_entry_message)

                        positiveButton(res = R.string.confirm){
                            val diaryEntry = list.data[entryTabs.getSelectedTabIndex()]
                            viewModel
                                    .removeDiaryEntry(diaryEntry)
                                    .subscribeOn(AndroidSchedulers.mainThread())
                                    .subscribe { data, throwable ->
                                        Toasty.success(context, context.getString(R.string.action_delete_successful))
                                        viewModel.isFirstTime = true
                                    }
                        }

                        negativeButton(res = R.string.cancel)
                    }
                    return@setOnMenuItemClickListener true
                }
            }

            editEntryFab.setOnClickListener {
                val diaryEntry = list.data[entryTabs.getSelectedTabIndex()]
                Log.d(LOG_TAG,
                      "Entering the Edit Activity with [EntryId = ${diaryEntry.id}] @ index = ${entryTabs.getSelectedTabIndex()}")

                val action = ShowEntryFragmentDirections.actionShowEntryFragmentToEntryEditActivity2(
                        diaryEntry.id, diaryEntry.timeCreated)
                findNavController().navigate(action)
            }
        }
    }

    private fun scrollTab(index: Int) {
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

class ImageCarouselRvAdapter :
        ListAdapter<DiaryImage, ImageCarouselRvAdapter.ViewHolder>(DiaryImageDiffCallback()) {

    private val LOG_TAG = this::class.java.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_carousel_image, parent, false)
        return ViewHolder(layoutInflater)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)!!
        holder.imageView.apply {
            Log.d(LOG_TAG, "Getting Image from $item")
            if(item.isUploaded) {
                Glide.with(context!!).load(item.getImageStorageReference()).into(this)
            } else {
                Glide.with(context!!).load(item.imageBitmap).into(this)
            }
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

