package io.dairyly.dairyly.screens.oobe.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.jakewharton.rxbinding2.widget.RxTextView
import es.dmoral.toasty.Toasty
import io.dairyly.dairyly.R
import io.dairyly.dairyly.utils.isValidEmail
import io.dairyly.dairyly.viewmodels.RegisterViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.buttons_login_register.*
import kotlinx.android.synthetic.main.fragment_register_email.*
import kotlinx.android.synthetic.main.header_login_register.*
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass.
 */
class RegisterEmailFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val registerViewModel = ViewModelProvider(
                this.activity!!).get(
                RegisterViewModel::class.java)

        // Set the page header
        defaultHeaderText.text = getString(
                R.string.register_a_new_account)
        subtitleTextView.text = getString(
                R.string.tell_us_your_email)

        // Set the back button behavior
        signInBackBtn.setOnClickListener { findNavController().popBackStack() }

        registerViewModel.isNewEmailStatusLiveData.observe(viewLifecycleOwner) { isValid ->
            bottomProgressBar.visibility = View.GONE
            if(isValid == null) {
                // Toast.makeText(this@RegisterEmailFragment.activity, "Error Retrieving Status",
                //                Toast.LENGTH_SHORT).show()
                signInContinueBtn.isEnabled = false
                return@observe
            }
            if(!isValid) {
                emailTextField.error = getString(
                        R.string.error_email_invalid_already_taken)
                emailTextField.apply {
                    endIconDrawable = AppCompatResources.getDrawable(
                            emailTextField.context,
                            R.drawable.ic_cancel_black_24dp)
                }
                signInContinueBtn.isEnabled = false
            } else {
                emailTextField.error = null
                // TODO: Show valid message
                emailTextField.apply {
                    endIconDrawable = AppCompatResources.getDrawable(
                            emailTextField.context,
                            R.drawable.ic_check_circle_black_24dp)
                    helperText = context.getString(
                            R.string.email_is_ready)
                }
                signInContinueBtn.isEnabled = true
            }
        }

        val a = RxTextView.textChanges(
                emailEditText)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .debounce(1, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe {
                    // Toasty.info(context!!, "sadas").show()

                    val userEmail = it.toString().trim()
                    if(userEmail.isValidEmail()) {
                        emailTextField.error = null

                        bottomProgressBar.visibility = View.VISIBLE

                        // TODO: Proceed to validate the email with the server
                        registerViewModel.emailLiveData.value = userEmail

                        registerViewModel
                                .validateExistingEmail()
                                .observeOn(AndroidSchedulers.mainThread())
                                .onErrorReturn {throwable ->
                                    Toasty.error(context!!, getString(R.string.error_email_invalid_already_taken)).show()

                                    throwable.printStackTrace()
                                    false
                                }
                                .subscribe { status, throwable ->
                                    when(throwable) {
                                        null -> registerViewModel.isNewEmailStatusLiveData.value = status
                                        else -> registerViewModel.isNewEmailStatusLiveData.value = false
                                    }
                                }
                    }
                }

        emailEditText.doOnTextChanged { text, _, _, _ ->

            // Reset the hint if it is showing that the email is valid
            if(emailTextField.helperText == getString(
                            R.string.email_is_ready)) {
                emailTextField.apply {
                    emailTextField.endIconDrawable = null
                    emailTextField.helperText = getString(
                            R.string.email_login_hint)
                }
            }

            val userEmail = text.toString().trim()
            registerViewModel.emailLiveData.value = userEmail
            registerViewModel.isNewEmailStatusLiveData.value = null

            if(userEmail.isValidEmail()) {
                emailTextField.error = null
            } else {
                if(emailEditText.error == null) {
                    emailTextField.error = context!!.getString(
                            R.string.invalid_email)
                }
            }

        }

        signInContinueBtn.setOnClickListener {
            val action = RegisterEmailFragmentDirections.actionRegisterEmailFragmentToRegisterPasswordFragment()
            findNavController().navigate(action)
        }

        loginTextView.setOnClickListener {
            val action = RegisterEmailFragmentDirections.actionRegisterEmailFragmentToLoginEmailFragment()
            findNavController().navigate(action)
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(
                R.layout.fragment_register_email, container, false)
    }
}