package io.dairyly.dairyly.models

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.dairyly.dairyly.models.data.Profile
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

    fun injectUserToAppRepo() {
        firebaseUser = firebaseAuth.currentUser
        FirebaseAppRepository.setUserDatabaseReference(
                firebaseUser!!.uid)
        Log.d(LOG_TAG, "Injected Firebase User reference to the App Repo")
    }

    fun getUserId(): String? {
        if(firebaseUser == null){
            firebaseUser = firebaseAuth.currentUser
        }
        return firebaseUser?.uid
    }

    fun injectUserToStorageRepo() {
        // firebaseUser = firebaseAuth.currentUser
        FirebaseStorageRepository.setUserStorageReference(
                firebaseUser!!.uid)
        Log.d(LOG_TAG, "Injected Firebase User reference to the Storage Repo")

    }

    @SuppressLint("RestrictedApi")
    fun validateExistingUsername(username: String): Single<Boolean> {
        return Flowable
                .create<Boolean>({ flowable ->
                                     val a = FirebaseDatabase.getInstance()
                                             .reference
                                     Log.d(LOG_TAG, "Getting Data on Ref: ${a.key} @ ${a.path}")
                                             a.root.orderByChild("profile/username")
                                             .equalTo(username)
                                             .addListenerForSingleValueEvent(object : ValueEventListener {
                                                 override fun onCancelled(p0: DatabaseError) {
                                                     TODO("Not yet implemented")
                                                 }

                                                 override fun onDataChange(p0: DataSnapshot) {
                                                     val isNewUser = p0.childrenCount == 0L

                                                     if(p0.exists()) {
                                                         flowable.onError(Throwable("Error!"))
                                                         return
                                                     }
                                                     if(isNewUser) {
                                                         Log.d(LOG_TAG, "This is a New User!")
                                                         flowable.onNext(isNewUser)
                                                         flowable.onComplete()
                                                     } else {
                                                         Log.e(LOG_TAG, "This is an Old User!")
                                                         flowable.onError(Throwable(
                                                                 "This account is already taken!"))
                                                     }
                                                 }
                                             })
                                 }, BackpressureStrategy.BUFFER)
                .timeout(4L, TimeUnit.SECONDS).singleOrError()
    }

    fun Profile.email() = firebaseUser?.email!!

    fun logoutUserAccount() {
        firebaseAuth.signOut()
    }

}