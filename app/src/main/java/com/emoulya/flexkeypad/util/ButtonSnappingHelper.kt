package com.emoulya.flexkeypad.util

import com.emoulya.flexkeypad.domain.model.KeypadButton
import kotlin.math.abs

/**
 * Result of magnetic snapping calculation.
 */
data class SnapResult(
    val snappedX: Float,
    val snappedY: Float,
    val guideLineX: Float? = null,
    val guideLineY: Float? = null
)

/**
 * Pure utility to calculate magnetic snapping between keypad buttons.
 * Allows buttons to smoothly lock onto neighboring button edges, centers, or adjacent margins.
 */
object ButtonSnappingHelper {

    const val DEFAULT_SNAP_THRESHOLD_DP = 12f
    val STANDARD_GAPS_DP = listOf(0f, 8f) // 0dp (flush) and 8dp (standard keypad gap)

    /**
     * Calculates magnetic snapped coordinates for a dragged button against all other buttons on the canvas.
     *
     * @param buttonId ID of the button being dragged (excluded from snap targets)
     * @param currentX Candidate X position (unbounded/continuous drag position)
     * @param currentY Candidate Y position (unbounded/continuous drag position)
     * @param width Width of the dragged button
     * @param height Height of the dragged button
     * @param otherButtons List of all buttons in the active profile
     * @param thresholdDp Maximum distance in DP to trigger snapping (default 12dp)
     * @return [SnapResult] containing final snapped coordinates and optional visual guideline coordinates
     */
    fun calculateSnap(
        buttonId: String,
        currentX: Float,
        currentY: Float,
        width: Float,
        height: Float,
        otherButtons: List<KeypadButton>,
        thresholdDp: Float = DEFAULT_SNAP_THRESHOLD_DP
    ): SnapResult {
        val targets = otherButtons.filter { it.id != buttonId }
        if (targets.isEmpty()) {
            return SnapResult(
                snappedX = currentX.coerceAtLeast(0f),
                snappedY = currentY.coerceAtLeast(0f)
            )
        }

        var bestX = currentX
        var minDeltaX = thresholdDp + 1f
        var guideLineX: Float? = null

        var bestY = currentY
        var minDeltaY = thresholdDp + 1f
        var guideLineY: Float? = null

        // Evaluate Horizontal (X) snapping candidates against each target button
        for (target in targets) {
            // Give preference to buttons that have vertical overlap or are vertically close
            val verticalOverlap = (currentY + height >= target.positionY - 40f) &&
                    (currentY <= target.positionY + target.height + 40f)

            val xCandidates = mutableListOf<Pair<Float, Float>>() // Pair<snappedX, guidelineX>

            // 1. Edge-to-Edge Alignment: Left-to-Left
            xCandidates.add(target.positionX to target.positionX)

            // 2. Edge-to-Edge Alignment: Right-to-Right
            xCandidates.add((target.positionX + target.width - width) to (target.positionX + target.width))

            // 3. Center-to-Center Alignment
            xCandidates.add(
                (target.positionX + (target.width - width) / 2f) to (target.positionX + target.width / 2f)
            )

            // 4. Adjacent Snapping: Side-by-side with gaps (0dp flush, 8dp standard)
            if (verticalOverlap) {
                for (gap in STANDARD_GAPS_DP) {
                    // Dragged button to the RIGHT of target
                    val rightSideX = target.positionX + target.width + gap
                    xCandidates.add(rightSideX to target.positionX + target.width)

                    // Dragged button to the LEFT of target
                    val leftSideX = target.positionX - gap - width
                    xCandidates.add(leftSideX to target.positionX)
                }
            }

            // Find closest X candidate
            for ((candidateX, guideX) in xCandidates) {
                val delta = abs(candidateX - currentX)
                if (delta < minDeltaX) {
                    minDeltaX = delta
                    bestX = candidateX
                    guideLineX = guideX
                }
            }
        }

        // Evaluate Vertical (Y) snapping candidates against each target button
        for (target in targets) {
            // Give preference to buttons that have horizontal overlap or are horizontally close
            val horizontalOverlap = (currentX + width >= target.positionX - 40f) &&
                    (currentX <= target.positionX + target.width + 40f)

            val yCandidates = mutableListOf<Pair<Float, Float>>() // Pair<snappedY, guidelineY>

            // 1. Edge-to-Edge Alignment: Top-to-Top
            yCandidates.add(target.positionY to target.positionY)

            // 2. Edge-to-Edge Alignment: Bottom-to-Bottom
            yCandidates.add((target.positionY + target.height - height) to (target.positionY + target.height))

            // 3. Center-to-Center Alignment
            yCandidates.add(
                (target.positionY + (target.height - height) / 2f) to (target.positionY + target.height / 2f)
            )

            // 4. Adjacent Snapping: Stacked above/below with gaps (0dp flush, 8dp standard)
            if (horizontalOverlap) {
                for (gap in STANDARD_GAPS_DP) {
                    // Dragged button BELOW target
                    val belowY = target.positionY + target.height + gap
                    yCandidates.add(belowY to target.positionY + target.height)

                    // Dragged button ABOVE target
                    val aboveY = target.positionY - gap - height
                    yCandidates.add(aboveY to target.positionY)
                }
            }

            // Find closest Y candidate
            for ((candidateY, guideY) in yCandidates) {
                val delta = abs(candidateY - currentY)
                if (delta < minDeltaY) {
                    minDeltaY = delta
                    bestY = candidateY
                    guideLineY = guideY
                }
            }
        }

        val finalX = if (minDeltaX <= thresholdDp) bestX else currentX
        val finalY = if (minDeltaY <= thresholdDp) bestY else currentY

        return SnapResult(
            snappedX = finalX.coerceAtLeast(0f),
            snappedY = finalY.coerceAtLeast(0f),
            guideLineX = if (minDeltaX <= thresholdDp) guideLineX else null,
            guideLineY = if (minDeltaY <= thresholdDp) guideLineY else null
        )
    }
}
