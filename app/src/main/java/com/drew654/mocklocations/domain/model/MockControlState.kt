package com.drew654.mocklocations.domain.model

data class MockControlState(
    val isMocking: Boolean = false,
    val isPaused: Boolean = false,
    val isWaitingAtEndOfRoute: Boolean = false,
    val activeLocationTarget: LocationTarget = LocationTarget.Empty,
    val isUsingCrosshairs: Boolean = true,
    val isWaitingForRouteFetch: Boolean = false
)

fun MockControlState.isStartVisible(): Boolean {
    return !isMocking
}

fun MockControlState.isStartEnabled(): Boolean {
    if ((activeLocationTarget is LocationTarget.Empty && !isUsingCrosshairs) || isWaitingForRouteFetch) {
        return false
    }
    return isStartVisible()
}

fun MockControlState.isStopVisible(): Boolean {
    return isMocking
}

fun MockControlState.isStopEnabled(): Boolean {
    return isStopVisible()
}

fun MockControlState.isPauseVisible(): Boolean {
    return isMocking && activeLocationTarget.isRoute() && !isPaused && !isWaitingAtEndOfRoute
}

fun MockControlState.isPauseEnabled(): Boolean {
    return isPauseVisible()
}

fun MockControlState.isResumeVisible(): Boolean {
    return isMocking && activeLocationTarget.isRoute() && isPaused && !isWaitingAtEndOfRoute
}

fun MockControlState.isResumeEnabled(): Boolean {
    return isResumeVisible()
}

fun MockControlState.isAddPointVisible(): Boolean {
    return isUsingCrosshairs && !isMocking
}

fun MockControlState.isAddPointEnabled(): Boolean {
    return isAddPointVisible() && !isWaitingForRouteFetch
}

fun MockControlState.isLongPressAddPointEnabled(): Boolean {
    return !isMocking && !isWaitingForRouteFetch
}

fun isPopPointVisible(): Boolean {
    return true
}

fun MockControlState.isPopPointEnabled(): Boolean {
    if (activeLocationTarget is LocationTarget.Empty || isMocking || isWaitingForRouteFetch) {
        return false
    }
    return isPopPointVisible()
}

fun isClearLocationTargetVisible(): Boolean {
    return true
}

fun MockControlState.isClearLocationTargetEnabled(): Boolean {
    if (activeLocationTarget is LocationTarget.Empty || isMocking || isWaitingForRouteFetch) {
        return false
    }
    return isClearLocationTargetVisible()
}
