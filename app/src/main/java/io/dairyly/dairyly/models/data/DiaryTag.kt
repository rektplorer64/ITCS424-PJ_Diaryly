package io.dairyly.dairyly.models.data

data class DiaryTag (
        val title: String = ""
): Comparable<DiaryTag> {
    override fun compareTo(other: DiaryTag): Int {
        return title.compareTo(other.title)
    }

    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(javaClass != other?.javaClass) return false

        other as DiaryTag

        if(title != other.title) return false

        return true
    }

    override fun hashCode(): Int {
        return title.hashCode()
    }


}