package io.dairyly.dairyly.usecases

import io.dairyly.dairyly.data.Resource
import io.dairyly.dairyly.data.Status
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class RxUseCaseProcedure<T>(
        private val operation: Flowable<T>,
        private val mapPredicate: ((T) -> T)?) {

    fun proceed(): Flowable<Resource<T>> {

        var a = operation.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())

        if(mapPredicate != null) {
            a = a.map(mapPredicate)
        }

        return a.map {
                    Resource.loading(it, null)
                }
                .doOnError {
                    Resource.error(null,
                                   it.message)
                }
                .map {
                    it.status = Status.SUCCESS
                    it
                }
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


