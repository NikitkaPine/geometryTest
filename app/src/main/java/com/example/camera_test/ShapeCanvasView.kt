package com.example.camera_test

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.math.min
import kotlin.math.sqrt

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

        drawEqualMark(canvas, cx, top, true)
        drawEqualMark(canvas, cx, bottom, true)
        drawEqualMark(canvas, left, cy, false)
        drawEqualMark(canvas, right, cy, false)

        canvas.drawText("A", left - 30, top - 10, textPaint)
        canvas.drawText("B", right + 30, top - 10, textPaint)
        canvas.drawText("C", right + 30, bottom + 45, textPaint)
        canvas.drawText("D", left - 30, bottom + 45, textPaint)
        canvas.drawText("a", cx, top - 20, smallTextPaint)
    }

    private fun drawRectangleWithLabels(canvas: Canvas, cx: Float, cy: Float, size: Float) {
        val width = size
        val height = size * 0.6f

        val left = cx - width / 2
        val top = cy - height / 2
        val right = cx + width / 2
        val bottom = cy + height / 2

        canvas.drawRect(left, top, right, bottom, linePaint)

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
    }

    private fun drawIsoscelesTriangleWithLabels(canvas: Canvas, cx: Float, cy: Float, size: Float) {
        val path = Path()

        // A - вершина (сверху)
        // B - левый угол у основания
        // C - правый угол у основания
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

        // Отмечаем равные боковые стороны (одна черточка на каждой)
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

        // Отмечаем равные углы у основания B и C (маленькие дуги)
        val arcRadius = 30f

        // Левый угол B - от горизонтали к боковой стороне
        val angleBToSide = Math.toDegrees(Math.atan2((topY - leftY).toDouble(), (topX - leftX).toDouble())).toFloat()
        canvas.drawArc(
            leftX - arcRadius, leftY - arcRadius,
            leftX + arcRadius, leftY + arcRadius,
            180f, angleBToSide - 180f, false, thinLinePaint
        )

        // Правый угол C - от боковой стороны к горизонтали
        val angleCToSide = Math.toDegrees(Math.atan2((topY - rightY).toDouble(), (topX - rightX).toDouble())).toFloat()
        canvas.drawArc(
            rightX - arcRadius, rightY - arcRadius,
            rightX + arcRadius, rightY + arcRadius,
            angleCToSide, 180f - angleCToSide, false, thinLinePaint
        )

        // Буквы вершин
        canvas.drawText("A", topX, topY - 20, textPaint)
        canvas.drawText("B", leftX - 30, leftY + 40, textPaint)
        canvas.drawText("C", rightX + 30, rightY + 40, textPaint)

        // Обозначение сторон
        canvas.drawText("a", leftMidX - 25, leftMidY, smallTextPaint)
        canvas.drawText("a", rightMidX + 25, rightMidY, smallTextPaint)
        canvas.drawText("b", cx, leftY + 30, smallTextPaint)
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

        // Квадратик прямого угла
        val squareSize = 25f
        canvas.drawRect(
            leftX,
            bottomY - squareSize,
            leftX + squareSize,
            bottomY,
            linePaint
        )

        // Буквы вершин
        canvas.drawText("A", leftX - 30, topY - 10, textPaint)
        canvas.drawText("B", leftX - 30, bottomY + 40, textPaint)
        canvas.drawText("C", rightX + 30, bottomY + 40, textPaint)

        // Обозначение сторон
        canvas.drawText("a", leftX - 35, (topY + bottomY) / 2, smallTextPaint)
        canvas.drawText("b", (leftX + rightX) / 2, bottomY + 30, smallTextPaint)
        val hypMidX = (leftX + rightX) / 2
        val hypMidY = (topY + bottomY) / 2
        canvas.drawText("c", hypMidX + 30, hypMidY, smallTextPaint)
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