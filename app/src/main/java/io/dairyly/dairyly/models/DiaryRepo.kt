package io.dairyly.dairyly.models

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import io.dairyly.dairyly.data.models.DiaryDateHolder
import io.dairyly.dairyly.models.FirebaseStorageRepository.detachUserStorageReference
import io.dairyly.dairyly.models.data.*
import io.dairyly.dairyly.models.data.FirebaseDiaryImage.Companion.getFirebaseStoragePath
import io.dairyly.dairyly.utils.*
import io.reactivex.*
import org.apache.commons.lang3.time.DateUtils
import java.util.*
import kotlin.collections.HashMap

object DiaryRepo {

    private val LOG_TAG = this::class.java.simpleName

    private val userRoot = FirebaseAppRepository.userRoot
    private val diaryEntryRoot = userRoot.child("diaryEntry")

    fun addNewEntry(context: Context,
                    diaryEntry: DiaryEntry): Single<Boolean> {

        val ref = diaryEntryRoot.push()
        val key = ref.key
        val entryToBeInserted = diaryEntry.toFirebaseData().apply {
            if(!images.isNullOrEmpty()) {
                val hashMap = hashMapOf<String, FirebaseDiaryImage>()
                for(entry in images!!) {
                    entry.value.id = entry.value.hashCode().toString()
                    entry.value.entryId = key!!
                    hashMap[entry.value.id] = entry.value
                }
                images = hashMap
            }
        }

        Log.d(LOG_TAG, "ADDING a new diary entry: $entryToBeInserted and its ${entryToBeInserted.images?.size?: 0} images")

        val flowCallback = FlowableOnSubscribe { flowable: FlowableEmitter<Boolean> ->
            val task = ref.setValue(
                    entryToBeInserted.apply { id = key!!; Log.d(LOG_TAG, "Got a new key ($key)") })

            task.addOnCompleteListener {
                Log.d(LOG_TAG,
                      "Task Finished\nAdding a new diary! (Result = ${it.isSuccessful})\n\t${it}")

                if(key != null && !entryToBeInserted.images.isNullOrEmpty()) {
                    val imageList = entryToBeInserted.images!!.values.toList()
                    Log.d(LOG_TAG, "An UPLOADING task has been initialized for ${imageList.size} images.")
                    createUploadDiaryImageWork(context, getFirebaseStoragePath(entryToBeInserted.id),
                                               imageList)
                }
                flowable.onNext(it.isSuccessful)
                flowable.onComplete()
            }
            .addOnFailureListener {
                Log.e(LOG_TAG, "Task Error!\n${it.stackTrace}")
                flowable.onError(it)
            }
        }

        return Flowable
                .create(flowCallback, BackpressureStrategy.BUFFER)
                .singleOrError()
    }

    fun deleteAnEntry(diaryEntry: DiaryEntry,
                      context: Context): Single<Boolean>{

        Log.d(LOG_TAG, "DELETING a diary entry: ${diaryEntry.id} and its ${diaryEntry.images?.size?: 0} images")

        val flowCallback = FlowableOnSubscribe { flowable: FlowableEmitter<Boolean> ->
            val task = diaryEntryRoot.child(diaryEntry.id).removeValue()

            task.addOnCompleteListener {
                Log.d(LOG_TAG,
                      "Task Finished\nAdding a new diary! (Result = ${it.isSuccessful})\n\t${it}")

                if(!diaryEntry.images.isNullOrEmpty()) {
                    val imageList = diaryEntry.images!!.map(DiaryImage::toFirebaseData).toList()
                    Log.d(LOG_TAG, "An UPLOADING task has been initialized for ${imageList.size} images.")
                    createDiaryImageDeletionWork(context, getFirebaseStoragePath(diaryEntry.id),
                                               imageList)
                }
                flowable.onNext(it.isSuccessful)
                flowable.onComplete()
            }.addOnFailureListener {
                        Log.e(LOG_TAG, "Task Error!\n${it.stackTrace}")
                        flowable.onError(it)
                    }
        }

        return Flowable
                .create(flowCallback, BackpressureStrategy.BUFFER)
                .singleOrError()
    }

