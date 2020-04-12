package io.dairyly.dairyly.models

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream

object FirebaseStorageRepository {

    private val LOG_TAG = this::class.java.simpleName
    private val firebaseStorage = Firebase.storage("gs://diaryly-52a2c.appspot.com/").reference
    private lateinit var userRoot: StorageReference

    fun uploadDiaryEntryImages(storagePath: String,
                               nameBitmapPairs: List<Pair<String, Bitmap>>?): Flowable<Pair<String, Float>>? {
        val entryFolderRef = userRoot.child(storagePath)

        val asyncList = arrayListOf<Flowable<Pair<String, Float>>>()
        for(pair in nameBitmapPairs!!) {
            val b =
                uploadAnImage(entryFolderRef, "${pair.first}.jpg", pair.second)
            asyncList.add(b)
        }
        return Flowable.merge(asyncList).observeOn(AndroidSchedulers.mainThread())
    }

    @ExperimentalCoroutinesApi
    private suspend fun uploadAnImageSuspended(storageRef: StorageReference, bitmap: Bitmap,
                                               fileName: String) = suspendCancellableCoroutine<Boolean> { cont ->
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

        val data = outputStream.toByteArray()
        storageRef
                .child(fileName)
                .putBytes(data)
                .addOnCompleteListener {
                    cont.resume(true) {
                        it.printStackTrace()
                    }
                }
                .addOnFailureListener {
                    cont.resume(false) {
                        it.printStackTrace()
                    }
                }
    }

    private fun uploadAnImage(storageRef: StorageReference,
                              fileName: String, bitmap: Bitmap): Flowable<Pair<String, Float>> {
        return Flowable
                .create({ flowable ->
                    val outputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

                    val data = outputStream.toByteArray()
                    storageRef
                            .child(fileName)
                            .putBytes(data)
                            .addOnProgressListener {
                                Log.d(LOG_TAG, "Uploading an image ($fileName): ${it.bytesTransferred / it.totalByteCount.toFloat()}...")
                                flowable.onNext(
                                        (Pair(fileName, (it.bytesTransferred / it.totalByteCount).toFloat())))
                            }
                            .addOnCompleteListener {
                                Log.d(LOG_TAG, "Uploading an image ($fileName): COMPLETED!")
                                flowable.onNext(Pair(fileName, 1f))
                                flowable.onComplete()
                            }
                            .addOnFailureListener {
                                it.printStackTrace()
                                flowable.onError(it)
                            }
                }, BackpressureStrategy.BUFFER)
    }

    fun setUserStorageReference(uid: String) {
        userRoot = firebaseStorage.child(uid)
    }

}