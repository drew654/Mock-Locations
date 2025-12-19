package com.drew654.mocklocations.domain.model

data class Coordinates(
    val latitude: Double,
    val longitude: Double
) {
    init {
        require(latitude in -90.0..90.0) { "Latitude must be between -90 and 90" }
        require(longitude in -180.0..180.0) { "Longitude must be between -180 and 180" }
    }

    override fun toString(): String {
        fun format(value: Double): String {
            return "%.7f".format(value).trimEnd('0').trimEnd('.')
        }
        return "(${format(latitude)}, ${format(longitude)})"
    }
}
