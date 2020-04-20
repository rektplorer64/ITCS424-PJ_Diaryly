package io.dairyly.dairyly.screens.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import io.dairyly.dairyly.R
import io.dairyly.dairyly.data.DairyRepository
import io.dairyly.dairyly.data.Resource.Status
import io.dairyly.dairyly.data.models.DiaryDateHolder
import io.dairyly.dairyly.ui.recyclerview.DairyRvAdapter
import io.dairyly.dairyly.utils.AppViewModelFactory
import io.dairyly.dairyly.viewmodels.DiaryViewModel
import kotlinx.android.synthetic.main.fragment_diary_list.*
import kotlinx.android.synthetic.main.state_empty_diary_list.*

class DiaryListFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_diary_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var dataHolder: DiaryDateHolder?
        var userId: Int?

        arguments!!.apply{
            getSerializable(BUNDLE_DIARY_DATE_HOLDER).let {
                dataHolder = it as DiaryDateHolder
            }
            getInt(BUNDLE_USER_ID).let {
                userId = it
            }
        }

        val viewModelFactory = this.context?.let { DairyRepository.getInstance(it) }?.let {
            AppViewModelFactory()
        }!!

        val viewModel: DiaryViewModel = ViewModelProvider(this@DiaryListFragment, viewModelFactory).get(dataHolder.toString(), DiaryViewModel::class.java)
        viewModel.setDateHolder(dataHolder!!)

        val rvAdapter = DairyRvAdapter(dataHolder!!)
        diaryListRecyclerView.apply {
            setHasFixedSize(true)
            adapter = rvAdapter
            layoutManager = LinearLayoutManager(this.context)
        }



        viewModel.entryListByDateHolder.observe(this){
            // Toast.makeText(this@DiaryListFragment.context, it.status.toString(), Toast.LENGTH_SHORT).show()

            when(it.status) {
                Status.SUCCESS -> {
                    if(!it.data.isNullOrEmpty()) {
                        successState()
                        rvAdapter.submitList(it.data)
                    } else {
                        errorState()
                    }
                }
                Status.LOADING -> {
                    loadingState()
                }
                else           -> {
                    errorState()
                }
            }
        }

        // viewModel.listAllEntries.observe(this){
        //     Log.d("NEW DIARY ENTRY", "Entry $it")
        // }

    }

    private fun successState() {
        emptyStateView.visibility = View.GONE
        shimmerViewContainer.visibility = View.GONE
        shimmerViewContainer.stopShimmer()
        diaryListRecyclerView.visibility = View.VISIBLE
    }

    private fun loadingState() {
        emptyStateView.visibility = View.GONE
        shimmerViewContainer.visibility = View.VISIBLE
        shimmerViewContainer.startShimmer()
        diaryListRecyclerView.visibility = View.GONE
    }

    private fun errorState() {
        emptyStateView.visibility = View.VISIBLE
        shimmerViewContainer.visibility = View.GONE
        shimmerViewContainer.stopShimmer()
        diaryListRecyclerView.visibility = View.GONE

        val imageDrawableInt = arrayListOf(R.drawable.state_empty_list_1,
                                           R.drawable.state_empty_list_2,
                                           R.drawable.state_empty_list_3)
        imageDrawableInt.shuffle()

        Glide.with(this).asBitmap().load("").placeholder(imageDrawableInt[0]).into(emptyStateImage)
    }


    companion object{
        const val BUNDLE_USER_ID = "userId"
        const val BUNDLE_DIARY_DATE_HOLDER = "diaryDateHolder"

        fun newInstance(userId: Int, dateHolder: DiaryDateHolder): DiaryListFragment {
            return DiaryListFragment().apply {
                val bundle = Bundle().apply {
                    putInt(BUNDLE_USER_ID, userId)
                    putSerializable(
                            BUNDLE_DIARY_DATE_HOLDER, dateHolder)
                }
                arguments = bundle
            }
        }
    }
}