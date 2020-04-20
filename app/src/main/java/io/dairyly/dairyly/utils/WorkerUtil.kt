package io.dairyly.dairyly.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.*
import io.dairyly.dairyly.background.ImageDeleteWorker
import io.dairyly.dairyly.background.ImageUploadWorker
import io.dairyly.dairyly.models.data.FirebaseDiaryImage

fun createUploadDiaryImageWork(applicationContext: Context,
                               storagePath: String,
                               images: List<FirebaseDiaryImage>) {
    Log.d("createUploadImageWork",
          "Beginning to Upload ${images.size} images")

    val constraintBuilder = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    val imageUploadWorker = OneTimeWorkRequestBuilder<ImageUploadWorker>()
            .setInputData(workDataOf(
                    ImageUploadWorker.KEY_STORAGE_PATH to storagePath,
                    ImageUploadWorker.KEY_IMAGE_ID_ARRAY to images.map(FirebaseDiaryImage::id).toTypedArray(),
                    ImageUploadWorker.KEY_IMAGE_URI_ARRAY to images.map(FirebaseDiaryImage::uri).toTypedArray()))
            .setConstraints(constraintBuilder)
            .build()
    WorkManager.getInstance(applicationContext).enqueue(imageUploadWorker)
}

fun createDiaryImageDeletionWork(applicationContext: Context,
                                 storagePath: String,
                                 images: List<FirebaseDiaryImage>) {
    Log.d("createImageDeletionWork",
          "Beginning to Delete ${images.size} images")

    val constraintBuilder = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    val imageUploadWorker = OneTimeWorkRequestBuilder<ImageDeleteWorker>()
            .setInputData(workDataOf(
                    ImageUploadWorker.KEY_STORAGE_PATH to storagePath,
                    ImageUploadWorker.KEY_IMAGE_ID_ARRAY to images.map(FirebaseDiaryImage::id).toTypedArray()))
            .setConstraints(constraintBuilder)
            .build()
    WorkManager.getInstance(applicationContext).enqueue(imageUploadWorker)
}

fun createProfileImageWork(applicationContext: Context,
                               storagePath: String, fileName: String,
                               image: Uri) {
    Log.d("createProfileImageWork",
          "Beginning to Upload the profile image")

    val constraintBuilder = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    val imageUploadWorker = OneTimeWorkRequestBuilder<ImageUploadWorker>()
            .setInputData(workDataOf(
                    ImageUploadWorker.KEY_STORAGE_PATH to storagePath,
                    ImageUploadWorker.KEY_IMAGE_ID_ARRAY to arrayOf(fileName),
                    ImageUploadWorker.KEY_IMAGE_URI_ARRAY to arrayOf(image.toString())))
            .setConstraints(constraintBuilder)
            .build()
    WorkManager.getInstance(applicationContext).enqueue(imageUploadWorker)
}

