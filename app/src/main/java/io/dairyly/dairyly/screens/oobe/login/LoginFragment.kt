package io.dairyly.dairyly.screens.oobe.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import io.dairyly.dairyly.R
import io.dairyly.dairyly.models.FirebaseUserRepository.injectUserToAppRepo
import io.dairyly.dairyly.models.FirebaseUserRepository.injectUserToStorageRepo
import io.dairyly.dairyly.viewmodels.RegisterViewModel
import kotlinx.android.synthetic.main.fragment_login.view.*

/**
 * A simple [Fragment] subclass.
 */
class LoginFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = ViewModelProvider(this@LoginFragment.activity!!).get(
                RegisterViewModel::class.java)
        if(viewModel.isAlreadyLoggedIn){
            val action = LoginFragmentDirections.actionLoginFragmentToMainActivity(
                    viewModel.account!!.uid)
            injectUserToAppRepo()
            injectUserToStorageRepo()

            findNavController().navigate(action)
            activity!!.finish()
        }

        view.emailLoginBtn.apply {
            setOnClickListener {
                // Toast.makeText(this@LoginFragment.context, "Login Tapped!", Toast.LENGTH_SHORT).show()
                val action = LoginFragmentDirections.actionLoginFragmentToLoginEmailFragment()
                this@LoginFragment.findNavController().navigate(action)
            }
        }

        view.signUpTextView.apply {
            setOnClickListener {
                // Toast.makeText(this@LoginFragment.context, "Sign Up Tapped!", Toast.LENGTH_SHORT).show()
                val action = LoginFragmentDirections.actionLoginFragmentToRegisterEmailFragment()
                this@LoginFragment.findNavController().navigate(action)
            }
        }

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

}
