// ShapeCanvasView.kt
package com.example.camera_test

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class ShapeCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var currentShape: ShapeType = ShapeType.NONE

    private val linePaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
    }

    private val thinLinePaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 40f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val smallTextPaint = Paint().apply {
        color = Color.BLACK
        textSize = 30f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    fun setShape(shape: ShapeType) {
        currentShape = shape
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (currentShape == ShapeType.NONE) {
            return
        }

        val centerX = width / 2f
        val centerY = height / 2f
        val size = min(width, height) * 0.5f

        when (currentShape) {
            ShapeType.CIRCLE -> drawCircleWithLabels(canvas, centerX, centerY, size / 2)
            ShapeType.SQUARE -> drawSquareWithLabels(canvas, centerX, centerY, size)
            ShapeType.RECTANGLE -> drawRectangleWithLabels(canvas, centerX, centerY, size)
            ShapeType.ISOSCELES_TRIANGLE -> drawIsoscelesTriangleWithLabels(canvas, centerX, centerY, size)
            ShapeType.RIGHT_TRIANGLE -> drawRightTriangleWithLabels(canvas, centerX, centerY, size)
            ShapeType.NONE -> { }
        }
    }

    private fun drawCircleWithLabels(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        canvas.drawCircle(cx, cy, radius, linePaint)
        canvas.drawCircle(cx, cy, 5f, linePaint)
        canvas.drawLine(cx, cy, cx + radius, cy, thinLinePaint)
        canvas.drawLine(cx - radius, cy, cx + radius, cy, thinLinePaint)
        canvas.drawText("O", cx, cy - 15, textPaint)
        canvas.drawText("r", cx + radius / 2, cy - 10, smallTextPaint)
        canvas.drawText("d", cx - radius / 2, cy - 10, smallTextPaint)
    }

    private fun drawSquareWithLabels(canvas: Canvas, cx: Float, cy: Float, size: Float) {
        val halfSize = size / 2
        val left = cx - halfSize
        val top = cy - halfSize
        val right = cx + halfSize
        val bottom = cy + halfSize

        canvas.drawRect(left, top, right, bottom, linePaint)

        // Diagonals
        canvas.drawLine(left, top, right, bottom, thinLinePaint)
        canvas.drawLine(right, top, left, bottom, thinLinePaint)

        drawEqualMark(canvas, cx, top, true)
        drawEqualMark(canvas, cx, bottom, true)
        drawEqualMark(canvas, left, cy, false)
        drawEqualMark(canvas, right, cy, false)

        canvas.drawText("A", left - 30, top - 10, textPaint)
        canvas.drawText("B", right + 30, top - 10, textPaint)
        canvas.drawText("C", right + 30, bottom + 45, textPaint)
        canvas.drawText("D", left - 30, bottom + 45, textPaint)
        canvas.drawText("a", cx, top - 20, smallTextPaint)
        canvas.drawText("d", cx + 15, cy - 15, smallTextPaint)
    }

    private fun drawRectangleWithLabels(canvas: Canvas, cx: Float, cy: Float, size: Float) {
        val width = size
        val height = size * 0.6f

        val left = cx - width / 2
        val top = cy - height / 2
        val right = cx + width / 2
        val bottom = cy + height / 2

        canvas.drawRect(left, top, right, bottom, linePaint)

        // Diagonals
        canvas.drawLine(left, top, right, bottom, thinLinePaint)
        canvas.drawLine(right, top, left, bottom, thinLinePaint)

        drawEqualMark(canvas, cx, top, true)
        drawEqualMark(canvas, cx, bottom, true)
        drawDoubleMark(canvas, left, cy, false)
        drawDoubleMark(canvas, right, cy, false)

        canvas.drawText("A", left - 30, top - 10, textPaint)
        canvas.drawText("B", right + 30, top - 10, textPaint)
        canvas.drawText("C", right + 30, bottom + 45, textPaint)
        canvas.drawText("D", left - 30, bottom + 45, textPaint)
        canvas.drawText("a", cx, top - 20, smallTextPaint)
        canvas.drawText("b", right + 35, cy, smallTextPaint)
        canvas.drawText("d", cx + 15, cy - 15, smallTextPaint)
    }

    private fun drawIsoscelesTriangleWithLabels(canvas: Canvas, cx: Float, cy: Float, size: Float) {
        val path = Path()

        // A - top corner (top)
        // B - left corner at the base
        // C - right corner at the base
        val topX = cx
        val topY = cy - size / 2
        val leftX = cx - size / 2
        val leftY = cy + size / 2
        val rightX = cx + size / 2
        val rightY = cy + size / 2

        path.moveTo(topX, topY)
        path.lineTo(leftX, leftY)
        path.lineTo(rightX, rightY)
        path.close()

        canvas.drawPath(path, linePaint)

        // Bisector from vertex A to the base (also known as the height and median)
        val baseMidX = (leftX + rightX) / 2
        val baseMidY = (leftY + rightY) / 2
        canvas.drawLine(topX, topY, baseMidX, baseMidY, thinLinePaint)

        // Mark the equal sides (one line on each)
        val leftMidX = (topX + leftX) / 2
        val leftMidY = (topY + leftY) / 2

        canvas.save()
        canvas.translate(leftMidX, leftMidY)
        val angleLeft = Math.toDegrees(Math.atan2((leftY - topY).toDouble(), (leftX - topX).toDouble())).toFloat()
        canvas.rotate(angleLeft + 90f)
        canvas.drawLine(-8f, 0f, 8f, 0f, thinLinePaint)
        canvas.restore()

        val rightMidX = (topX + rightX) / 2
        val rightMidY = (topY + rightY) / 2

        canvas.save()
        canvas.translate(rightMidX, rightMidY)
        val angleRight = Math.toDegrees(Math.atan2((rightY - topY).toDouble(), (rightX - topX).toDouble())).toFloat()
        canvas.rotate(angleRight + 90f)
        canvas.drawLine(-8f, 0f, 8f, 0f, thinLinePaint)
        canvas.restore()

        // Mark equal angles at the base B and C (small arcs)
        val arcRadius = 30f

        // Left corner B - from the base to the side
        val angleBStart = 0f // From the horizontal (base)
        val angleBToSide = Math.toDegrees(Math.atan2((topY - leftY).toDouble(), (topX - leftX).toDouble())).toFloat()
        val angleBSweep = angleBToSide - angleBStart

        canvas.drawArc(
            leftX - arcRadius, leftY - arcRadius,
            leftX + arcRadius, leftY + arcRadius,
            angleBStart, angleBSweep, false, thinLinePaint
        )

        // Right angle C - inversion (draw a small arc on the other side)
        val angleCToSide = Math.toDegrees(Math.atan2((topY - rightY).toDouble(), (topX - rightX).toDouble())).toFloat()
        val angleCStart = 180f
        val angleCEnd = angleCToSide + 360f
        val angleCSweep = angleCEnd - angleCStart

        canvas.drawArc(
            rightX - arcRadius, rightY - arcRadius,
            rightX + arcRadius, rightY + arcRadius,
            angleCStart, angleCSweep, false, thinLinePaint
        )

        // Vertex letters
        canvas.drawText("A", topX, topY - 20, textPaint)
        canvas.drawText("B", leftX - 30, leftY + 40, textPaint)
        canvas.drawText("C", rightX + 30, rightY + 40, textPaint)

        // Designation of parties
        canvas.drawText("a", leftMidX - 25, leftMidY, smallTextPaint)
        canvas.drawText("a", rightMidX + 25, rightMidY, smallTextPaint)
        canvas.drawText("b", cx, leftY + 30, smallTextPaint)
        canvas.drawText("l", cx - 20, (topY + baseMidY) / 2, smallTextPaint)
    }

    private fun drawRightTriangleWithLabels(canvas: Canvas, cx: Float, cy: Float, size: Float) {
        val path = Path()

        val leftX = cx - size / 2
        val bottomY = cy + size / 2
        val rightX = cx + size / 2
        val topY = cy - size / 2

        path.moveTo(leftX, bottomY)
        path.lineTo(rightX, bottomY)
        path.lineTo(leftX, topY)
        path.close()

        canvas.drawPath(path, linePaint)

        // Bisector from right angle B to hypotenuse
        val hypMidX = (leftX + rightX) / 2
        val hypMidY = (topY + bottomY) / 2
        canvas.drawLine(leftX, bottomY, hypMidX, hypMidY, thinLinePaint)

        // Square of a right angle
        val squareSize = 25f
        canvas.drawRect(
            leftX,
            bottomY - squareSize,
            leftX + squareSize,
            bottomY,
            linePaint
        )

        // Vertex letters
        canvas.drawText("A", leftX - 30, topY - 10, textPaint)
        canvas.drawText("B", leftX - 30, bottomY + 40, textPaint)
        canvas.drawText("C", rightX + 30, bottomY + 40, textPaint)

        // Designation of parties
        canvas.drawText("a", leftX - 35, (topY + bottomY) / 2, smallTextPaint)
        canvas.drawText("b", (leftX + rightX) / 2, bottomY + 30, smallTextPaint)
        canvas.drawText("c", hypMidX + 30, hypMidY, smallTextPaint)
        canvas.drawText("l", (leftX + hypMidX) / 2 + 15, (bottomY + hypMidY) / 2, smallTextPaint)
    }

    private fun drawEqualMark(canvas: Canvas, x: Float, y: Float, horizontal: Boolean) {
        val markLength = 15f
        if (horizontal) {
            canvas.drawLine(x, y - markLength/2, x, y + markLength/2, thinLinePaint)
        } else {
            canvas.drawLine(x - markLength/2, y, x + markLength/2, y, thinLinePaint)
        }
    }

    private fun drawDoubleMark(canvas: Canvas, x: Float, y: Float, horizontal: Boolean) {
        val markLength = 15f
        val spacing = 5f

        if (horizontal) {
            canvas.drawLine(x - spacing, y - markLength/2, x - spacing, y + markLength/2, thinLinePaint)
            canvas.drawLine(x + spacing, y - markLength/2, x + spacing, y + markLength/2, thinLinePaint)
        } else {
            canvas.drawLine(x - markLength/2, y - spacing, x + markLength/2, y - spacing, thinLinePaint)
            canvas.drawLine(x - markLength/2, y + spacing, x + markLength/2, y + spacing, thinLinePaint)
        }
    }
}