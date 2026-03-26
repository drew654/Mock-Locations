package com.drew654.mocklocations.domain.model

data class MockControlState(
    val isMocking: Boolean = false,
    val isPaused: Boolean = false,
    val isWaitingAtEndOfRoute: Boolean = false,
    val activeLocationTarget: LocationTarget = LocationTarget.Empty,
    val isUsingCrosshairs: Boolean = true
)

fun MockControlState.getVisibleActions(): Set<MockControlAction> {
    val actions = mutableSetOf<MockControlAction>()
    if (isStartVisible()) {
        actions.add(MockControlAction.START)
    }
    if (isStopVisible()) {
        actions.add(MockControlAction.STOP)
    }
    if (isPauseVisible()) {
        actions.add(MockControlAction.PAUSE)
    }
    if (isResumeVisible()) {
        actions.add(MockControlAction.RESUME)
    }
    if (isAddPointVisible()) {
        actions.add(MockControlAction.ADD_POINT)
    }
    if (isPopPointVisible()) {
        actions.add(MockControlAction.POP_POINT)
    }
    if (isClearLocationTargetVisible()) {
        actions.add(MockControlAction.CLEAR_LOCATION_TARGET)
    }
    return actions
}

fun MockControlState.getEnabledActions(): Set<MockControlAction> {
    val actions = mutableSetOf<MockControlAction>()
    if (isStartEnabled()) {
        actions.add(MockControlAction.START)
    }
    if (isStopEnabled()) {
        actions.add(MockControlAction.STOP)
    }
    if (isPauseEnabled()) {
        actions.add(MockControlAction.PAUSE)
    }
    if (isResumeEnabled()) {
        actions.add(MockControlAction.RESUME)
    }
    if (isAddPointEnabled()) {
        actions.add(MockControlAction.ADD_POINT)
    }
    if (isPopPointEnabled()) {
        actions.add(MockControlAction.POP_POINT)
    }
    if (isClearLocationTargetEnabled()) {
        actions.add(MockControlAction.CLEAR_LOCATION_TARGET)
    }
    return actions
}

fun MockControlState.isStartVisible(): Boolean {
    return !isMocking
}

fun MockControlState.isStartEnabled(): Boolean {
    if (activeLocationTarget is LocationTarget.Empty && !isUsingCrosshairs) {
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
    return isAddPointVisible()
}

fun isPopPointVisible(): Boolean {
    return true
}

fun MockControlState.isPopPointEnabled(): Boolean {
    if (activeLocationTarget is LocationTarget.Empty || isMocking) {
        return false
    }
    return isPopPointVisible()
}

fun isClearLocationTargetVisible(): Boolean {
    return true
}

fun MockControlState.isClearLocationTargetEnabled(): Boolean {
    if (activeLocationTarget is LocationTarget.Empty || isMocking) {
        return false
    }
    return isClearLocationTargetVisible()
}
