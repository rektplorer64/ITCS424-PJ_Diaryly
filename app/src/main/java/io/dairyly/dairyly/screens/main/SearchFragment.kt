package io.dairyly.dairyly.screens.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.widget.RxTextView
import io.dairyly.dairyly.R
import io.dairyly.dairyly.models.data.Resource
import io.dairyly.dairyly.ui.recyclerview.DairyRvAdapter
import io.dairyly.dairyly.usecases.UserDiaryUseCase
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_search.*
import java.util.concurrent.TimeUnit

class SearchFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = ViewModelProvider(this)[SearchViewModel::class.java]

        val a = RxTextView.textChanges(searchTextField)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .debounce(2, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe {
                    viewModel.setSearchKeyword(it.toString())
                }

        val rvAdapter = DairyRvAdapter()
        searchRecyclerView.apply {
            setHasFixedSize(true)
            adapter = rvAdapter
            layoutManager = LinearLayoutManager(this.context)
        }

        viewModel.searchResult.observe(viewLifecycleOwner){
            if(it.status == Resource.Status.LOADING){
                shimmerViewContainer.visibility = View.VISIBLE
                searchRecyclerView.visibility = View.GONE
            }else{
                shimmerViewContainer.visibility = View.GONE
                searchRecyclerView.visibility = View.VISIBLE
                rvAdapter.submitList(it.data)
            }
        }
    }
}

class SearchViewModel : ViewModel() {
    private val searchKeyword = MutableLiveData("")

    val searchResult = Transformations.switchMap(searchKeyword){
        LiveDataReactiveStreams.fromPublisher(UserDiaryUseCase.searchDiaryByTitle(it))
    }

    fun setSearchKeyword(searchKeyValue: String) {
        searchKeyword.value = searchKeyValue
    }
}