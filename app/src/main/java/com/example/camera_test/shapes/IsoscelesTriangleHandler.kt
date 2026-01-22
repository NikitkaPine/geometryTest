// shapes/IsoscelesTriangleHandler.kt
package com.example.camera_test.shapes

import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.*

class IsoscelesTriangleHandler(
    private val fieldIsoSide: LinearLayout,
    private val fieldIsoBase: LinearLayout,
    private val fieldIsoAngleA: LinearLayout,
    private val fieldIsoAngleB: LinearLayout,
    private val fieldIsoAngleC: LinearLayout,
    private val inputIsoSide: EditText,
    private val inputIsoBase: EditText,
    private val inputIsoAngleA: EditText,
    private val inputIsoAngleB: EditText,
    private val inputIsoAngleC: EditText,
    private val inputPerimeter: EditText,
    private val inputArea: EditText,
    private val resultIsoBisectorValue: EditText,
    private val tvError: TextView
) : ShapeHandler {

    override fun showFields() {
        fieldIsoSide.visibility = View.VISIBLE
        fieldIsoBase.visibility = View.VISIBLE
        fieldIsoAngleA.visibility = View.VISIBLE
        fieldIsoAngleB.visibility = View.VISIBLE
        fieldIsoAngleC.visibility = View.VISIBLE
    }

    override fun calculate() {
        tvError.visibility = View.GONE

        val a = inputIsoSide.text.toString().toDoubleOrNull()
        val b = inputIsoBase.text.toString().toDoubleOrNull()
        val l = resultIsoBisectorValue.text.toString().toDoubleOrNull()
        val angleA = inputIsoAngleA.text.toString().toDoubleOrNull()?.let { Math.toRadians(it) }
        val angleB = inputIsoAngleB.text.toString().toDoubleOrNull()?.let { Math.toRadians(it) }
        val angleC = inputIsoAngleC.text.toString().toDoubleOrNull()?.let { Math.toRadians(it) }

        // Проверка на валидность углов, если они введены
        val angleASet = angleA != null
        val angleBSet = angleB != null
        val angleCSet = angleC != null
        if (angleASet && angleBSet && angleCSet) {
            val sum = Math.toDegrees(angleA) + Math.toDegrees(angleB) + Math.toDegrees(angleC)
            if (abs(sum - 180) > 0.01 || abs(Math.toDegrees(angleB) - Math.toDegrees(angleC)) > 0.01) {
                tvError.visibility = View.VISIBLE
                return
            }
        }

        var calculatedL: Double? = null

        when {
            resultIsoBisectorValue.hasFocus() && l != null && l > 0 -> {
                when {
                    a != null && a > l -> {
                        val base = 2 * sqrt(a.pow(2) - l.pow(2))
                        if (base <= 0 || base.isNaN()) {
                            tvError.visibility = View.VISIBLE
                            return
                        }
                        val angleAtA = 2 * asin(base / (2 * a))
                        val angleAtBase = (PI - angleAtA) / 2

                        setTextIfDifferent(inputIsoBase, base)
                        setTextIfDifferent(inputPerimeter, 2 * a + base)
                        setTextIfDifferent(inputArea, (base * l) / 2)
                        setTextIfDifferent(inputIsoAngleA, Math.toDegrees(angleAtA))
                        setTextIfDifferent(inputIsoAngleB, Math.toDegrees(angleAtBase))
                        setTextIfDifferent(inputIsoAngleC, Math.toDegrees(angleAtBase))
                    }
                    b != null && b > 0 -> {
                        val side = sqrt(l.pow(2) + (b / 2).pow(2))
                        if (side <= 0 || side.isNaN()) {
                            tvError.visibility = View.VISIBLE
                            return
                        }
                        val angleAtA = 2 * asin(b / (2 * side))
                        val angleAtBase = (PI - angleAtA) / 2

                        setTextIfDifferent(inputIsoSide, side)
                        setTextIfDifferent(inputPerimeter, 2 * side + b)
                        setTextIfDifferent(inputArea, (b * l) / 2)
                        setTextIfDifferent(inputIsoAngleA, Math.toDegrees(angleAtA))
                        setTextIfDifferent(inputIsoAngleB, Math.toDegrees(angleAtBase))
                        setTextIfDifferent(inputIsoAngleC, Math.toDegrees(angleAtBase))
                    }
                    else -> {
                        tvError.visibility = View.VISIBLE
                        return
                    }
                }
            }
            a != null && a > 0 && b != null && b > 0 && a > b/2 -> {
                val h = sqrt(a.pow(2) - (b / 2).pow(2))
                if (h <= 0 || h.isNaN()) {
                    tvError.visibility = View.VISIBLE
                    return
                }
                val angleAtA = 2 * asin(b / (2 * a))
                val angleAtBase = (PI - angleAtA) / 2

                calculatedL = h
                setTextIfDifferent(resultIsoBisectorValue, h)
                setTextIfDifferent(inputPerimeter, 2 * a + b)
                setTextIfDifferent(inputArea, (b * h) / 2)
                setTextIfDifferent(inputIsoAngleA, Math.toDegrees(angleAtA))
                setTextIfDifferent(inputIsoAngleB, Math.toDegrees(angleAtBase))
                setTextIfDifferent(inputIsoAngleC, Math.toDegrees(angleAtBase))
            }
            a != null && a > 0 && angleA != null && angleA > 0 && angleA < PI -> {
                if (angleA >= PI) {
                    tvError.visibility = View.VISIBLE
                    return
                }
                val base = 2 * a * sin(angleA / 2)
                val h = a * cos(angleA / 2)
                if (base <= 0 || h <= 0 || base.isNaN() || h.isNaN()) {
                    tvError.visibility = View.VISIBLE
                    return
                }
                val angleAtBase = (PI - angleA) / 2

                calculatedL = h
                setTextIfDifferent(inputIsoBase, base)
                setTextIfDifferent(resultIsoBisectorValue, h)
                setTextIfDifferent(inputPerimeter, 2 * a + base)
                setTextIfDifferent(inputArea, (base * h) / 2)
                setTextIfDifferent(inputIsoAngleB, Math.toDegrees(angleAtBase))
                setTextIfDifferent(inputIsoAngleC, Math.toDegrees(angleAtBase))
            }
            a != null && a > 0 && angleB != null && angleB > 0 && angleB < PI/2 -> {
                val angleAtA = PI - 2 * angleB
                if (angleAtA <= 0) {
                    tvError.visibility = View.VISIBLE
                    return
                }
                val base = 2 * a * sin(angleAtA / 2)
                val h = a * cos(angleAtA / 2)
                if (base <= 0 || h <= 0 || base.isNaN() || h.isNaN()) {
                    tvError.visibility = View.VISIBLE
                    return
                }

                calculatedL = h
                setTextIfDifferent(inputIsoBase, base)
                setTextIfDifferent(resultIsoBisectorValue, h)
                setTextIfDifferent(inputPerimeter, 2 * a + base)
                setTextIfDifferent(inputArea, (base * h) / 2)
                setTextIfDifferent(inputIsoAngleA, Math.toDegrees(angleAtA))
                setTextIfDifferent(inputIsoAngleC, Math.toDegrees(angleB))
            }
            a != null && a > 0 && angleC != null && angleC > 0 && angleC < PI/2 -> {
                val angleAtA = PI - 2 * angleC
                if (angleAtA <= 0) {
                    tvError.visibility = View.VISIBLE
                    return
                }
                val base = 2 * a * sin(angleAtA / 2)
                val h = a * cos(angleAtA / 2)
                if (base <= 0 || h <= 0 || base.isNaN() || h.isNaN()) {
                    tvError.visibility = View.VISIBLE
                    return
                }

                calculatedL = h
                setTextIfDifferent(inputIsoBase, base)
                setTextIfDifferent(resultIsoBisectorValue, h)
                setTextIfDifferent(inputPerimeter, 2 * a + base)
                setTextIfDifferent(inputArea, (base * h) / 2)
                setTextIfDifferent(inputIsoAngleA, Math.toDegrees(angleAtA))
                setTextIfDifferent(inputIsoAngleB, Math.toDegrees(angleC))
            }
            b != null && b > 0 && angleA != null && angleA > 0 && angleA < PI -> {
                if (angleA >= PI) {
                    tvError.visibility = View.VISIBLE
                    return
                }
                val side = b / (2 * sin(angleA / 2))
                val h = side * cos(angleA / 2)
                if (side <= 0 || h <= 0 || side.isNaN() || h.isNaN()) {
                    tvError.visibility = View.VISIBLE
                    return
                }
                val angleAtBase = (PI - angleA) / 2

                calculatedL = h
                setTextIfDifferent(inputIsoSide, side)
                setTextIfDifferent(resultIsoBisectorValue, h)
                setTextIfDifferent(inputPerimeter, 2 * side + b)
                setTextIfDifferent(inputArea, (b * h) / 2)
                setTextIfDifferent(inputIsoAngleB, Math.toDegrees(angleAtBase))
                setTextIfDifferent(inputIsoAngleC, Math.toDegrees(angleAtBase))
            }
            b != null && b > 0 && angleB != null && angleB > 0 && angleB < PI/2 -> {
                val angleAtA = PI - 2 * angleB
                if (angleAtA <= 0) {
                    tvError.visibility = View.VISIBLE
                    return
                }
                val side = b / (2 * sin(angleAtA / 2))
                val h = side * cos(angleAtA / 2)
                if (side <= 0 || h <= 0 || side.isNaN() || h.isNaN()) {
                    tvError.visibility = View.VISIBLE
                    return
                }

                calculatedL = h
                setTextIfDifferent(inputIsoSide, side)
                setTextIfDifferent(resultIsoBisectorValue, h)
                setTextIfDifferent(inputPerimeter, 2 * side + b)
                setTextIfDifferent(inputArea, (b * h) / 2)
                setTextIfDifferent(inputIsoAngleA, Math.toDegrees(angleAtA))
                setTextIfDifferent(inputIsoAngleC, Math.toDegrees(angleB))
            }
            b != null && b > 0 && angleC != null && angleC > 0 && angleC < PI/2 -> {
                val angleAtA = PI - 2 * angleC
                if (angleAtA <= 0) {
                    tvError.visibility = View.VISIBLE
                    return
                }
                val side = b / (2 * sin(angleAtA / 2))
                val h = side * cos(angleAtA / 2)
                if (side <= 0 || h <= 0 || side.isNaN() || h.isNaN()) {
                    tvError.visibility = View.VISIBLE
                    return
                }

                calculatedL = h
                setTextIfDifferent(inputIsoSide, side)
                setTextIfDifferent(resultIsoBisectorValue, h)
                setTextIfDifferent(inputPerimeter, 2 * side + b)
                setTextIfDifferent(inputArea, (b * h) / 2)
                setTextIfDifferent(inputIsoAngleA, Math.toDegrees(angleAtA))
                setTextIfDifferent(inputIsoAngleB, Math.toDegrees(angleC))
            }
            else -> {
                // Если ввод некорректен
                tvError.visibility = View.VISIBLE
            }
        }

        calculatedL?.let { setTextIfDifferent(resultIsoBisectorValue, it) }
    }

    override fun setTextIfDifferent(editText: EditText, value: Double) {
        if (value.isNaN() || value.isInfinite() || value <= 0) return

        val formattedValue = String.format("%.2f", value)
        val currentText = editText.text.toString()

        if (currentText.isEmpty() || !editText.hasFocus()) {
            val currentValue = currentText.toDoubleOrNull()
            if (currentValue == null || abs(currentValue - value) > 0.01) {
                editText.setText(formattedValue)
            }
        }
    }
}