package com.drew654.mocklocations.domain.model

import com.drew654.mocklocations.domain.rules.MockControlActionRules

data class MockControlState(
    val isMocking: Boolean = false,
    val isPaused: Boolean = false,
    val activeLocationTarget: LocationTarget = LocationTarget.Empty,
    val isUsingCrosshairs: Boolean = false
)

fun MockControlState.getVisibleActions(): Set<MockControlAction> {
    return MockControlActionRules.visibleActions(
        isMocking,
        isPaused,
        activeLocationTarget,
        isUsingCrosshairs
    )
}

fun MockControlState.getEnabledActions(): Set<MockControlAction> {
    return MockControlActionRules.enabledActions(
        isMocking,
        isPaused,
        activeLocationTarget,
        isUsingCrosshairs
    )
}
