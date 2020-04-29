package io.dairyly.dairyly.screens.oobe.register

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.jakewharton.rxbinding2.widget.RxTextView
import es.dmoral.toasty.Toasty
import io.dairyly.dairyly.R
import io.dairyly.dairyly.models.FirebaseUserRepository
import io.dairyly.dairyly.screens.entry.EntryEditFragment
import io.dairyly.dairyly.utils.CODE_PERMISSION_READ_STORAGE
import io.dairyly.dairyly.utils.invokeImageSelectionIntent
import io.dairyly.dairyly.utils.isGrantedExternalStoragePermission
import io.dairyly.dairyly.utils.isValidUsername
import io.dairyly.dairyly.viewmodels.RegisterViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_profile_customize.*
import kotlinx.android.synthetic.main.header_login_register.*
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass.
 */
class ProfileCustomizeFragment : Fragment() {

    private val LOG_TAG: String = this::class.java.simpleName
    private lateinit var viewModel: RegisterViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        FirebaseUserRepository.injectUserToStorageRepo()
        FirebaseUserRepository.injectUserToAppRepo()

        return inflater.inflate(
                R.layout.fragment_profile_customize, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = ProfileCustomizeFragmentArgs.fromBundle(arguments!!).uid

        viewModel = ViewModelProvider(activity!!).get(RegisterViewModel::class.java)

        // Set the page header
        defaultHeaderText.text = getString(R.string.customize_your_account)
        subtitleTextView.text = getString(R.string.set_your_username_and_image)

        userEmailText.text = viewModel.emailLiveData.value!!

        viewModel.isUsernameAvailableStatusLiveData.observe(viewLifecycleOwner) { isValid ->
            bottomProgressBar.visibility = View.GONE
            if(isValid == null) {
                // Toast.makeText(this@RegisterEmailFragment.activity, "Error Retrieving Status",
                //                Toast.LENGTH_SHORT).show()
                continueBtn.isEnabled = false
                return@observe
            }
            if(!isValid) {
                usernameTextForm.error = getString(
                        R.string.error_email_invalid_already_taken)
            } else {
                usernameTextForm.error = null
                // TODO: Show valid message
                usernameTextForm.apply {
                    endIconDrawable = AppCompatResources.getDrawable(
                            usernameTextForm.context,
                            R.drawable.ic_check_circle_black_24dp)
                    helperText = context.getString(
                            R.string.username_is_ready)
                }
                continueBtn.isEnabled = true
            }
        }

        continueBtn.setOnClickListener {
            if(viewModel.isUsernameAvailableStatusLiveData.value == null) {
                return@setOnClickListener
            }

            if(!viewModel.isUsernameAvailableStatusLiveData.value!!) {
                return@setOnClickListener
            }
            viewModel
                    .saveCustomizationData(context!!)
                    ?.subscribe { data, throwable ->
                        if(data[0] && data[1]) {
                            val action = ProfileCustomizeFragmentDirections.actionProfileCustomizeFragmentToMainActivity(
                                    userId)
                            findNavController().navigate(action)
                        } else {
                            Toasty.error(context!!, "Error getting updating your profile!")
                        }
                    }

        }

        viewModel.profileImageBitmap.observe(viewLifecycleOwner) {
            // Log.d(LOG_TAG, "The URI of the image is updated! -> $it")
            if(it == null) {
                addImageButton.visibility = View.VISIBLE
                return@observe
            }
            addImageButton.visibility = View.GONE
            Toasty.info(context!!, "A profile image selected!").show()
            Glide.with(context!!)
                    .load(it[0].second)
                    .into(heroProfileView)
        }

        val addImageClickListener: (v: View) -> Unit = {
            if(activity!!.isGrantedExternalStoragePermission()) {
                Log.d(LOG_TAG,
                      "Permission is granted after the Image is Clicked! -> Opening Gallery")
                invokeImageSelectionIntent()
            } else {
                requestPermissions(CODE_PERMISSION_READ_STORAGE).also {
                    if(!it){
                        Log.d(LOG_TAG, "Permission is denied after the Image is Clicked!")
                    }
                }
            }
        }
        heroProfileView.setOnClickListener(addImageClickListener)
        addImageButton.setOnClickListener(addImageClickListener)

        val a = RxTextView.textChanges(usernameEditText)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .debounce(1, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe {
                    // Toasty.info(context!!, "sadas").show()
                    val username = it.toString().trim()
                    if(username.isValidUsername()) {
                        usernameTextForm.error = null
                        bottomProgressBar.visibility = View.VISIBLE

                        // TODO: Proceed to validate the email with the server
                        viewModel.emailLiveData.value = username

                        viewModel
                                .validateExistingUsername()
                                .onErrorReturn { throwable ->
                                    Toasty.error(context!!, getString(
                                            R.string.error_email_invalid_already_taken)).show()
                                    throwable.printStackTrace()
                                    false
                                }
                                .subscribeOn(AndroidSchedulers.mainThread())
                                .subscribe { status, throwable ->
                                    when(throwable) {
                                        null -> viewModel.isUsernameAvailableStatusLiveData.value = status
                                        else -> viewModel.isUsernameAvailableStatusLiveData.value = false
                                    }
                                }
                    }
                }

        usernameEditText.doOnTextChanged { text, _, _, _ ->

            // Reset the hint if it is showing that the email is valid
            if(usernameTextForm.helperText == getString(
                            R.string.username_is_ready)) {
                usernameTextForm.apply {
                    usernameTextForm.endIconDrawable = null
                    usernameTextForm.helperText = getString(
                            R.string.username_login_hint)
                }
            }

            val userEmail = text.toString().trim()
            viewModel.username.value = userEmail
            viewModel.isUsernameAvailableStatusLiveData.value = null

            if(userEmail.isValidUsername()) {
                usernameTextForm.error = null
            } else {
                if(usernameEditText.error == null) {
                    usernameTextForm.error = context!!.getString(
                            R.string.invalid_username)
                }
            }

        }
    }

    private fun requestPermissions(permissionCode: Int): Boolean {
        if(permissionCode == CODE_PERMISSION_READ_STORAGE) {
            if(!activity!!.isGrantedExternalStoragePermission()) {
                ActivityCompat.requestPermissions(activity!!,
                                                  arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                                  CODE_PERMISSION_READ_STORAGE)
            } else {
                return true
            }
        } else {
            return false
        }
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        Log.d(LOG_TAG, "Receiving Permission Result (code = $requestCode)")
        when(requestCode) {
            CODE_PERMISSION_READ_STORAGE  -> {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toasty.success(context!!, getString(R.string.permission_thanks_storage)).show()
                    invokeImageSelectionIntent()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(LOG_TAG, "Receiving Activity Result (code = $requestCode)")
        if(resultCode == Activity.RESULT_OK) {
            Log.d(LOG_TAG, "The Result is OKAY!)")
            when(requestCode) {
                EntryEditFragment.REQUEST_CODE_GALLERY -> {
                    Log.d(LOG_TAG, "The Result is related to GALLERY!!")
                    Toasty.info(context!!, "A profile image selected!").show()

                    //data.getData return the content URI for the selected Image
                    val selectedImage: Uri = data?.data ?: return
                    Log.d(LOG_TAG, "The Result contains ${selectedImage}!")
                    viewModel.profileImageUri.value = selectedImage
                }
            }
        }
    }
}
