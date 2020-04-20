package io.dairyly.dairyly.screens.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.NavigationUI.onNavDestinationSelected
import io.dairyly.dairyly.R
import io.dairyly.dairyly.utils.viewModelInjectionHelper
import io.dairyly.dairyly.viewmodels.DiaryDateViewModel
import io.dairyly.dairyly.viewmodels.DiaryViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModelInjectionHelper<DiaryViewModel>(this)
        viewModelInjectionHelper<DiaryDateViewModel>(this)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.mainNavHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        NavigationUI.setupWithNavController(bottomNavigation, navController)

        bottomNavigation.setOnNavigationItemSelectedListener {menuItem ->
            // if(menuItem.itemId != R.id.diaryFragment
            //    || menuItem.itemId != R.id.searchFragment){
            //     return@setOnNavigationItemSelectedListener false
            // }
            onNavDestinationSelected(menuItem, navController)
            return@setOnNavigationItemSelectedListener true
        }
    }
}
