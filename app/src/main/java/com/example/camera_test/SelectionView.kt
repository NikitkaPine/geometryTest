package com.example.camera_test

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.sqrt

// Кастомная View — рамка выделения для кадрирования изображения.
// Пользователь может перетаскивать рамку и тянуть за угловые точки для изменения размера.
class SelectionView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    // Кисть для рисования белой рамки вокруг выделенной области
    private val framePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    // Кисть для затемнения области вне рамки (полупрозрачный чёрный)
    private val overlayPaint = Paint().apply {
        color = Color.parseColor("#66000000")
    }

    // Кисть для рисования белых кружков в углах рамки (ручки изменения размера)
    private val handlePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // Радиус угловых ручек — по нему определяем, попал ли палец на ручку
    private val handleRadius = 24f

    // Текущий прямоугольник выделения в координатах View
    private var rect = RectF(0f, 0f, 0f, 0f)
    // Флаг: рамка уже инициализирована
    private var initialized = false

    // Режим касания: ничего / перемещение / изменение размера за один из 4 углов
    private enum class Mode { NONE, MOVE, RESIZE_LT, RESIZE_RT, RESIZE_LB, RESIZE_RB }
    private var mode = Mode.NONE

    // Запоминаем координаты предыдущего касания, чтобы считать смещение (dx/dy)
    private var lastX = 0f
    private var lastY = 0f

    // Минимальный размер рамки — чтобы нельзя было сжать до нуля
    private val minSize = 80f

    // Когда View получила реальные размеры — создаём рамку по центру
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (!initialized && w > 0 && h > 0) {
            initCenteredRect()
        }
    }

    /**
     * Создаёт рамку в центре View размером 60% x 60% от её размеров.
     */
    fun initCenteredRect() {
        val w = width.toFloat()
        val h = height.toFloat()
        val rw = w * 0.6f
        val rh = h * 0.6f
        val left = (w - rw) / 2f
        val top  = (h - rh) / 2f
        rect.set(left, top, left + rw, top + rh)
        initialized = true
        invalidate() // Просим перерисовать View
    }

    /**
     * Рисуем три слоя:
     * 1. Тёмный оверлей на весь экран.
     * 2. «Вырезаем» прямоугольник рамки — там оверлей прозрачный.
     * 3. Белая рамка и угловые ручки поверх.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Слой 1: затемняем всё
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)

        // Слой 2: прорезаем «дырку» внутри рамки через PorterDuff.CLEAR
        // saveLayer нужен, чтобы CLEAR работал только в рамках этого слоя
        val saveCount = canvas.saveLayer(null, null)
        val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }
        canvas.drawRect(rect, clearPaint)
        canvas.restoreToCount(saveCount)

        // Слой 3: рамка и ручки
        canvas.drawRect(rect, framePaint)
        drawHandles(canvas)
    }

    /**
     * Рисуем четыре белых кружка по углам рамки.
     * LT = левый верх, RT = правый верх, LB = левый низ, RB = правый низ.
     */
    private fun drawHandles(canvas: Canvas) {
        canvas.drawCircle(rect.left,  rect.top,    handleRadius, handlePaint) // LT
        canvas.drawCircle(rect.right, rect.top,    handleRadius, handlePaint) // RT
        canvas.drawCircle(rect.left,  rect.bottom, handleRadius, handlePaint) // LB
        canvas.drawCircle(rect.right, rect.bottom, handleRadius, handlePaint) // RB
    }

    /**
     * Проверяет, находится ли точка (x,y) рядом с центром ручки (cx,cy).
     * Используем расстояние по теореме Пифагора. Порог — 1.5 радиуса.
     */
    private fun isNear(x: Float, y: Float, cx: Float, cy: Float): Boolean {
        val dx = x - cx
        val dy = y - cy
        return sqrt(dx * dx + dy * dy) <= handleRadius * 1.5f
    }

    /**
     * Обрабатываем касания:
     * ACTION_DOWN  — определяем режим (за какую ручку/тело тронули).
     * ACTION_MOVE  — двигаем или растягиваем рамку по dx/dy от предыдущей позиции.
     * ACTION_UP    — сбрасываем режим.
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN -> {
                lastX = x
                lastY = y
                // Определяем, что именно потрогали
                mode = when {
                    isNear(x, y, rect.left,  rect.top)    -> Mode.RESIZE_LT
                    isNear(x, y, rect.right, rect.top)    -> Mode.RESIZE_RT
                    isNear(x, y, rect.left,  rect.bottom) -> Mode.RESIZE_LB
                    isNear(x, y, rect.right, rect.bottom) -> Mode.RESIZE_RB
                    rect.contains(x, y)                   -> Mode.MOVE
                    else                                  -> Mode.NONE
                }
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = x - lastX
                val dy = y - lastY

                when (mode) {
                    Mode.MOVE -> {
                        // Смещаем весь прямоугольник
                        rect.offset(dx, dy)
                        // Не даём выйти за границы View
                        val dxFix = when {
                            rect.left  < 0      -> -rect.left
                            rect.right > width  -> width - rect.right
                            else                -> 0f
                        }
                        val dyFix = when {
                            rect.top    < 0      -> -rect.top
                            rect.bottom > height -> height - rect.bottom
                            else                 -> 0f
                        }
                        rect.offset(dxFix, dyFix)
                    }

                    // Для каждого угла меняем соответствующие стороны, не давая
                    // уменьшить рамку ниже minSize и выйти за границы View
                    Mode.RESIZE_LT -> {
                        rect.left = (rect.left + dx).coerceAtMost(rect.right - minSize)
                        rect.top  = (rect.top  + dy).coerceAtMost(rect.bottom - minSize)
                        if (rect.left < 0) rect.left = 0f
                        if (rect.top  < 0) rect.top  = 0f
                    }
                    Mode.RESIZE_RT -> {
                        rect.right = (rect.right + dx).coerceAtLeast(rect.left + minSize)
                        rect.top   = (rect.top   + dy).coerceAtMost(rect.bottom - minSize)
                        if (rect.right > width) rect.right = width.toFloat()
                        if (rect.top < 0)       rect.top   = 0f
                    }
                    Mode.RESIZE_LB -> {
                        rect.left   = (rect.left   + dx).coerceAtMost(rect.right - minSize)
                        rect.bottom = (rect.bottom + dy).coerceAtLeast(rect.top + minSize)
                        if (rect.left   < 0)      rect.left   = 0f
                        if (rect.bottom > height) rect.bottom = height.toFloat()
                    }
                    Mode.RESIZE_RB -> {
                        rect.right  = (rect.right  + dx).coerceAtLeast(rect.left + minSize)
                        rect.bottom = (rect.bottom + dy).coerceAtLeast(rect.top + minSize)
                        if (rect.right  > width)  rect.right  = width.toFloat()
                        if (rect.bottom > height) rect.bottom = height.toFloat()
                    }
                    else -> {}
                }
                lastX = x
                lastY = y
                invalidate() // Перерисовываем рамку после каждого движения
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mode = Mode.NONE // Жест завершён — сбрасываем режим
            }
        }
        return true
    }

    /**
     * Возвращает копию текущего прямоугольника выделения в координатах этой View.
     * Копия нужна, чтобы внешний код не мог случайно изменить внутреннее состояние.
     */
    fun getCropRect(): RectF = RectF(rect)

    /**
     * Устанавливает прямоугольник снаружи (например, если нужно восстановить состояние).
     */
    fun setCropRect(r: RectF) {
        rect.set(r)
        initialized = true
        invalidate()
    }
}