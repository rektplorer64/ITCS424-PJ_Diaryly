package io.dairyly.dairyly.background

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import io.dairyly.dairyly.models.FirebaseStorageRepository
import io.dairyly.dairyly.screens.entry.EntryEditFragment
import io.reactivex.Single

/**
 * A background worker that is responsible for issuing and monitoring Firebase file uploading task
 * @constructor default constructor for RxWorker
 */
class ImageUploadWorker(context: Context, workerParams: WorkerParameters)
    : RxWorker(context, workerParams) {

    companion object {
        /**
         * Key for image ID array value that is being transferred to this Worker
         */
        const val KEY_IMAGE_ID_ARRAY = "ImageID"

        /**
         * Key for image URI array value that is being transferred to this Worker
         */
        const val KEY_IMAGE_URI_ARRAY = "ImageUri"

        /**
         * Key for storage path string value that is being transferred to this Worker
         */
        const val KEY_STORAGE_PATH = "FirebaseStoragePath"

        /**
         * Key for the Number of Image to be uploaded that is being transferred to this Worker
         */
        const val KEY_NO_IMAGE_UPLOADS = "uploaded"
    }

    /**
     * Create an Observable work that executes this image uploading task in the background.
     * @return Single<Result> observable for the background task
     */
    override fun createWork(): Single<Result> {

        // Getting params from the received key and value pairs
        val imageHash = inputData.getStringArray(KEY_IMAGE_ID_ARRAY)!!
        val imageUri = inputData.getStringArray(KEY_IMAGE_URI_ARRAY)!!.map(Uri::parse)

        val storagePath = inputData.getString(KEY_STORAGE_PATH)

        val bitmapArrayList = arrayListOf<Pair<String, Bitmap>>()

        // Iterating through all received Image URI
        for(index in imageUri.indices) {
            // Resolve each URI by reading its descriptor using the application context
            val descriptor = applicationContext.applicationContext!!.contentResolver
                    .openAssetFileDescriptor(
                    imageUri[index], EntryEditFragment.FILE_READ_ONLY)!!

            // Read and decode the bitmap from it
            val bitmap = BitmapFactory.decodeFileDescriptor(descriptor.fileDescriptor, null, null)

            // Add it to the pair of Key received and the bitmap
            bitmapArrayList.add(Pair(imageHash[index], bitmap))
        }

        // Create an image uploading task in the background thread
        return FirebaseStorageRepository
                .uploadDiaryEntryImages(storagePath!!, bitmapArrayList)!!
                .toList()                                   // Pack each Continuous observable (Flowable) into Single
                .doOnError { it.printStackTrace() }         // If error, print some string.
                .map {
                    // Map each pair of success work to a resource so that we can handle it properly when error
                    val outputData = workDataOf(KEY_NO_IMAGE_UPLOADS to it.size)
                    Result.success(outputData)
                }
    }
}