package com.infostride.virtualtryon.domain.model

data class Outfit(val id: Int, val category: String, val image: ByteArray) { //end class
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Outfit

        if (id != other.id) return false
        if (category != other.category) return false
        if (!image.contentEquals(other.image)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + category.hashCode()
        result = 31 * result + image.contentHashCode()
        return result
    }
}
