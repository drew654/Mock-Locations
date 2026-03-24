package com.drew654.mocklocations.domain.model

import com.drew654.mocklocations.domain.rules.MockControlActionRules

data class MockControlState(
    val isMocking: Boolean = false,
    val isPaused: Boolean = false,
    val isWaitingAtEndOfRoute: Boolean = false,
    val activeLocationTarget: LocationTarget = LocationTarget.Empty,
    val isUsingCrosshairs: Boolean = false
)

fun MockControlState.getVisibleActions(): Set<MockControlAction> {
    return MockControlActionRules.visibleActions(
        isMocking,
        isPaused,
        isWaitingAtEndOfRoute,
        activeLocationTarget,
        isUsingCrosshairs
    )
}

fun MockControlState.getEnabledActions(): Set<MockControlAction> {
    return MockControlActionRules.enabledActions(
        isMocking,
        isPaused,
        isWaitingAtEndOfRoute,
        activeLocationTarget,
        isUsingCrosshairs
    )
}

fun MockControlState.isPauseVisible(): Boolean {
    val isRoute =
        activeLocationTarget is LocationTarget.Route || activeLocationTarget is LocationTarget.SavedRoute
    return isMocking && isRoute && !isPaused && !isWaitingAtEndOfRoute
}

fun MockControlState.isResumeVisible(): Boolean {
    val isRoute =
        activeLocationTarget is LocationTarget.Route || activeLocationTarget is LocationTarget.SavedRoute
    return isMocking && isRoute && isPaused && !isWaitingAtEndOfRoute
}
