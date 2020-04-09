package io.dairyly.dairyly.models

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import io.dairyly.dairyly.models.data.DiaryEntry
import io.dairyly.dairyly.models.data.FirebaseDiaryEntry
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single

object DiaryRepo {

    private val LOG_TAG = this::class.java.simpleName

    private val userRoot = FirebaseAppRepository.userRoot
    private val diaryEntryRoot = userRoot.child("diaryEntry")

    fun addNewEntry(diaryEntry: DiaryEntry): Single<Boolean> {
        return Flowable
                .create<Boolean>(
                        { flowable ->
                            val ref = diaryEntryRoot.push()
                            val key = ref.key

                            ref.setValue(diaryEntry.apply {
                                id = key!!
                            }.toFirebaseData())
                                    .addOnCompleteListener {
                                        flowable.onNext(it.isSuccessful)
                                    }.addOnFailureListener {
                                        flowable.onError(it)
                                    }.addOnCompleteListener {
                                        flowable.onComplete()
                                    }
                        }, BackpressureStrategy.BUFFER).singleOrError()
    }

    fun retrieveAllEntry(): Flowable<List<DiaryEntry>> {
        return Flowable.create(
                { flowable ->
                    val ref = diaryEntryRoot.limitToLast(10)
                            .addValueEventListener(object : ValueEventListener{
                                override fun onCancelled(p0: DatabaseError) {
                                    flowable.onComplete()
                                }

                                override fun onDataChange(p0: DataSnapshot) {
                                    val list = arrayListOf<DiaryEntry>()
                                    for(it in p0.children){
                                        list.add(it.getValue(FirebaseDiaryEntry::class.java)!!.toNormalData())
                                    }
                                    flowable.onNext(list)

                                    Log.d(LOG_TAG, "Fetched new entries: $list")
                                    flowable.onComplete()
                                }
                            })
                }, BackpressureStrategy.BUFFER)
    }

    // fun retrieveEntriesInTimeRange(): Flowable<>
}