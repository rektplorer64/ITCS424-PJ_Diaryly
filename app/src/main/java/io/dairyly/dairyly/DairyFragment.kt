package io.dairyly.dairyly

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import io.dairyly.dairyly.data.DairyRepository
import io.dairyly.dairyly.ui.adapter.DairyRvAdapter
import io.dairyly.dairyly.viewmodels.DiaryViewModel
import kotlinx.android.synthetic.main.fragment_dairy.*

class DairyFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dairy, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val repo = this.context?.let { DairyRepository(it) }!!
        val viewModel: DiaryViewModel by viewModels{
            object : ViewModelProvider.Factory{
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return if(modelClass == DiaryViewModel::class.java){
                        DiaryViewModel(repo, 1) as T
                    }else{
                        DiaryViewModel(repo, 1) as T
                    }
                }
            }
        }

        val rvAdapter = DairyRvAdapter()
        diaryRecyclerView.apply {
            setHasFixedSize(true)
            adapter = rvAdapter
            layoutManager = LinearLayoutManager(this.context)
        }
        viewModel.listAllDiaryEntries.observe(this){
            rvAdapter.submitList(it.data)
        }
    }
}