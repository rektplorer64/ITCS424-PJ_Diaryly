package io.dairyly.dairyly.screens.oobe.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import es.dmoral.toasty.Toasty
import io.dairyly.dairyly.R
import io.dairyly.dairyly.models.FirebaseUserRepository
import io.dairyly.dairyly.viewmodels.LoginViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.buttons_login_register.*
import kotlinx.android.synthetic.main.fragment_login_email.*
import kotlinx.android.synthetic.main.fragment_register_email.emailEditText
import kotlinx.android.synthetic.main.header_login_register.*
import kotlinx.android.synthetic.main.header_login_register.subtitleTextView

/**
 * A simple [Fragment] subclass.
 */
class LoginEmailFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the page header
        defaultHeaderText.text = getString(
                R.string.login_to_diaryly)
        subtitleTextView.text = getString(
                R.string.login_enter_email_password)

        // Set the back button behavior
        signInBackBtn.setOnClickListener { findNavController().popBackStack() }

        val loginViewModel = ViewModelProvider(this).get(
                LoginViewModel::class.java)

        signInContinueBtn.setOnClickListener {
            signInContinueBtn.isEnabled = false
            bottomProgressBar.visibility = View.VISIBLE
            loginViewModel.email.value = emailEditText.text.toString().trim()
            loginViewModel.password.value = passwordEditText.text.toString().trim()

            loginViewModel
                    .loginUserAccount()
                    .observeOn(AndroidSchedulers.mainThread())
                    .onErrorReturn {throwable ->
                        Toasty.error(context!!, throwable.localizedMessage!!).show()
                        throwable.printStackTrace()
                        if(throwable is FirebaseAuthInvalidCredentialsException){
                            emailTextField.error = throwable.localizedMessage
                            passwordTextField.error = null
                        }else if(throwable is FirebaseTooManyRequestsException){
                            emailTextField.error = getString(R.string.login_error_too_many_attempts)
                            passwordTextField.error = getString(R.string.login_error_too_many_attempts)
                        }
                        null
                    }
                    .subscribe { user, _ ->
                        signInContinueBtn.isEnabled = true
                        bottomProgressBar.visibility = View.INVISIBLE
                        if(user == null) {
                            return@subscribe
                        }
                        FirebaseUserRepository.injectUserToAppRepo()
                        FirebaseUserRepository.injectUserToStorageRepo()
                        // The user creation process is finished
                        Toasty.info(context!!, "${getString(R.string.logged_in)}: ${user.email}").show()
                        val action = LoginEmailFragmentDirections.actionLoginEmailFragmentToMainActivity(user.uid)
                        findNavController().navigate(action)
                        activity!!.finish()
                    }
        }

        emailEditText.doOnTextChanged { text, _, _, _ ->
            signInContinueBtn.isEnabled = !(text.isNullOrEmpty() || passwordEditText.text.isNullOrEmpty())
            if(emailTextField.error != null){
                emailTextField.error = null
            }
        }

        passwordEditText.doOnTextChanged { text, _, _, _ ->
            signInContinueBtn.isEnabled = !(text.isNullOrEmpty() || emailEditText.text.isNullOrEmpty())
        }

        signUpTextView.setOnClickListener {
            val action = LoginEmailFragmentDirections.actionLoginEmailFragmentToRegisterEmailFragment()
            findNavController().navigate(action)
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login_email, container, false)
    }


}

