package io.dairyly.dairyly.usecases

import android.annotation.SuppressLint
import io.dairyly.dairyly.data.Resource
import io.dairyly.dairyly.data.Status
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import java.io.PrintWriter
import java.io.StringWriter

class RxUseCaseProcedure<T>(
        private val operation: Flowable<T>,
        private val mapPredicate: ((T) -> T)?) {

    @SuppressLint("CheckResult")
    fun proceed(): Flowable<Resource<T>> {

        val publishSubject = PublishProcessor.create<Resource<T>>()

        var a = operation
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())

        if(mapPredicate != null) {
            a = a.map(mapPredicate)
        }

        a.map { Resource.loading(it, null) }
                .subscribe({
                               it.status = Status.SUCCESS
                               publishSubject.offer(it)
                           }, {
                                it.printStackTrace()

                                val stringWriter = StringWriter()
                                it.printStackTrace(PrintWriter(stringWriter))
                                publishSubject.onNext(
                                        Resource.error(
                                                null, stringWriter.toString()))
                           }, {
                                publishSubject.onComplete()
                           })
        return publishSubject.toObservable().toFlowable(BackpressureStrategy.BUFFER)
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