    private fun getProfileImagePath(): String {
        return "/profile/"
    }

    fun updateProfileInfo(context: Context, uri: Uri, username: String): Single<List<Boolean>> {
        return updateUsername(username).concatWith(uploadProfileImage(context, uri)).toList()
    }

    private fun updateUsername(username: String): Single<Boolean>{
        val ref = userRoot.child("profile/username")

        val flowCallback = FlowableOnSubscribe { flowable: FlowableEmitter<Boolean> ->
            val task = ref.setValue(username)

            task.addOnCompleteListener {
                if(it.isSuccessful) {
                    flowable.onNext(it.isSuccessful)
                    flowable.onComplete()
                }else{
                    flowable.onNext(!it.isSuccessful)
                    flowable.onError(it.exception!!)
                }
            }.addOnFailureListener {
                Log.e(LOG_TAG, "Task Error!\n${it.stackTrace}")
                flowable.onError(it)
            }
        }

        return Flowable
                .create(flowCallback, BackpressureStrategy.BUFFER)
                .singleOrError()
    }

    private fun uploadProfileImage(context: Context, uri: Uri): Single<Boolean>{
        val ref = userRoot.child("profile/profileImageName")

        val flowCallback = FlowableOnSubscribe { flowable: FlowableEmitter<Boolean> ->

            val profileFilename = uri.hashCode()
            val task = ref.setValue("$profileFilename.jpg")

            task.addOnCompleteListener {
                if(it.isSuccessful) {
                    Log.d(LOG_TAG, "Image Uploading task initializing...")
                    createProfileImageWork(context, getProfileImagePath(), "$profileFilename", uri)
                }

                flowable.onNext(it.isSuccessful)
                flowable.onComplete()
            }.addOnFailureListener {
                Log.e(LOG_TAG, "Task Error!\n${it.stackTrace}")
                flowable.onError(it)
            }
        }

        return Flowable
                .create(flowCallback, BackpressureStrategy.BUFFER)
                .singleOrError()
    }

    fun reactivelyRetrieveAnEntryById(entryId: String, isReactive: Boolean = true): Flowable<DiaryEntry> {
        val flowCallback = FlowableOnSubscribe { flowable: FlowableEmitter<DiaryEntry> ->
            diaryEntryRoot.child(entryId)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(p0: DataSnapshot) {
                            val value = p0.getValue(FirebaseDiaryEntry::class.java)
                            if(value == null) {
                                flowable.onError(Throwable("No data found!"))
                                flowable.onComplete()
                                return
                            }
                            Log.d(LOG_TAG, "Retrieved a new entry -> ${value.id}")
                            flowable.onNext(value.toNormalData())

                            if(!isReactive){
                                flowable.onComplete()
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            // *** Do not call Flowable.onComplete() in onDataChange(); ***
                            // Doing so will make the Flowable not reactive to later change!!
                            // Flowable.onComplete() must be called once!
                            // flowable.onError(error.toException())
                            flowable.onComplete()
                        }
                    })
        }

