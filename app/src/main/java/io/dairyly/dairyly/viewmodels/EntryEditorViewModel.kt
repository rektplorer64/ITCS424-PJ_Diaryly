package io.dairyly.dairyly.viewmodels

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import io.dairyly.dairyly.R
import io.dairyly.dairyly.models.DiaryRepo
import io.dairyly.dairyly.models.data.DEFAULT_COLOR
import io.dairyly.dairyly.models.data.DiaryEntry
import io.dairyly.dairyly.models.data.DiaryImage
import io.dairyly.dairyly.models.data.DiaryTag
import io.dairyly.dairyly.screens.entry.EntryEditFragment
import io.dairyly.dairyly.usecases.RxSingleUseCaseProcedure
import io.dairyly.dairyly.utils.TIME_FORMATTER_FULL
import io.dairyly.dairyly.utils.zipLiveData
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class EntryEditorViewModel(application: Application, private val editorEntryId: String? = null) :
        AndroidViewModel(application) {
    companion object {
        val EMPTY_LOCATION = Pair(0.0, 0.0)
    }

    var SESSION_DEFAULT_LOCATION = Pair(0.0, 0.0)

    val isModification = editorEntryId != null
    var initializationCompleted = false
        private set

    // private val compositeDisposable = CompositeDisposable()

    private val LOG_TAG = this::class.java.simpleName

    val title = MutableLiveData("")

    val subtitle = MutableLiveData("")
    val content = MutableLiveData("")
    val color = MutableLiveData<Int?>(null)

    val goodBad = MutableLiveData(0)
    val date = MutableLiveData(
            Calendar.getInstance().time)

    val dateText = Transformations.map(date) {
        TIME_FORMATTER_FULL.format(it)
    }

    private val newImages = MutableLiveData<ArrayList<DiaryImage>>(arrayListOf())

    private var oldImages: MutableLiveData<List<DiaryImage>>? = MutableLiveData(arrayListOf())

    val coordinate = MutableLiveData(EMPTY_LOCATION)

    private val newDiaryImages: LiveData<List<DiaryImage>?> = Transformations.switchMap(newImages) {
        Log.d(LOG_TAG, "imageBitMap LiveData invoked")
        it ?: return@switchMap liveData<List<DiaryImage>?> {
            emit(null)
        }

        Log.d(LOG_TAG, "Loading Images...")

        val deferred = arrayListOf<Pair<Uri, Deferred<Bitmap>>>()
        for(image in it) {

            val descriptor = application
                    .applicationContext!!
                    .contentResolver
                    .openAssetFileDescriptor(image.uri, EntryEditFragment.FILE_READ_ONLY)!!
            viewModelScope.launch {
                deferred.add(Pair(image.uri, async {
                    BitmapFactory.decodeFileDescriptor(descriptor.fileDescriptor, null, null)
                }))
            }
            descriptor.close()
        }

        val resultBitmap = arrayListOf<DiaryImage>()
        for(defer in deferred) {
            viewModelScope.launch {
                resultBitmap.add(DiaryImage(imageBitmap = defer.second.await(), uri = defer.first))
            }
        }
        Log.d(LOG_TAG, "Deferred: ${deferred.size} \tLoaded: ${resultBitmap.size} Bitmaps")

        return@switchMap liveData<List<DiaryImage>?> {
            if(!resultBitmap.isNullOrEmpty()) {
                emit(ArrayList(resultBitmap))
            } else {
                emit(arrayListOf())
            }
        }
    }

    val tagArray = MutableLiveData(TreeSet<DiaryTag>())

    init {
        Log.d(LOG_TAG, "Initializing...")
        if(editorEntryId != null && editorEntryId != application.getString(
                        R.string.debug_default_id)) {
            Log.d(LOG_TAG, "Entering Diary Entry Edit Mode... for $editorEntryId")
            val rx = RxSingleUseCaseProcedure(
                    DiaryRepo.retrieveAnEntryById(editorEntryId), null)
                    .proceed()
                    .subscribe { data, throwable ->
                        Log.d(LOG_TAG, "The result has arrived!")
                        if(data.isNullOrEmpty() || throwable != null) {
                            Log.d(LOG_TAG, "We got an error getting data from the remote source!")
                            return@subscribe
                        }
                        val e = data[0].data ?: return@subscribe

                        Log.d(LOG_TAG, "The Editor initialization has begin!")

                        title.value = e.title
                        subtitle.value = e.subtitle
                        content.value = e.content
                        color.value = e.color
                        goodBad.value = e.goodBadScore
                        date.value = e.timeCreated
                        oldImages!!.value = e.images
                        tagArray.value = TreeSet(e.tags)

                        coordinate.value = e.location

                        initializationCompleted = true

                        Log.d(LOG_TAG, "Final Result\n${title.value!!}\n${subtitle.value!!}\n" +
                                       content.value!!)
                        Log.d(LOG_TAG, "The Editor has been successfully initialized!")
                    }

            // compositeDisposable.add(rx)
        } else {
            oldImages = MutableLiveData(arrayListOf())
        }
    }

    // val allImages: LiveData<ArrayList<DiaryImage>?> = concat(
    //         oldImages as LiveData<ArrayList<DiaryImage>?>,
    //         newDiaryImages as LiveData<ArrayList<DiaryImage>?>)
    val allImages: LiveData<ArrayList<DiaryImage>> = zipLiveData(oldImages as LiveData<ArrayList<DiaryImage>>
                                                                 , newDiaryImages as LiveData<ArrayList<DiaryImage>>){ a, b ->
        val full = ArrayList<DiaryImage>(a)
        full.addAll(b)

        return@zipLiveData full
    }

    // override fun onCleared() {
    //     compositeDisposable.clear()
    // }

    fun saveData(): Single<Boolean> {
        val c = Calendar.getInstance()
        val tags = tagArray
        val diaryImage = newImages.value
        val color = color.value ?: DEFAULT_COLOR

        var createdTime = c.time
        var id = "-1"
        if(isModification) {
            createdTime = date.value!!
            id = editorEntryId!!
        }

        Log.d(LOG_TAG, "Saving data...\n\t\t${title.value!!}\n\t\tAnd ${newImages.value!!.size} images")

        val entry = DiaryEntry(id, title.value!!,
                               subtitle.value!!, content.value!!,
                               tags.value?.toList() ?: listOf(),
                               goodBad.value!!,
                               color,
                               createdTime,
                               c.time,
                               diaryImage,
                               coordinate.value!!.first,
                               coordinate.value!!.second)
        return DiaryRepo.addNewEntry(getApplication(), entry)
                .observeOn(
                        AndroidSchedulers.mainThread())
    }

    fun updateData(): Single<Boolean> {
        val c = Calendar.getInstance()
        val tags = tagArray

        val diaryImage = newImages.value?.apply {
            if(!oldImages?.value.isNullOrEmpty()) {
                addAll(oldImages?.value!!)
            }
        }

        val color = color.value ?: DEFAULT_COLOR

        var createdTime = c.time
        var id = "-1"
        if(isModification) {
            createdTime = date.value!!
            id = editorEntryId!!
        }

        Log.d(LOG_TAG, "Updating data...")

        val entry = DiaryEntry(id, title.value!!,
                               subtitle.value!!, content.value!!,
                               tags.value?.toList() ?: listOf(),
                               goodBad.value!!,
                               color,
                               createdTime,
                               c.time,
                               diaryImage,
                               coordinate.value!!.first,
                               coordinate.value!!.second)
        return DiaryRepo.updateAnEntry(getApplication(), entry)
                .observeOn(
                        AndroidSchedulers.mainThread())
    }

    fun addAnImage(img: Uri) {
        val newImageArray = if(newImages.value == null) {
            arrayListOf()
        } else {
            newImages.value
        }

        newImageArray!!.add(DiaryImage(uri = img))
        newImages.value = newImageArray
        Log.d(LOG_TAG, "Adding a new Image; Now the array has ${newImages.value!!.size} elements.")
    }

    fun addDiaryTag(tagString: String) {
        val old = tagArray.value!!
        old.add(DiaryTag(tagString))
        tagArray.value = old
    }

    fun removeDiaryTag(tagString: String) {
        val old = tagArray.value!!
        for(tag in old) {
            if(tag.title == tagString) {
                old.remove(tag)
                break
            }
        }

        Log.d(LOG_TAG, "Removing: $old")
        tagArray.value = old
    }
}
