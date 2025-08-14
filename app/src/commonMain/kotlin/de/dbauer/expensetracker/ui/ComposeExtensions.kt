package de.dbauer.expensetracker.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.Spring.StiffnessMediumLow
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.round
import kotlinx.coroutines.launch

fun Modifier.animatePlacement(): Modifier = this.then(AnimatePlacementElement)

private object AnimatePlacementElement : ModifierNodeElement<AnimatePlacementNode>() {
    override fun create(): AnimatePlacementNode = AnimatePlacementNode()

    override fun update(node: AnimatePlacementNode) {}

    override fun InspectorInfo.inspectableProperties() {
        name = "animatePlacement"
    }

    override fun hashCode(): Int = "animatePlacement".hashCode()

    override fun equals(other: Any?): Boolean = (other === this)
}

private class AnimatePlacementNode :
    Modifier.Node(),
    LayoutModifierNode,
    GlobalPositionAwareModifierNode {
    private var isInitiallyPlaced = false
    private var targetOffset: IntOffset = IntOffset.Zero
    private var animatable: Animatable<IntOffset, AnimationVector2D>? = null

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        val newOffset = coordinates.positionInParent().round()

        if (!isInitiallyPlaced) {
            targetOffset = newOffset
            isInitiallyPlaced = true
        } else if (newOffset != targetOffset) {
            val previousOffset = targetOffset
            targetOffset = newOffset

            val anim =
                animatable ?: Animatable(previousOffset, IntOffset.VectorConverter).also {
                    animatable = it
                }

            coroutineScope.launch {
                anim.snapTo(previousOffset)
                anim.animateTo(targetOffset, spring(stiffness = StiffnessMediumLow))
            }
        }
    }

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints,
    ): MeasureResult {
        val placeable = measurable.measure(constraints)
        val offset = animatable?.let { it.value - targetOffset } ?: IntOffset.Zero

        return layout(placeable.width, placeable.height) {
            placeable.placeRelative(offset)
        }
    }
}
