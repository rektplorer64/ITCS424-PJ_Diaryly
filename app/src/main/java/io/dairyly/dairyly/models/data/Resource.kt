package io.dairyly.dairyly.models.data

data class Resource<out T>(
        var status: Status,
        val data: T?,
        var message: String?
) {
    companion object {
        fun <T> success(data: T?, msg: String?): Resource<T> {
            return Resource(
                    Status.SUCCESS, data,
                    msg)
        }

        fun <T> error(data: T?, msg: String?): Resource<T> {
            return Resource(
                    Status.ERROR, data,
                    msg)
        }

        fun <T> loading(data: T?, msg: String?): Resource<T> {
            return Resource(
                    Status.LOADING, data,
                    msg)
        }
    }

    enum class Status {
        SUCCESS, ERROR, LOADING
    }
}


