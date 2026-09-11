package com.emoulya.flexkeypad.util

import com.emoulya.flexkeypad.domain.model.KeypadButton
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ButtonSnappingHelperTest {

    private val targetButton = KeypadButton(
        id = "btn-target",
        label = "Target",
        positionX = 100f,
        positionY = 100f,
        width = 80f,
        height = 60f,
        hidKeyCode = 0x04
    )

    @Test
    fun calculateSnap_noOtherButtons_returnsCurrentPositionCoerced() {
        val result = ButtonSnappingHelper.calculateSnap(
            buttonId = "btn-drag",
            currentX = -10f,
            currentY = 50f,
            width = 80f,
            height = 60f,
            otherButtons = emptyList()
        )

        assertEquals(0f, result.snappedX, 0.01f)
        assertEquals(50f, result.snappedY, 0.01f)
        assertNull(result.guideLineX)
        assertNull(result.guideLineY)
    }

    @Test
    fun calculateSnap_draggedMatchesTargetId_isIgnored() {
        val result = ButtonSnappingHelper.calculateSnap(
            buttonId = "btn-target",
            currentX = 105f,
            currentY = 105f,
            width = 80f,
            height = 60f,
            otherButtons = listOf(targetButton)
        )

        assertEquals(105f, result.snappedX, 0.01f)
        assertEquals(105f, result.snappedY, 0.01f)
        assertNull(result.guideLineX)
        assertNull(result.guideLineY)
    }

    @Test
    fun calculateSnap_withinThreshold_snapsLeftEdgeToLeftEdge() {
        // Target left is 100f. Dragged left is 104f (delta 4f <= 12f)
        val result = ButtonSnappingHelper.calculateSnap(
            buttonId = "btn-drag",
            currentX = 104f,
            currentY = 250f,
            width = 80f,
            height = 60f,
            otherButtons = listOf(targetButton)
        )

        assertEquals(100f, result.snappedX, 0.01f)
        assertEquals(100f, result.guideLineX)
    }

    @Test
    fun calculateSnap_withinThreshold_snapsRightEdgeToRightEdge() {
        // Target right is 100 + 80 = 180f. Dragged width is 80f.
        // For right alignment, snapped left should be 180 - 80 = 100f.
        // If dragged is width 60f, snapped left = 180 - 60 = 120f.
        val result = ButtonSnappingHelper.calculateSnap(
            buttonId = "btn-drag",
            currentX = 123f, // delta 3f from 120f
            currentY = 250f,
            width = 60f,
            height = 60f,
            otherButtons = listOf(targetButton)
        )

        assertEquals(120f, result.snappedX, 0.01f)
        assertEquals(180f, result.guideLineX)
    }

    @Test
    fun calculateSnap_withinThreshold_snapsCenterToCenter() {
        // Target center X = 100 + 40 = 140f.
        // Dragged width = 60f -> center offset = 30f -> target snapped X = 140 - 30 = 110f.
        val result = ButtonSnappingHelper.calculateSnap(
            buttonId = "btn-drag",
            currentX = 112f, // delta 2f from 110f
            currentY = 250f,
            width = 60f,
            height = 60f,
            otherButtons = listOf(targetButton)
        )

        assertEquals(110f, result.snappedX, 0.01f)
        assertEquals(140f, result.guideLineX)
    }

    @Test
    fun calculateSnap_withinThreshold_snapsAdjacentRightFlush() {
        // Target right = 180f. Gap = 0f -> snappedX = 180f.
        // With vertical overlap (currentY = 110f, height = 60f overlaps target 100f..160f)
        val result = ButtonSnappingHelper.calculateSnap(
            buttonId = "btn-drag",
            currentX = 183f, // delta 3f from 180f
            currentY = 110f,
            width = 80f,
            height = 60f,
            otherButtons = listOf(targetButton)
        )

        assertEquals(180f, result.snappedX, 0.01f)
        assertEquals(180f, result.guideLineX)
    }

    @Test
    fun calculateSnap_withinThreshold_snapsAdjacentRight8dp() {
        // Target right = 180f. Standard gap = 8f -> snappedX = 188f.
        val result = ButtonSnappingHelper.calculateSnap(
            buttonId = "btn-drag",
            currentX = 190f, // delta 2f from 188f
            currentY = 110f,
            width = 80f,
            height = 60f,
            otherButtons = listOf(targetButton)
        )

        assertEquals(188f, result.snappedX, 0.01f)
        assertEquals(180f, result.guideLineX)
    }

    @Test
    fun calculateSnap_withinThreshold_snapsTopEdgeToTopEdge() {
        // Target top = 100f. Dragged currentY = 103f (delta 3f <= 12f)
        val result = ButtonSnappingHelper.calculateSnap(
            buttonId = "btn-drag",
            currentX = 300f,
            currentY = 103f,
            width = 80f,
            height = 60f,
            otherButtons = listOf(targetButton)
        )

        assertEquals(100f, result.snappedY, 0.01f)
        assertEquals(100f, result.guideLineY)
    }

    @Test
    fun calculateSnap_withinThreshold_snapsAdjacentBelowFlush() {
        // Target bottom = 100 + 60 = 160f. Gap = 0f -> snappedY = 160f.
        // Horizontal overlap: currentX = 110f overlaps target 100f..180f
        val result = ButtonSnappingHelper.calculateSnap(
            buttonId = "btn-drag",
            currentX = 110f,
            currentY = 163f, // delta 3f from 160f
            width = 80f,
            height = 60f,
            otherButtons = listOf(targetButton)
        )

        assertEquals(160f, result.snappedY, 0.01f)
        assertEquals(160f, result.guideLineY)
    }

    @Test
    fun calculateSnap_withinThreshold_snapsAdjacentBelow8dp() {
        // Target bottom = 160f. Gap = 8f -> snappedY = 168f.
        val result = ButtonSnappingHelper.calculateSnap(
            buttonId = "btn-drag",
            currentX = 110f,
            currentY = 166f, // delta 2f from 168f
            width = 80f,
            height = 60f,
            otherButtons = listOf(targetButton)
        )

        assertEquals(168f, result.snappedY, 0.01f)
        assertEquals(160f, result.guideLineY)
    }

    @Test
    fun calculateSnap_outsideThreshold_doesNotSnap() {
        // Delta > 12dp threshold
        val result = ButtonSnappingHelper.calculateSnap(
            buttonId = "btn-drag",
            currentX = 135f,
            currentY = 135f,
            width = 80f,
            height = 60f,
            otherButtons = listOf(targetButton),
            thresholdDp = 4f
        )

        assertEquals(135f, result.snappedX, 0.01f)
        assertEquals(135f, result.snappedY, 0.01f)
        assertNull(result.guideLineX)
        assertNull(result.guideLineY)
    }
}
