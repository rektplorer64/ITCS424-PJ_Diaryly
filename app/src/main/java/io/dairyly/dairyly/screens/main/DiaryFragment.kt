package io.dairyly.dairyly.screens.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textview.MaterialTextView
import com.mikhaellopez.circularimageview.CircularImageView
import io.dairyly.dairyly.R
import io.dairyly.dairyly.data.models.DiaryDateHolder
import io.dairyly.dairyly.models.DiaryRepo
import io.dairyly.dairyly.models.FirebaseStorageRepository.getProfileImageStorageReference
import io.dairyly.dairyly.models.FirebaseUserRepository.email
import io.dairyly.dairyly.models.data.Profile
import io.dairyly.dairyly.models.data.Resource
import io.dairyly.dairyly.ui.components.RylyTabDateDelegate
import io.dairyly.dairyly.ui.components.RylyToolbarView
import io.dairyly.dairyly.viewmodels.DiaryDateViewModel
import kotlinx.android.synthetic.main.fragment_diary.*
import org.apache.commons.lang3.time.DateUtils
import java.util.*


class DiaryFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_diary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userId = 1
        val viewModel: DiaryDateViewModel = ViewModelProvider(
                this@DiaryFragment.activity!!).get(DiaryDateViewModel::class.java)

        val calendarBar: RylyToolbarView<DiaryDateHolder> = view.findViewById<RylyToolbarView<DiaryDateHolder>>(
                R.id.calendarBar).apply {

            val rylyTabDateDelegate = RylyTabDateDelegate {
                viewModel.today.value = it.time
            }
            behaviorBehaviorDelegate = rylyTabDateDelegate
        }

        // TODO: Make the tab scroll on when the activity start!
        viewModel.dateHolders.observe(this) {
            val fragmentAdapter = DiaryDateViewPagerAdapter(
                    userId, this, it)

            diaryDateViewPager.apply {
                adapter = fragmentAdapter

                TabLayoutMediator(calendarBar.tabLayoutWrapper.calendarTab, this) { tab, _ ->
                    this.setCurrentItem(tab.position, true)
                }.attach()

                offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
            }
            calendarBar.postDataUpdate(it)

            if(viewModel.isFirstLaunch) {
                diaryDateViewPager.currentItem = (it.size / 2) - 1
            }

            addEntryFAB.setOnClickListener {_ ->
                val action = DiaryFragmentDirections.actionDiaryFragmentToEntryEditActivity(it[calendarBar.getSelectedTabIndex()].date)
                findNavController().navigate(action)
            }
        }
        calendarBarLayout.setLiftable(true)

        viewModel.userProfile.observe(viewLifecycleOwner) {profile ->

            if(profile.status == Resource.Status.ERROR){
                return@observe
            }

            val data: Profile = profile.data ?: return@observe

            Glide.with(calendarBar.context).load(data.getProfileImageStorageReference())
                    .placeholder(R.color.colorPrimaryDark)
                    .into(calendarBar.circleImageView)

            calendarBar.setCircleImageButtonListener {
                MaterialDialog(this@DiaryFragment.context!!, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                    customView(R.layout.btm_sheet_profile)

                    val root = getCustomView()
                    root.findViewById<CircularImageView>(R.id.profileImageView).apply {
                        Glide.with(calendarBar.context).load(data.getProfileImageStorageReference())
                                .placeholder(R.color.colorPrimaryDark)
                                .into(this)
                    }

                    root.findViewById<MaterialTextView>(R.id.nameTextView).apply {
                        text = data.username
                    }

                    root.findViewById<MaterialTextView>(R.id.emailTextView).apply {
                        text = data.email()
                    }

                    root.findViewById<FloatingActionButton>(R.id.logoutBtn).setOnClickListener {

                        MaterialDialog(it.context).show {
                            title(text = getString(R.string.dialog_logout_confirmation_ask))
                            message(text = getString(R.string.dialog_logout_confirmation_message))

                            positiveButton(R.string.confirm) {
                                // Log out user from the application!
                                DiaryRepo.logoutUser()
                                // val intent = Intent(context, LoginActivity::class.java)
                                // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                // startActivity(intent)
                                // activity!!.finish()

                                val intent: Intent = activity!!.baseContext.packageManager
                                        .getLaunchIntentForPackage(
                                                activity!!.baseContext.packageName)!!
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                startActivity(intent)

                                activity!!.finish()
                            }

                            negativeButton(R.string.cancel)
                        }

                    }
                }
            }
        }



    }
}

class DiaryDateViewPagerAdapter(val userId: Int, fragment: Fragment,
                                private val dates: List<DiaryDateHolder>) :
        FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return dates.size
    }

    override fun createFragment(position: Int): Fragment {
        return DiaryListFragment
                .newInstance(userId,
                             dates[position].apply {
                                 date = DateUtils.truncate(
                                         this.date,
                                         Calendar.DATE)
                             })
    }

}
