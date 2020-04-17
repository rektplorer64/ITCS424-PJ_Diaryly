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

class ImageUploadWorker(context: Context, workerParams: WorkerParameters)
    : RxWorker(context, workerParams) {

    companion object {
        const val KEY_IMAGE_ID_ARRAY = "ImageID"
        const val KEY_IMAGE_URI_ARRAY = "ImageUri"
        const val KEY_STORAGE_PATH = "FirebaseStoragePath"
        const val KEY_NO_IMAGE_UPLOADS = "uploaded"
    }

    override fun createWork(): Single<Result> {
        val imageHash = inputData.getStringArray(KEY_IMAGE_ID_ARRAY)!!
        val imageUri = inputData.getStringArray(KEY_IMAGE_URI_ARRAY)!!.map(Uri::parse)

        val storagePath = inputData.getString(KEY_STORAGE_PATH)

        val bitmapArrayList = arrayListOf<Pair<String, Bitmap>>()

        for(index in imageUri.indices) {
            val descriptor = applicationContext.applicationContext!!.contentResolver.openAssetFileDescriptor(
                    imageUri[index], EntryEditFragment.FILE_READ_ONLY)!!
            val bitmap = BitmapFactory.decodeFileDescriptor(descriptor.fileDescriptor, null, null)
            bitmapArrayList.add(Pair(imageHash[index], bitmap))
        }
        return FirebaseStorageRepository
                .uploadDiaryEntryImages(storagePath!!, bitmapArrayList)!!
                .toList()
                .doOnError { it.printStackTrace() }
                .map {
                    val outputData = workDataOf(KEY_NO_IMAGE_UPLOADS to it.size)
                    Result.success(outputData)
                }
    }
}