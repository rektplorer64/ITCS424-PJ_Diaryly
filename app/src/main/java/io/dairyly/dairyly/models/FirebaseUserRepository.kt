package io.dairyly.dairyly.models

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.concurrent.TimeUnit

object FirebaseUserRepository {
    private val LOG_TAG = this::class.java.simpleName

    private val firebaseAuth = FirebaseAuth.getInstance()

    private var firebaseUser = firebaseAuth.currentUser

    fun createUserAccount(email: String, password: String): Single<FirebaseUser> {
        return Flowable
                .create<FirebaseUser>(
                        { flowable ->
                            firebaseAuth
                                    .createUserWithEmailAndPassword(email,
                                                                    password)
                                    .addOnCompleteListener { task ->
                                        if(task.isSuccessful) {
                                            //"Create an Account Successfully"
                                            if(task.result?.user != null) {
                                                flowable.onNext(
                                                        task.result?.user!!)
                                                        .also { flowable.onComplete() }
                                            } else {
                                                if(task.exception == null) {
                                                    flowable.onError(Exception(
                                                            "Couldn't create a new user Account!"))
                                                } else {
                                                    flowable.onError(
                                                            task.exception!!)
                                                }
                                            }
                                        } else {
                                            flowable.onError(task.exception!!)
                                        }
                                    }
                                    .addOnFailureListener {
                                        flowable.onError(it)
                                    }
                        }, BackpressureStrategy.BUFFER).singleOrError()
    }

    fun validateExistingEmail(email: String): Single<Boolean> {
        return Flowable.create<Boolean>({ flowable ->
                                            firebaseAuth.fetchSignInMethodsForEmail(email)
                                                    .addOnFailureListener {
                                                        flowable.onError(it)
                                                    }
                                                    .addOnCompleteListener { task ->
                                                        val isNewUser = task.result!!.signInMethods
                                                                ?.isEmpty()
                                                        if(isNewUser == null && task.exception != null) {
                                                            flowable.onError(task.exception!!)
                                                            return@addOnCompleteListener
                                                        }
                                                        if(isNewUser!!) {
                                                            Log.d(LOG_TAG, "This is a New User!")
                                                            flowable.onNext(isNewUser)
                                                            flowable.onComplete()
                                                        } else {
                                                            Log.e(LOG_TAG, "This is an Old User!")
                                                            flowable.onError(
                                                                    Throwable(
                                                                            "This account is already taken!"))
                                                            return@addOnCompleteListener
                                                        }
                                                    }
                                        }, BackpressureStrategy.BUFFER)
                .timeout(4L, TimeUnit.SECONDS).singleOrError()
    }

    fun loginUserAccount(email: String, password: String): Single<FirebaseUser> {
        return Flowable.create<FirebaseUser>({ flowable ->
                                                 firebaseAuth
                                                         .signInWithEmailAndPassword(email,
                                                                                     password)
                                                         .addOnCompleteListener { task ->
                                                             if(task.isSuccessful) {
                                                                 //"Create an Account Successfully"
                                                                 if(task.result?.user != null) {
                                                                     injectUserToAppRepo()
                                                                     injectUserToStorageRepo()
                                                                     flowable.onNext(
                                                                             task.result?.user!!)
                                                                             .also { flowable.onComplete() }
                                                                 } else {
                                                                     if(task.exception == null) {
                                                                         flowable.onError(Exception(
                                                                                 "Couldn't login your Account!"))
                                                                     } else {
                                                                         flowable.onError(
                                                                                 task.exception!!)
                                                                     }
                                                                 }
                                                             }
                                                         }
                                                         .addOnFailureListener {
                                                             flowable.onError(it)
                                                         }

                                             }, BackpressureStrategy.BUFFER)
                .timeout(4L, TimeUnit.SECONDS)
                .singleOrError()
    }

    fun injectUserToAppRepo(){
        firebaseUser = firebaseAuth.currentUser
        FirebaseAppRepository.setUserDatabaseReference(
                firebaseUser!!.uid)
        Log.d(LOG_TAG, "Injected Firebase User reference to the App Repo")
    }

    fun injectUserToStorageRepo(){
        // firebaseUser = firebaseAuth.currentUser
        FirebaseStorageRepository.setUserStorageReference(
                firebaseUser!!.uid)
        Log.d(LOG_TAG, "Injected Firebase User reference to the Storage Repo")

    }

}