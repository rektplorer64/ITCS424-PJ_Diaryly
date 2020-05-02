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
import io.reactivex.FlowableEmitter
import io.reactivex.Single
import java.util.concurrent.TimeUnit

/**
 * A SINGLETON OBJECT that contains all methods required for interacting with user
 * account from Firebase Authentication
 */
object FirebaseUserRepository {
    private val LOG_TAG = this::class.java.simpleName

    private val firebaseAuth = FirebaseAuth.getInstance()

    private var firebaseUser = firebaseAuth.currentUser

    fun createUserAccount(email: String, password: String): Single<FirebaseUser> {
        val function: (emitter: FlowableEmitter<FirebaseUser>) -> Unit = { flowable ->
            firebaseAuth
                    .createUserWithEmailAndPassword(email,
                                                    password)
                    .addOnCompleteListener { task ->
                        if(task.isSuccessful) {
                            //"Create an Account Successfully"
                            if(task.result?.user != null) {
                                firebaseUser = task.result?.user!!
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
        }
        return Flowable
                .create<FirebaseUser>(
                        function, BackpressureStrategy.BUFFER).singleOrError()
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

                                                                     // Attach user credential to the application repo
                                                                     // USER IS LOGGED IN TO THIS APP!
                                                                     attachUserToFirebaseRepositories()

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

    /**
     * Attach user to the application database on Firebase
     *
     * Set the database reference to be for this user.
     */
    private fun attachUserToDatabaseRepo() {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        FirebaseAppRepository.setUserDatabaseReference(firebaseUser!!.uid)
        Log.d(LOG_TAG, "Injected Firebase User reference to the App Repo -> ${firebaseUser!!.uid}")
    }

    /**
     * Attach user to the application file storage on Firebase
     *
     * Set the storage reference to be for this user.
     */
    private fun attachUserToStorageRepo() {
        FirebaseStorageRepository.setUserStorageReference(
                FirebaseAuth.getInstance().currentUser!!.uid)
        Log.d(LOG_TAG, "Injected Firebase User reference to the Storage Repo -> ${firebaseUser!!.uid}")
    }

    /**
     * Attach the user to Firebase Database and File Storage
     * It is similar to logging the user in to the app.
     */
    fun attachUserToFirebaseRepositories(){
        attachUserToDatabaseRepo()
        attachUserToStorageRepo()
    }

    fun getUserId(): String? {
        if(firebaseUser == null){
            firebaseUser = firebaseAuth.currentUser
        }
        return firebaseUser?.uid
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
        Log.d(LOG_TAG, "Logging out user -> uid={${firebaseAuth.currentUser!!.uid}} from Firebase Auth")
        firebaseAuth.signOut()
        FirebaseStorageRepository.detachUserStorageReference()
        Log.d(LOG_TAG, "Logout result: ${firebaseAuth.currentUser == null}")
        firebaseUser = null
    }

}