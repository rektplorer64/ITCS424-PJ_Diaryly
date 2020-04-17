package io.dairyly.dairyly.background

import android.content.Context
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import io.dairyly.dairyly.models.FirebaseStorageRepository
import io.reactivex.Single

class ImageDeleteWorker(context: Context, workerParams: WorkerParameters)
    : RxWorker(context, workerParams) {

    companion object {
        const val KEY_IMAGE_ID_ARRAY = "ImageID"
        const val KEY_STORAGE_PATH = "FirebaseStoragePath"
        const val KEY_NO_IMAGE_UPLOADS = "uploaded"
    }

    override fun createWork(): Single<Result> {
        val storagePath = inputData.getString(KEY_STORAGE_PATH)
        val imageHash = inputData.getStringArray(KEY_IMAGE_ID_ARRAY)!!

        return FirebaseStorageRepository
                .deleteDiaryEntryImages(storagePath!!, imageHash.toList())!!
                .toList()
                .map {
                    val outputData = workDataOf(KEY_NO_IMAGE_UPLOADS to it.size)
                    Result.success(outputData)
                }
    }
}