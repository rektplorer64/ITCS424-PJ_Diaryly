package io.dairyly.dairyly.utils

import android.content.Context
import android.util.Log
import androidx.work.*
import io.dairyly.dairyly.background.ImageUploadWorker
import io.dairyly.dairyly.models.data.FirebaseDiaryImage

fun createUploadImageWork(applicationContext: Context,
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
