package io.dairyly.dairyly.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import io.dairyly.dairyly.models.FirebaseUserRepository
import io.reactivex.Single

class LoginViewModel : ViewModel(){

    private val LOG_TAG = this::class.java.simpleName

    val email: MutableLiveData<String?> = MutableLiveData(
            null)
    val password: MutableLiveData<String?> = MutableLiveData(
            null)

    fun loginUserAccount(): Single<FirebaseUser>{
        return FirebaseUserRepository.loginUserAccount(email.value!!, password.value!!)
    }
}