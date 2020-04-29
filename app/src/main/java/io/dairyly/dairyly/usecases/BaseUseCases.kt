package io.dairyly.dairyly.usecases

import android.annotation.SuppressLint
import io.dairyly.dairyly.models.data.Resource
import io.dairyly.dairyly.models.data.Resource.Status
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.io.PrintWriter
import java.io.StringWriter

class RxUseCaseProcedure<T>(
        private val operation: Flowable<T>,
        private val mapPredicate: ((T) -> T)?) {
    private val LOG_TAG = this::class.java.simpleName

    @SuppressLint("CheckResult")
    fun proceed(): Flowable<Resource<T>> {

        // https://github.com/ReactiveX/RxJava/issues/6214
        val publishSubject = BehaviorSubject.createDefault(
                Resource.loading<T>(null, "loading data..."))

        var a = operation
                // .startWith { publishSubject.onNext(Resource.loading<T>(null, "loading data...")) }
        if(mapPredicate != null) {
            a = a.map(mapPredicate)
        }
        a.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                                // Log.d(LOG_TAG, "RX --> Received an item: $it")
                               publishSubject.onNext(
                                       Resource.success(it, "Values has arrived!"))
                           }, {
                               it.printStackTrace()

                               val stringWriter = StringWriter()
                               it.printStackTrace(PrintWriter(stringWriter))
                               publishSubject.onNext(
                                       Resource.error(
                                               null, stringWriter.toString()))
                               publishSubject.onError(it)
                           }, {
                               publishSubject.onComplete()
                           })
        return publishSubject.toFlowable(BackpressureStrategy.BUFFER)
    }
}

class RxSingleUseCaseProcedure<T>(
        private val operation: Single<T>,
        private val mapPredicate: ((T) -> T)?) {

    @SuppressLint("CheckResult")
    fun proceed(): Single<List<Resource<T>>> {

        val publishSubject = PublishProcessor.create<Resource<T>>()

        var a = operation
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())

        if(mapPredicate != null) {
            a = a.map(mapPredicate)
        }
        a.map {
            Resource.loading(it, null)
        }.subscribe { data, throwable ->
            if(throwable == null) {
                data.status = Status.SUCCESS
                publishSubject.offer(data)
            } else {
                throwable.printStackTrace()

                val stringWriter = StringWriter()
                throwable.printStackTrace(PrintWriter(stringWriter))
                publishSubject.onNext(
                        Resource.error(
                                null, stringWriter.toString()))
            }
            publishSubject.onComplete()
        }

        return publishSubject.toList()
    }
}

class SuspendingUseCaseProcedure<T>(val useCasePredicate: suspend () -> T,
                                    val onError: (java.lang.Exception) -> String?) {

    suspend fun proceed(): Resource<T> {
        var result: T? = null
        val errorMsg: String?
        try {
            result = useCasePredicate()
        } catch(e: Exception) {
            errorMsg = onError(e)
            return Resource.error(result,
                                                                                    errorMsg)
        }
        return Resource.success(result, null)
    }
}