        return Flowable
                .create(flowCallback, BackpressureStrategy.BUFFER)
    }

    fun retrieveAnEntryById(entryId: String): Single<DiaryEntry> {
        Log.d(LOG_TAG, "Retrieving a Diary Entry by ID = $entryId")
        return reactivelyRetrieveAnEntryById(entryId, false).singleOrError()
    }

    fun retrieveAllEntry(): Flowable<List<DiaryEntry>> {
        return Flowable.create(
                { flowable ->
                    diaryEntryRoot
                            .limitToLast(10)
                            .addValueEventListener(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {
                                    flowable.onComplete()
                                }

                                override fun onDataChange(p0: DataSnapshot) {
                                    val list = arrayListOf<DiaryEntry>()
                                    for(it in p0.children) {
                                        list.add(it.getValue(FirebaseDiaryEntry::class.java)!!
                                                         .toNormalData())
                                    }
                                    flowable.onNext(list)

                                    Log.d(LOG_TAG, "Fetched new entries: $list")
                                    flowable.onComplete()
                                }
                            })
                }, BackpressureStrategy.BUFFER)
    }

    fun reactivelyRetrieveEntriesInTimeRange(time1: Date, time2: Date): Flowable<List<DiaryEntry>> {
        val function: (emitter: FlowableEmitter<List<DiaryEntry>>) -> Unit = { flowable ->
            diaryEntryRoot
                    .orderByChild("timeCreated")
                    .startAt(time1.time.toDouble())
                    .endAt(time2.time.toDouble())
                    .addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {
                            flowable.onComplete()
                        }

                        override fun onDataChange(snapshot: DataSnapshot) {
                            val list = arrayListOf<DiaryEntry>()
                            for(it in snapshot.children) {
                                list.add(it.getValue(FirebaseDiaryEntry::class.java)!!
                                                 .toNormalData())
                            }
                            flowable.onNext(list)
                        }
                    })
        }
        return Flowable.create(
                function, BackpressureStrategy.BUFFER)
    }

    fun retrieveEntryInDay(date: Date): Flowable<List<DiaryEntry>> {
        val day = date.getDayRange()
        return Flowable.create(
                { flowable ->
                    diaryEntryRoot
                            .orderByChild("timeCreated")
                            .startAt(day.first.time.toDouble())
                            .endAt(day.second.time.toDouble())
                            .addValueEventListener(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {
                                    flowable.onComplete()
                                }

                                override fun onDataChange(p0: DataSnapshot) {
                                    val list = arrayListOf<DiaryEntry>()
                                    for(it in p0.children) {
                                        list.add(it.getValue(FirebaseDiaryEntry::class.java)!!
                                                         .toNormalData())
                                    }
                                    Log.d(LOG_TAG, "Fetched new entries: $list")
                                    flowable.onNext(list)
                                }
                            })
                }, BackpressureStrategy.LATEST)
    }

    fun identifyGoodBadScoreListInRange(time1: Date, time2: Date): Flowable<List<DiaryDateHolder>> {
        return reactivelyRetrieveEntriesInTimeRange(time1, time2).map { list ->
            val dateMap = TreeMap<Long, Int>()
            list.forEach {
                val date = DateUtils.truncate(it.timeCreated, Calendar.DATE)
                dateMap.putIfAbsent(date.time, 0)

                if(dateMap[date.time] == null) {
                    return@forEach
                }

                dateMap[date.time] = dateMap[date.time]!! + it.goodBadScore
            }

            val a = arrayListOf<DiaryDateHolder>()
            for(entry in dateMap) {
                a.add(DiaryDateHolder(Date(entry.key), entry.value))
                Log.d(LOG_TAG, "Processing ${a.last()}")
            }
            return@map a
        }
    }

    fun updateAnEntry(context: Context, diaryEntry: DiaryEntry): Single<Boolean>{
        val firebaseDiaryEntry = diaryEntry.toFirebaseData().apply {
            if(!images.isNullOrEmpty()) {
                val hashMap = hashMapOf<String, FirebaseDiaryImage>()
                for(entry in images!!) {
                    if(entry.value.id == DEFAULT_ID) {
                        entry.value.id = entry.value.hashCode().toString()
                        entry.value.entryId = diaryEntry.id
                    }
                    hashMap[entry.value.id] = entry.value
                }
                images = hashMap
            }
        }

        Log.d(LOG_TAG, "UPDATE: Total Image to be Uploaded: ${diaryEntry.images?.size}")

        // List of new images to be uploaded
        val addedImages = firebaseDiaryEntry.images?.filter { !it.value.isUploaded }
        Log.d(LOG_TAG, "UPDATE: Total Image to be Uploaded: ${addedImages?.size}")

        val addedImageMap = addedImages?.values?.toList()

        // List of old images to be deleted
        val targetForRemovalImages = firebaseDiaryEntry.images?.filter { it.value.markedForDeletion }?.values?.toList()

        val serializedDiaryEntry = HashMap(firebaseDiaryEntry.serializeToMap()).apply {
            remove("isUploaded")
            remove("markedForDeletion")
        }

        Log.d(LOG_TAG, "Preparing to UPDATE diary entry: ${firebaseDiaryEntry.id}")
        val ref = diaryEntryRoot.child(firebaseDiaryEntry.id)

        val flowCallback = FlowableOnSubscribe { flowable: FlowableEmitter<Boolean> ->
            val task = ref.updateChildren(serializedDiaryEntry)

            task.addOnCompleteListener {
                Log.d(LOG_TAG,
                      "Task Finished\nUPDATING a diary entry! (Result = ${it.isSuccessful})\n\t${it}")

                if(it.isSuccessful && !addedImageMap.isNullOrEmpty()) {
                    Log.d(LOG_TAG, "Image Uploading task initializing...")
                    createUploadDiaryImageWork(context, getFirebaseStoragePath(firebaseDiaryEntry.id),
                                               addedImageMap)
                }

                // if(it.isSuccessful && !targetForRemovalImages.isNullOrEmpty()) {
                //     Log.d(LOG_TAG, "Image Deletion task initializing...")
                //     createImageDeletionWork(context, getFirebaseStoragePath(firebaseDiaryImage.id),
                //                           targetForRemovalImages)
                // }
                flowable.onNext(it.isSuccessful)
                flowable.onComplete()
            }.addOnFailureListener {
                Log.e(LOG_TAG, "Task Error!\n${it.stackTrace}")
                flowable.onError(it)
            }
        }

        return Flowable
                .create(flowCallback, BackpressureStrategy.BUFFER)
                .singleOrError()
    }

    fun reactivelyRetrieveProfileInfo(isReactive: Boolean = true): Flowable<Profile>{
        val flowCallback = FlowableOnSubscribe { flowable: FlowableEmitter<Profile> ->
            val task = userRoot.child("profile")

                    task.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(p0: DataSnapshot) {
                            val value = p0.getValue(Profile::class.java)
                            // Log.d(LOG_TAG, "Getting user profile... -> ${task.path.wireFormat()}")
                            if(value == null) {
                                flowable.onError(Throwable("No data found!"))
                                flowable.onComplete()
                                return
                            }
                            Log.d(LOG_TAG, "Getting Profile for $value")
                            flowable.onNext(value)

                            if(!isReactive){
                                flowable.onComplete()
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            // *** Do not call Flowable.onComplete() in onDataChange(); ***
                            // Doing so will make the Flowable not reactive to later change!!
                            // Flowable.onComplete() must be called once!
                            // flowable.onError(error.toException())
                            flowable.onComplete()
                        }
                    })
        }

        return Flowable
                .create(flowCallback, BackpressureStrategy.BUFFER)
    }

    fun reactivelyRetrieveEntryByTitle(title: String): Flowable<List<DiaryEntry>> {
        return Flowable.create(
                { flowable ->
                    diaryEntryRoot
                            .orderByChild("title")
                            .startAt(title)
                            .endAt(title + "\uf8ff")
                            .addValueEventListener(object : ValueEventListener {
                                override fun onCancelled(error: DatabaseError) {
                                    flowable.onComplete()
                                }

                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val list = arrayListOf<DiaryEntry>()
                                    for(it in snapshot.children) {
                                        list.add(it.getValue(FirebaseDiaryEntry::class.java)!!
                                                         .toNormalData())
                                    }
                                    flowable.onNext(list)
                                }
                            })
                }, BackpressureStrategy.BUFFER)
    }

    fun logoutUser() {
        Log.d(LOG_TAG, "Logging user out from the app")
        FirebaseUserRepository.logoutUserAccount()
        detachUserStorageReference()
    }
}