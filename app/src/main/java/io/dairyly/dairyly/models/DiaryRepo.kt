package io.dairyly.dairyly.models

import android.content.Context
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import io.dairyly.dairyly.data.models.DiaryDateHolder
import io.dairyly.dairyly.models.data.DiaryEntry
import io.dairyly.dairyly.models.data.FirebaseDiaryEntry
import io.dairyly.dairyly.models.data.FirebaseDiaryImage
import io.dairyly.dairyly.models.data.FirebaseDiaryImage.Companion.getFirebaseStoragePath
import io.dairyly.dairyly.utils.createUploadImageWork
import io.dairyly.dairyly.utils.getDayRange
import io.reactivex.*
import org.apache.commons.lang3.time.DateUtils
import java.util.*

object DiaryRepo {

    private val LOG_TAG = this::class.java.simpleName

    private val userRoot = FirebaseAppRepository.userRoot
    private val diaryEntryRoot = userRoot.child("diaryEntry")

    fun addNewEntry(context: Context,
                    diaryEntry: DiaryEntry): Single<Boolean> {

        val entryToBeInserted = diaryEntry.toFirebaseData().apply {
            if(!images.isNullOrEmpty()) {
                val hashMap = hashMapOf<String, FirebaseDiaryImage>()
                for(entry in images!!) {
                    entry.value.id = entry.value.hashCode().toString()
                    hashMap[entry.value.id] = entry.value
                }
                images = hashMap
            }
        }

        Log.d(LOG_TAG, "Adding a new diary entry: $entryToBeInserted")

        val flowCallback = FlowableOnSubscribe { flowable: FlowableEmitter<Boolean> ->
            val ref = diaryEntryRoot.push()
            val key = ref.key
            val task = ref.setValue(
                    entryToBeInserted.apply { id = key!!; Log.d(LOG_TAG, "Got a new key ($key)") })

            task.addOnCompleteListener {
                Log.d(LOG_TAG,
                      "Task Finished\nAdding a new diary! (Result = ${it.isSuccessful})\n\t${it}")

                if(key != null && !entryToBeInserted.images.isNullOrEmpty()) {
                    Log.d(LOG_TAG, "Image Uploading task initializing...")
                    createUploadImageWork(context, getFirebaseStoragePath(entryToBeInserted.id),
                                          entryToBeInserted.images!!.values.toList())
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

    fun reactivelyRetrieveAnEntryById(entry: String): Flowable<DiaryEntry> {
        val flowCallback = FlowableOnSubscribe { flowable: FlowableEmitter<DiaryEntry> ->
            diaryEntryRoot.child(entry)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(p0: DataSnapshot) {
                            val value = p0.getValue(FirebaseDiaryEntry::class.java)
                            if(value == null) {
                                flowable.onError(Throwable("No data found!"))
                                return
                            }
                            Log.d(LOG_TAG, "Retrieved a new entry -> ${value.id}")
                            flowable.onNext(value.toNormalData())
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

    fun retrieveAnEntryById(entry: String): Single<DiaryEntry> {
        return reactivelyRetrieveAnEntryById(entry).singleOrError()
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

    fun retrieveEntriesInTimeRange(time1: Date, time2: Date): Flowable<List<DiaryEntry>> {
        return Flowable.create(
                { flowable ->
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

                                    Log.d(LOG_TAG, "Fetched new entries: $list")
                                    flowable.onComplete()
                                }
                            })
                }, BackpressureStrategy.BUFFER)
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
                                    flowable.onNext(list)

                                    Log.d(LOG_TAG, "Fetched new entries: $list")
                                    flowable.onComplete()
                                }
                            })
                }, BackpressureStrategy.BUFFER)
    }

    fun identifyGoodBadScoreListInRange(time1: Date, time2: Date): Flowable<List<DiaryDateHolder>> {
        return retrieveEntriesInTimeRange(time1, time2).map { list ->
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
}