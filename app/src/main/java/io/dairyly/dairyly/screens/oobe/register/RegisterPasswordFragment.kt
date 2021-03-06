package io.dairyly.dairyly.screens.oobe.register

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import es.dmoral.toasty.Toasty
import io.dairyly.dairyly.R
import io.dairyly.dairyly.models.FirebaseUserRepository
import io.dairyly.dairyly.viewmodels.RegisterViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.buttons_login_register.*
import kotlinx.android.synthetic.main.fragment_register_password.*
import kotlinx.android.synthetic.main.header_login_register.*


/**
 * A Fragment that allows users to enter a password associated with the entered email.
 *
 * @property LOG_TAG String String Tag string for showing Debugging Log
 */
class RegisterPasswordFragment : Fragment() {

    private val LOG_TAG = this::class.java.simpleName

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val registerViewModel = ViewModelProvider(this.activity!!).get(
                RegisterViewModel::class.java)

        // Set the page header
        defaultHeaderText.text = getString(
                R.string.register_a_new_account)
        subtitleTextView.text = getString(
                R.string.specify_your_password)

        // Observe the email address of the user
        registerViewModel.emailLiveData.observe(viewLifecycleOwner) {
            userEmailText.text = it!!
        }

        // Set the back button behavior
        signInBackBtn.setOnClickListener { findNavController().popBackStack() }

        registerViewModel
                .arePasswordsOkayToSignUp
                .observe(viewLifecycleOwner,
                         object : Observer<Boolean?> {
                             var alreadyDisplayed = false
                             override fun onChanged(
                                     isValid: Boolean?) {
                                 signInContinueBtn.isEnabled = isValid!!

                                 if(isValid) {
                                     if(!alreadyDisplayed) {
                                         alreadyDisplayed = true
                                         Toasty.success(context!!,
                                                        getString(
                                                                R.string.password_valid),
                                                        Toast.LENGTH_SHORT)
                                                 .show()
                                     }
                                 } else {
                                     alreadyDisplayed = false
                                 }
                             }

                         })

        signInContinueBtn.setOnClickListener {
            bottomProgressBar.visibility = View.VISIBLE
            it.isEnabled = !it.isEnabled

            registerViewModel
                    .createUser()
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .doOnError { throwable ->
                        throwable.printStackTrace()
                        Toasty.error(context!!, "Error: ${throwable.localizedMessage}", Toast.LENGTH_LONG).show()

                        // Handles the error that the Firebase Auth server throw!
                        passwordTextField1.error = getString(R.string.register_error_email_badly_formatted)
                        bottomProgressBar.visibility = View.GONE
                    }
                    .subscribe { data, throwable ->

                        if(data != null) {
                            // The user creation process is finished
                            Toasty.info(context!!,
                                        "${getString(R.string.logged_in)}: ${data.email}").show()

                            // Attach user credential to the application repo
                            FirebaseUserRepository.attachUserToFirebaseRepositories()

                            val action = RegisterPasswordFragmentDirections
                                    .actionRegisterPasswordFragmentToProfileCustomizeFragment(
                                            data.uid)
                            findNavController().navigate(action)
                        }

                        if(throwable != null) {
                            Log.e(LOG_TAG, throwable.message ?: "An error happens!")
                        }
                    }
        }

        registerViewModel.password1Status.observe(viewLifecycleOwner) {
            // Log.d(LOG_TAG, "Pass 1's Message: $it")
            passwordTextField1.error = getPasswordErrorMessage(it)
        }
        registerViewModel.password2Status.observe(viewLifecycleOwner) {
            // Log.d(LOG_TAG, "Pass 2's Message: $it")
            passwordTextField2.error = getPasswordErrorMessage(it)
        }

        passwordEditText1.doOnTextChanged { text, _, _, _ ->
            // Log.d(LOG_TAG, "Pass 1 Text Changed: $text")
            registerViewModel.password1.value = text.toString()
        }
        passwordEditText2.doOnTextChanged { text, _, _, _ ->
            // Log.d(LOG_TAG, "Pass 2 Text Changed: $text")
            registerViewModel.password2.value = text.toString()
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(
                R.layout.fragment_register_password, container, false)
    }

    /**
     * Return an associate error message caused by a password error
     * @param validity PasswordValidity the enum for specifying the type
     * @return String? error message string
     */
    private fun getPasswordErrorMessage(validity: RegisterViewModel.PasswordValidity): String? {
        return when(validity) {
            RegisterViewModel.PasswordValidity.INVALID_FORMAT -> getString(
                    R.string.password_error_no_space)
            RegisterViewModel.PasswordValidity.NULL           -> getString(
                    R.string.password_error_null)
            RegisterViewModel.PasswordValidity.TOO_SHORT      -> getString(
                    R.string.password_error_too_short)
            RegisterViewModel.PasswordValidity.TOO_LONG       -> getString(
                    R.string.password_error_too_long)
            RegisterViewModel.PasswordValidity.VALID_FORMAT   -> null
        }
    }
}

