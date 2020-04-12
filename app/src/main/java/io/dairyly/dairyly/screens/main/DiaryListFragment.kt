package io.dairyly.dairyly.screens.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import io.dairyly.dairyly.R
import io.dairyly.dairyly.data.DairyRepository
import io.dairyly.dairyly.data.models.DiaryDateHolder
import io.dairyly.dairyly.ui.adapter.DairyRvAdapter
import io.dairyly.dairyly.utils.AppViewModelFactory
import io.dairyly.dairyly.viewmodels.DiaryViewModel
import kotlinx.android.synthetic.main.fragment_diary_list.*

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
            AppViewModelFactory(it, userId!!)
        }!!

        val viewModel: DiaryViewModel = ViewModelProvider(this@DiaryListFragment, viewModelFactory).get(dataHolder.toString(), DiaryViewModel::class.java)
        viewModel.postDateHolder(dataHolder!!)

        val rvAdapter = DairyRvAdapter(dataHolder!!)
        diaryRecyclerView.apply {
            setHasFixedSize(true)
            adapter = rvAdapter
            layoutManager = LinearLayoutManager(this.context)
        }

        viewModel.listEntriesByDateHolder.observe(this){
            // Toast.makeText(this@DiaryListFragment.context, it.status.toString(), Toast.LENGTH_SHORT).show()
            rvAdapter.submitList(it.data)
        }

        viewModel.listAllEntries.observe(this){
            Log.d("NEW DIARY ENTRY", "Entry $it")
        }

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