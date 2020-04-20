package io.dairyly.dairyly.utils

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.dairyly.dairyly.models.data.DiaryImage
import io.dairyly.dairyly.screens.entry.EntryEditFragment
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

fun ViewModel.suspendinglyLoadBitmapToDiarylyImage(
        application: Application,
        it: List<DiaryImage>): List<DiaryImage> {
    val deferred = arrayListOf<Pair<Uri, Deferred<Bitmap>>>()
    for(image in it) {

        val descriptor = application
                .applicationContext!!
                .contentResolver
                .openAssetFileDescriptor(image.uri,
                                         EntryEditFragment.FILE_READ_ONLY)!!
        viewModelScope.launch {
            deferred.add(Pair(image.uri, async {
                BitmapFactory.decodeFileDescriptor(
                        descriptor.fileDescriptor, null, null)
            }))
        }
        descriptor.close()
    }

    val resultBitmap = arrayListOf<DiaryImage>()
    for(defer in deferred) {
        viewModelScope.launch {
            resultBitmap.add(DiaryImage(
                    imageBitmap = defer.second.await(), uri = defer.first))
        }
    }
    Log.d("suspendinglyLoadBitmapFromImage",
          "Deferred: ${deferred.size} \tLoaded: ${resultBitmap.size} Bitmaps")
    return resultBitmap
}


fun ViewModel.suspendinglyLoadBitmapFromUri(
        application: Application,
        it: List<Uri>): List<Pair<Uri, Bitmap>> {
    val deferred = arrayListOf<Pair<Uri, Deferred<Bitmap>>>()
    for(uri in it) {

        val descriptor = application
                .applicationContext!!
                .contentResolver
                .openAssetFileDescriptor(uri,
                                         EntryEditFragment.FILE_READ_ONLY)!!
        viewModelScope.launch {
            deferred.add(Pair(uri, async {
                BitmapFactory.decodeFileDescriptor(
                        descriptor.fileDescriptor, null, null)
            }))
        }
        descriptor.close()
    }

    val resultBitmap = arrayListOf<Pair<Uri, Bitmap>>()
    for(defer in deferred) {
        viewModelScope.launch {
            resultBitmap.add(Pair(defer.first, defer.second.await()))
        }
    }
    Log.d("suspendinglyLoadBitmapFromUri",
          "Deferred: ${deferred.size} \tLoaded: ${resultBitmap.size} Bitmaps")
    return resultBitmap.toList()
}