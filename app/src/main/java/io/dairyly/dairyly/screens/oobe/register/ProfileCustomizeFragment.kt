package io.dairyly.dairyly.screens.oobe.register

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.dairyly.dairyly.R

/**
 * A simple [Fragment] subclass.
 */
class ProfileCustomizeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(
                R.layout.fragment_profile_customize, container, false)
    }

}
