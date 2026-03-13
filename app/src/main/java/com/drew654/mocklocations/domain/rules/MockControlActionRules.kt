package com.drew654.mocklocations.domain.rules

import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.MockControlAction

object MockControlActionRules {
    fun visibleActions(
        isMocking: Boolean,
        isPaused: Boolean,
        locationTarget: LocationTarget,
        isUsingCrosshairs: Boolean
    ): Set<MockControlAction> {
        val actions = mutableSetOf<MockControlAction>()

        if (isMocking) {
            actions.add(MockControlAction.STOP)
        } else {
            actions.add(MockControlAction.START)
        }

        val isRoute =
            locationTarget is LocationTarget.Route || locationTarget is LocationTarget.SavedRoute

        if (isMocking && isRoute) {
            if (isPaused) {
                actions.add(MockControlAction.RESUME)
            } else {
                actions.add(MockControlAction.PAUSE)
            }
        }

        if (!isMocking && isUsingCrosshairs) {
            actions.add(MockControlAction.ADD_POINT)
        }

        actions.add(MockControlAction.CLEAR_LOCATION_TARGET)
        actions.add(MockControlAction.POP_POINT)

        return actions
    }

    fun enabledActions(
        isMocking: Boolean,
        isPaused: Boolean,
        locationTarget: LocationTarget,
        isUsingCrosshairs: Boolean
    ): Set<MockControlAction> {
        val actions = visibleActions(
            isMocking,
            isPaused,
            locationTarget,
            isUsingCrosshairs
        ).toMutableSet()

        if (locationTarget is LocationTarget.Empty && !isUsingCrosshairs) {
            actions.remove(MockControlAction.START)
        }

        if (locationTarget is LocationTarget.Empty || isMocking) {
            actions.remove(MockControlAction.CLEAR_LOCATION_TARGET)
            actions.remove(MockControlAction.POP_POINT)
        }

        return actions
    }
}
