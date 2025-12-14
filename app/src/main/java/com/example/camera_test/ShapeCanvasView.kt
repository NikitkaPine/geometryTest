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

    // Основная линия фигуры
    private val linePaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
    }

    // Тонкая линия для обозначений
    private val thinLinePaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }

    // Текст для букв и обозначений
    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 40f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    // Маленький текст для r и d
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
        // Рисуем круг
        canvas.drawCircle(cx, cy, radius, linePaint)

        // Рисуем центральную точку
        canvas.drawCircle(cx, cy, 5f, linePaint)

        // Рисуем линию радиуса (горизонтально)
        canvas.drawLine(cx, cy, cx + radius, cy, thinLinePaint)

        // Продолжаем линию для диаметра
        canvas.drawLine(cx - radius, cy, cx + radius, cy, thinLinePaint)

        // Буква O в центре (чуть выше)
        canvas.drawText("O", cx, cy - 15, textPaint)

        // Буква r (радиус) - на половине радиуса справа
        canvas.drawText("r", cx + radius / 2, cy - 10, smallTextPaint)

        // Буква d (диаметр) - слева от центра
        canvas.drawText("d", cx - radius / 2, cy - 10, smallTextPaint)
    }

    private fun drawSquareWithLabels(canvas: Canvas, cx: Float, cy: Float, size: Float) {
        val halfSize = size / 2

        val left = cx - halfSize
        val top = cy - halfSize
        val right = cx + halfSize
        val bottom = cy + halfSize

        // Рисуем квадрат
        canvas.drawRect(left, top, right, bottom, linePaint)

        // Отмечаем равные стороны (по одной черточке на каждой стороне)
        drawEqualMark(canvas, cx, top, true) // верх
        drawEqualMark(canvas, cx, bottom, true) // низ
        drawEqualMark(canvas, left, cy, false) // лево
        drawEqualMark(canvas, right, cy, false) // право

        // Буквы вершин A B C D (по часовой стрелке начиная с левого верхнего)
        canvas.drawText("A", left - 30, top - 10, textPaint)
        canvas.drawText("B", right + 30, top - 10, textPaint)
        canvas.drawText("C", right + 30, bottom + 45, textPaint)
        canvas.drawText("D", left - 30, bottom + 45, textPaint)

        // Обозначение стороны a (сверху по центру)
        canvas.drawText("a", cx, top - 20, smallTextPaint)
    }

    private fun drawRectangleWithLabels(canvas: Canvas, cx: Float, cy: Float, size: Float) {
        val width = size
        val height = size * 0.6f

        val left = cx - width / 2
        val top = cy - height / 2
        val right = cx + width / 2
        val bottom = cy + height / 2

        // Рисуем прямоугольник
        canvas.drawRect(left, top, right, bottom, linePaint)

        // Отмечаем равные стороны
        drawEqualMark(canvas, cx, top, true) // верх - 1 черточка
        drawEqualMark(canvas, cx, bottom, true) // низ - 1 черточка
        drawDoubleMark(canvas, left, cy, false) // лево - 2 черточки
        drawDoubleMark(canvas, right, cy, false) // право - 2 черточки

        // Буквы вершин
        canvas.drawText("A", left - 30, top - 10, textPaint)
        canvas.drawText("B", right + 30, top - 10, textPaint)
        canvas.drawText("C", right + 30, bottom + 45, textPaint)
        canvas.drawText("D", left - 30, bottom + 45, textPaint)

        // Обозначение сторон
        canvas.drawText("a", cx, top - 20, smallTextPaint) // ширина сверху
        canvas.drawText("b", right + 35, cy, smallTextPaint) // высота справа
    }

    private fun drawIsoscelesTriangleWithLabels(canvas: Canvas, cx: Float, cy: Float, size: Float) {
        val path = Path()

        // Вершина треугольника (сверху)
        val topX = cx
        val topY = cy - size / 2

        // Левый нижний угол
        val leftX = cx - size / 2
        val leftY = cy + size / 2

        // Правый нижний угол
        val rightX = cx + size / 2
        val rightY = cy + size / 2

        path.moveTo(topX, topY)
        path.lineTo(leftX, leftY)
        path.lineTo(rightX, rightY)
        path.close()

        canvas.drawPath(path, linePaint)

        // Отмечаем равные боковые стороны (левая и правая)
        val leftMidX = (topX + leftX) / 2
        val leftMidY = (topY + leftY) / 2
        // Для левой стороны - поворот примерно на угол наклона
        canvas.save()
        canvas.translate(leftMidX, leftMidY)
        canvas.rotate(-60f) // наклон для левой стороны
        canvas.drawLine(0f, -8f, 0f, 8f, thinLinePaint)
        canvas.restore()

        val rightMidX = (topX + rightX) / 2
        val rightMidY = (topY + rightY) / 2
        // Для правой стороны
        canvas.save()
        canvas.translate(rightMidX, rightMidY)
        canvas.rotate(60f) // наклон для правой стороны
        canvas.drawLine(0f, -8f, 0f, 8f, thinLinePaint)
        canvas.restore()

        // Отмечаем равные углы у основания (маленькие дуги)
        val arcRadius = 25f
        // Левый угол (B) - дуга направлена вправо-вверх
        canvas.drawArc(
            leftX, leftY - arcRadius * 2,
            leftX + arcRadius * 2, leftY,
            -135f, 45f, false, thinLinePaint
        )
        // Правый угол (C) - дуга направлена влево-вверх
        canvas.drawArc(
            rightX - arcRadius * 2, rightY - arcRadius * 2,
            rightX, rightY,
            -45f, 45f, false, thinLinePaint
        )

        // Буквы вершин
        canvas.drawText("A", topX, topY - 20, textPaint)
        canvas.drawText("B", leftX - 30, leftY + 40, textPaint)
        canvas.drawText("C", rightX + 30, rightY + 40, textPaint)

        // Обозначение сторон
        canvas.drawText("a", leftMidX - 25, leftMidY, smallTextPaint) // левая боковая
        canvas.drawText("a", rightMidX + 25, rightMidY, smallTextPaint) // правая боковая
        canvas.drawText("b", cx, leftY + 30, smallTextPaint) // основание
    }

    private fun drawRightTriangleWithLabels(canvas: Canvas, cx: Float, cy: Float, size: Float) {
        val path = Path()

        // Прямой угол в левом нижнем углу
        val leftX = cx - size / 2
        val bottomY = cy + size / 2

        // Правый нижний угол
        val rightX = cx + size / 2

        // Верхний левый угол
        val topY = cy - size / 2

        path.moveTo(leftX, bottomY) // Начало - прямой угол
        path.lineTo(rightX, bottomY) // Горизонтальная линия
        path.lineTo(leftX, topY) // Вертикальная линия
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
        // a - левая вертикальная (катет)
        canvas.drawText("a", leftX - 35, (topY + bottomY) / 2, smallTextPaint)
        // b - нижняя горизонтальная (катет)
        canvas.drawText("b", (leftX + rightX) / 2, bottomY + 30, smallTextPaint)
        // c - гипотенуза (диагональ)
        val hypMidX = (leftX + rightX) / 2
        val hypMidY = (topY + bottomY) / 2
        canvas.drawText("c", hypMidX + 30, hypMidY, smallTextPaint)
    }

    // Одна черточка для обозначения равных сторон
    private fun drawEqualMark(canvas: Canvas, x: Float, y: Float, horizontal: Boolean, angle: Float = 0f) {
        val markLength = 15f
        canvas.save()
        canvas.translate(x, y)
        canvas.rotate(angle)

        if (horizontal) {
            canvas.drawLine(0f, -markLength/2, 0f, markLength/2, thinLinePaint)
        } else {
            canvas.drawLine(-markLength/2, 0f, markLength/2, 0f, thinLinePaint)
        }
        canvas.restore()
    }

    // Две черточки для обозначения других равных сторон
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

    // Дуга для обозначения угла
    private fun drawAngleMark(canvas: Canvas, x: Float, y: Float, radius: Float, startAngle: Float) {
        canvas.drawArc(
            x - radius, y - radius, x + radius, y + radius,
            startAngle, 60f, false, thinLinePaint
        )
    }
}