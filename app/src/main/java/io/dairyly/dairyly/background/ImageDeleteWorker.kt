package io.dairyly.dairyly.background

import android.content.Context
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import io.dairyly.dairyly.models.FirebaseStorageRepository
import io.reactivex.Single

/**
 * A background worker that is responsible for issuing and monitoring Firebase file deletion task
 * @constructor default constructor for RxWorker
 */
class ImageDeleteWorker(context: Context, workerParams: WorkerParameters)
    : RxWorker(context, workerParams) {

    companion object {
        /**
         * Key for image ID array value that is being transferred to this Worker
         */
        const val KEY_IMAGE_ID_ARRAY = "ImageID"

        /**
         * Key for storage path string value that is being transferred to this Worker
         */
        const val KEY_STORAGE_PATH = "FirebaseStoragePath"

        /**
         * Key for the Number of Image to be deleted that is being transferred to this Worker
         */
        const val KEY_TOTAL_IMAGES_DELETE = "uploaded"
    }

    /**
     * Create an Observable work that executes this image deletion task in the background.
     * @return Single<Result> observable for the background task
     */
    override fun createWork(): Single<Result> {

        // Getting params from the received key and value pairs
        val storagePath = inputData.getString(KEY_STORAGE_PATH)
        val imageHash = inputData.getStringArray(KEY_IMAGE_ID_ARRAY)!!

        // Create a background task
        return FirebaseStorageRepository
                .deleteDiaryEntryImages(storagePath!!, imageHash.toList())!!
                .toList()
                .map {
                    val outputData = workDataOf(KEY_TOTAL_IMAGES_DELETE to it.size)
                    Result.success(outputData)
                }
    }
}