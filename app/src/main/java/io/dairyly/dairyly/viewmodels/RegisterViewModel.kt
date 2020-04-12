package io.dairyly.dairyly.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.dairyly.dairyly.models.FirebaseUserRepository
import io.dairyly.dairyly.utils.zipLiveData
import io.reactivex.Single

class RegisterViewModel : ViewModel() {

    companion object {
        const val SHORT_PASSWORD_THRESHOLD = 6
        const val LONG_PASSWORD_THRESHOLD = 30
        const val DEFAULT_EMPTY_FIELD = "signInContinueBtn"
    }

    private val LOG_TAG = this::class.java.simpleName

    enum class PasswordValidity {
        TOO_SHORT, TOO_LONG, INVALID_FORMAT, NULL, VALID_FORMAT
    }

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    val isAlreadyLoggedIn = firebaseAuth.currentUser != null

    val emailLiveData: MutableLiveData<String?> = MutableLiveData(
            null)
    val isNewEmailStatusLiveData: MutableLiveData<Boolean?> = MutableLiveData(
            null)

    val password1: MutableLiveData<String?> = MutableLiveData(
            DEFAULT_EMPTY_FIELD)
    val password1Status: LiveData<PasswordValidity> =
            Transformations.map(password1) { return@map validatePasswordFormat(it) }

    val password2: MutableLiveData<String?> = MutableLiveData(
            DEFAULT_EMPTY_FIELD)
    val password2Status: LiveData<PasswordValidity> =
            Transformations.map(password2) { return@map validatePasswordFormat(it) }

    var account: FirebaseUser? = firebaseAuth.currentUser

    private val arePasswordsIdentical: LiveData<Boolean> by lazy {
        zipLiveData(password1,
                    password2) { pass: String?, confirmPass: String? ->
            Log.d(LOG_TAG, "Text: $pass & $confirmPass")
            if(pass == null || confirmPass == null) {
                return@zipLiveData false
            }
            if(pass == DEFAULT_EMPTY_FIELD || confirmPass == DEFAULT_EMPTY_FIELD) {
                return@zipLiveData false
            }
            pass.trim().contentEquals(confirmPass.trim())
        }
    }

    private val arePasswordsHasCorrectFormat: LiveData<Boolean> by lazy {
        zipLiveData(password1Status,
                    password2Status) { a, b -> a == PasswordValidity.VALID_FORMAT && b == PasswordValidity.VALID_FORMAT }
    }

    val arePasswordsOkayToSignUp: LiveData<Boolean> by lazy {
        zipLiveData(arePasswordsIdentical,
                    arePasswordsHasCorrectFormat) { a, b -> a && b }
    }

    private fun validatePasswordFormat(password: String?): PasswordValidity? {
        if(password == null) {
            return PasswordValidity.NULL
        } else {
            if(password.length < SHORT_PASSWORD_THRESHOLD) {
                return PasswordValidity.TOO_SHORT
            } else if(password.length > LONG_PASSWORD_THRESHOLD) {
                return PasswordValidity.TOO_LONG
            }

            return if(!password.contains(" ")) {
                PasswordValidity.VALID_FORMAT
            } else {
                PasswordValidity.INVALID_FORMAT
            }
        }
    }

    fun validateExistingEmail(): Single<Boolean> {
        return FirebaseUserRepository.validateExistingEmail(emailLiveData.value!!)
        // firebaseAuth.fetchSignInMethodsForEmail(emailLiveData.value!!)
        //         .addOnFailureListener {
        //             isNewEmailStatusLiveData.value = null
        //         }
        //         .addOnCompleteListener { task ->
        //             val isNewUser = task.result!!.signInMethods
        //                     ?.isEmpty()
        //             if(isNewUser!!) {
        //                 Log.d("TAG", "Is New User!")
        //                 isNewEmailStatusLiveData.value = true
        //             } else {
        //                 Log.e("TAG", "Is Old User!")
        //                 isNewEmailStatusLiveData.value = false
        //             }
        //         }
    }

    fun createUser(): Single<FirebaseUser> {
        return FirebaseUserRepository
                .createUserAccount(emailLiveData.value!!, password1.value!!)
        // firebaseAuth.createUserWithEmailAndPassword(emailLiveData.value!!, password1.value!!)
        //         .addOnCompleteListener { task ->
        //             if(task.isSuccessful) {
        //                 val firebaseUser = task.result?.user
        //                 val emailVerified = firebaseUser?.isEmailVerified
        //                 val user = firebaseUser?.displayName
        //                 val uid = firebaseUser?.uid
        //
        //                 // Do something with your data
        //                 if(!emailVerified!!) {
        //                     Log.d(LOG_TAG, "Email Verified!")
        //                     account = firebaseUser
        //                 } //manage your email verification
        //             } else {
        //                 //Manage error
        //             }
        //         }
    }

}