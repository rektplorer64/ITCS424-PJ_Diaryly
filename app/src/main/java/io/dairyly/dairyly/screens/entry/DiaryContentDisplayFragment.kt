package io.dairyly.dairyly.screens.entry

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import io.dairyly.dairyly.R
import io.dairyly.dairyly.models.data.Resource
import io.dairyly.dairyly.models.data.DEFAULT_ID
import io.dairyly.dairyly.models.data.DiaryEntry
import io.dairyly.dairyly.utils.getAreaNameByCoordinate
import io.dairyly.dairyly.utils.openCoordinateInGoogleMap
import io.dairyly.dairyly.utils.populateTags
import io.dairyly.dairyly.utils.viewModelInjectionHelper
import io.dairyly.dairyly.viewmodels.EntryDisplayViewModel
import io.dairyly.dairyly.viewmodels.EntryEditorViewModel.Companion.EMPTY_LOCATION
import kotlinx.android.synthetic.main.fragment_entry_content_display.*

class DiaryContentDisplayFragment : Fragment(), Observer<Resource<DiaryEntry>> {

    private val LOG_TAG = this::class.java.simpleName
    private lateinit var imageAdapter: ImageCarouselRvAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // viewGroup.getLayoutTransition().setAnimateParentHierarchy(false)
        return inflater.inflate(R.layout.fragment_entry_content_display, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var entryId = DEFAULT_ID
        arguments!!.apply{
            getString(BUNDLE_DIARY_ENTRY_ID).let {
                entryId = it!!
            }
        }

        imageAdapter = ImageCarouselRvAdapter()
        imageCarousel.apply {
            adapter = imageAdapter
            clipToPadding = false
            imageCarouselIndicator.setViewPager(this)
        }
        imageAdapter.registerAdapterDataObserver(imageCarouselIndicator.adapterDataObserver)

        // TODO -> Implement this fragment to display contents!
        val viewModel = viewModelInjectionHelper<EntryDisplayViewModel>(this,
                                                                        diaryEntryId = entryId)
        viewModel.entryContent.observe(viewLifecycleOwner, this)
    }

    override fun onChanged(it: Resource<DiaryEntry>?) {
        if(it?.data == null){
            return
        }
        val data = it.data

        Log.d(LOG_TAG, "Showing Diary: $it")

        entryContentTextView.apply {
            text = data.content
        }

        if(!data.images.isNullOrEmpty()) {
            imageAdapter.submitList(data.images)
        } else {
            imageCarousel.visibility = View.GONE
        }

        tagChipGroup.populateTags(data)

        goodBadView.apply {
            setNumber(data.goodBadScore)
            setIconBackgroundColor(data.color)
        }

        if(data.location != EMPTY_LOCATION) {
            locationIndicatorLayout.setOnClickListener {
                context!!.openCoordinateInGoogleMap(data.location)
            }

            context!!.getAreaNameByCoordinate(data.location).apply {
                locationNameTextView.text = first
                locationCoordinateTextView.text = second
            }
        }else{
            locationIndicatorLayout.visibility = View.GONE
        }
    }

    companion object{
        const val BUNDLE_DIARY_ENTRY_ID = "entryID"
        fun newInstance(entryId: String): DiaryContentDisplayFragment {
            return DiaryContentDisplayFragment().apply {
                val bundle = Bundle().apply {
                    putString(BUNDLE_DIARY_ENTRY_ID, entryId)
                }
                arguments = bundle
            }
        }

    }
}