package com.example.camera_test

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class SelectionView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val framePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    private val overlayPaint = Paint().apply {
        color = Color.parseColor("#66000000")
    }

    private val handlePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val handleRadius = 24f

    private var rect = RectF(0f, 0f, 0f, 0f)
    private var initialized = false

    // Для перетаскивания
    private enum class Mode { NONE, MOVE, RESIZE_LT, RESIZE_RT, RESIZE_LB, RESIZE_RB }
    private var mode = Mode.NONE
    private var lastX = 0f
    private var lastY = 0f

    // Минимальные размеры
    private val minSize = 80f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (!initialized && w > 0 && h > 0) {
            initCenteredRect()
        }
    }

    fun initCenteredRect() {
        // Создаём прямоугольник по центру — 60% ширины и 60% высоты view
        val w = width.toFloat()
        val h = height.toFloat()
        val rw = w * 0.6f
        val rh = h * 0.6f
        val left = (w - rw) / 2f
        val top = (h - rh) / 2f
        rect.set(left, top, left + rw, top + rh)
        initialized = true
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // тёмная заливка
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)

        // вырезаем выделенную область (CLEAR)
        val saveCount = canvas.saveLayer(null, null)
        val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }
        canvas.drawRect(rect, clearPaint)
        canvas.restoreToCount(saveCount)

        // рамка
        canvas.drawRect(rect, framePaint)

        // ручки в углах
        drawHandles(canvas)
    }

    private fun drawHandles(canvas: Canvas) {
        // LT
        canvas.drawCircle(rect.left, rect.top, handleRadius, handlePaint)
        // RT
        canvas.drawCircle(rect.right, rect.top, handleRadius, handlePaint)
        // LB
        canvas.drawCircle(rect.left, rect.bottom, handleRadius, handlePaint)
        // RB
        canvas.drawCircle(rect.right, rect.bottom, handleRadius, handlePaint)
    }

    private fun isNear(x: Float, y: Float, cx: Float, cy: Float): Boolean {
        val dx = x - cx
        val dy = y - cy
        return sqrt(dx * dx + dy * dy) <= handleRadius * 1.5f
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = x
                lastY = y
                // сначала ищем ручки
                mode = when {
                    isNear(x, y, rect.left, rect.top) -> Mode.RESIZE_LT
                    isNear(x, y, rect.right, rect.top) -> Mode.RESIZE_RT
                    isNear(x, y, rect.left, rect.bottom) -> Mode.RESIZE_LB
                    isNear(x, y, rect.right, rect.bottom) -> Mode.RESIZE_RB
                    rect.contains(x, y) -> Mode.MOVE
                    else -> Mode.NONE
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = x - lastX
                val dy = y - lastY
                when (mode) {
                    Mode.MOVE -> {
                        rect.offset(dx, dy)
                        // не выйти за границы view
                        val dxFix = when {
                            rect.left < 0 -> -rect.left
                            rect.right > width -> width - rect.right
                            else -> 0f
                        }
                        val dyFix = when {
                            rect.top < 0 -> -rect.top
                            rect.bottom > height -> height - rect.bottom
                            else -> 0f
                        }
                        rect.offset(dxFix, dyFix)
                    }
                    Mode.RESIZE_LT -> {
                        rect.left = (rect.left + dx).coerceAtMost(rect.right - minSize)
                        rect.top = (rect.top + dy).coerceAtMost(rect.bottom - minSize)
                        if (rect.left < 0) rect.left = 0f
                        if (rect.top < 0) rect.top = 0f
                    }
                    Mode.RESIZE_RT -> {
                        rect.right = (rect.right + dx).coerceAtLeast(rect.left + minSize)
                        rect.top = (rect.top + dy).coerceAtMost(rect.bottom - minSize)
                        if (rect.right > width) rect.right = width.toFloat()
                        if (rect.top < 0) rect.top = 0f
                    }
                    Mode.RESIZE_LB -> {
                        rect.left = (rect.left + dx).coerceAtMost(rect.right - minSize)
                        rect.bottom = (rect.bottom + dy).coerceAtLeast(rect.top + minSize)
                        if (rect.left < 0) rect.left = 0f
                        if (rect.bottom > height) rect.bottom = height.toFloat()
                    }
                    Mode.RESIZE_RB -> {
                        rect.right = (rect.right + dx).coerceAtLeast(rect.left + minSize)
                        rect.bottom = (rect.bottom + dy).coerceAtLeast(rect.top + minSize)
                        if (rect.right > width) rect.right = width.toFloat()
                        if (rect.bottom > height) rect.bottom = height.toFloat()
                    }
                    else -> {}
                }
                lastX = x
                lastY = y
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mode = Mode.NONE
            }
        }
        return true
    }

    /**
     * Возвращает rect выделения в координатах view (selectionView)
     */
    fun getCropRect(): RectF {
        // возвращаем копию
        return RectF(rect)
    }

    /**
     * Установить rect внешне (если нужно)
     */
    fun setCropRect(r: RectF) {
        rect.set(r)
        initialized = true
        invalidate()
    }
}
