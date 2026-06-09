package com.drew654.mocklocations.domain.model

data class CompassState(
    val bearing: () -> Float = { 0f },
    val tilt: () -> Float = { 0f }
) {
    val isVisible: () -> Boolean = {
        bearing() != 0f || tilt() != 0f
    }
}
