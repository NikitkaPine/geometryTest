// shapes/IsoscelesTriangleHandler.kt
package com.example.camera_test.shapes

import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
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
    private val resultIsoBisectorValue: EditText
) : ShapeHandler {

    override fun showFields() {
        fieldIsoSide.visibility = View.VISIBLE
        fieldIsoBase.visibility = View.VISIBLE
        fieldIsoAngleA.visibility = View.VISIBLE
        fieldIsoAngleB.visibility = View.VISIBLE
        fieldIsoAngleC.visibility = View.VISIBLE
    }

    override fun calculate() {
        val a = inputIsoSide.text.toString().toDoubleOrNull()
        val b = inputIsoBase.text.toString().toDoubleOrNull()
        val angleA = inputIsoAngleA.text.toString().toDoubleOrNull()?.let { Math.toRadians(it) }
        val angleB = inputIsoAngleB.text.toString().toDoubleOrNull()?.let { Math.toRadians(it) }
        val angleC = inputIsoAngleC.text.toString().toDoubleOrNull()?.let { Math.toRadians(it) }

        var calculatedL: Double? = null

        when {
            // Известны обе стороны a и b
            a != null && a > 0 && b != null && b > 0 && a > b/2 -> {
                val h = sqrt(a.pow(2) - (b / 2).pow(2))
                val angleAtA = 2 * asin(b / (2 * a))
                val angleAtBase = (PI - angleAtA) / 2

                calculatedL = h
                setTextIfDifferent(inputPerimeter, 2 * a + b)
                setTextIfDifferent(inputArea, (b * h) / 2)
                setTextIfDifferent(inputIsoAngleA, Math.toDegrees(angleAtA))
                setTextIfDifferent(inputIsoAngleB, Math.toDegrees(angleAtBase))
                setTextIfDifferent(inputIsoAngleC, Math.toDegrees(angleAtBase))
            }
            // Известна боковая сторона a и угол при вершине A
            a != null && a > 0 && angleA != null && angleA > 0 && angleA < PI -> {
                val base = 2 * a * sin(angleA / 2)
                val h = a * cos(angleA / 2)
                val angleAtBase = (PI - angleA) / 2

                calculatedL = h
                setTextIfDifferent(inputIsoBase, base)
                setTextIfDifferent(inputPerimeter, 2 * a + base)
                setTextIfDifferent(inputArea, (base * h) / 2)
                setTextIfDifferent(inputIsoAngleB, Math.toDegrees(angleAtBase))
                setTextIfDifferent(inputIsoAngleC, Math.toDegrees(angleAtBase))
            }
            // Известна боковая сторона a и угол при основании B
            a != null && a > 0 && angleB != null && angleB > 0 && angleB < PI/2 -> {
                val angleAtA = PI - 2 * angleB
                val base = 2 * a * sin(angleAtA / 2)
                val h = a * cos(angleAtA / 2)

                calculatedL = h
                setTextIfDifferent(inputIsoBase, base)
                setTextIfDifferent(inputPerimeter, 2 * a + base)
                setTextIfDifferent(inputArea, (base * h) / 2)
                setTextIfDifferent(inputIsoAngleA, Math.toDegrees(angleAtA))
                setTextIfDifferent(inputIsoAngleC, Math.toDegrees(angleB))
            }
            // Известна боковая сторона a и угол при основании C
            a != null && a > 0 && angleC != null && angleC > 0 && angleC < PI/2 -> {
                val angleAtA = PI - 2 * angleC
                val base = 2 * a * sin(angleAtA / 2)
                val h = a * cos(angleAtA / 2)

                calculatedL = h
                setTextIfDifferent(inputIsoBase, base)
                setTextIfDifferent(inputPerimeter, 2 * a + base)
                setTextIfDifferent(inputArea, (base * h) / 2)
                setTextIfDifferent(inputIsoAngleA, Math.toDegrees(angleAtA))
                setTextIfDifferent(inputIsoAngleB, Math.toDegrees(angleC))
            }
            // Известно основание b и угол при вершине A
            b != null && b > 0 && angleA != null && angleA > 0 && angleA < PI -> {
                val side = b / (2 * sin(angleA / 2))
                val h = side * cos(angleA / 2)
                val angleAtBase = (PI - angleA) / 2

                calculatedL = h
                setTextIfDifferent(inputIsoSide, side)
                setTextIfDifferent(inputPerimeter, 2 * side + b)
                setTextIfDifferent(inputArea, (b * h) / 2)
                setTextIfDifferent(inputIsoAngleB, Math.toDegrees(angleAtBase))
                setTextIfDifferent(inputIsoAngleC, Math.toDegrees(angleAtBase))
            }
            // Известно основание b и угол при основании B
            b != null && b > 0 && angleB != null && angleB > 0 && angleB < PI/2 -> {
                val angleAtA = PI - 2 * angleB
                val side = b / (2 * sin(angleAtA / 2))
                val h = side * cos(angleAtA / 2)

                calculatedL = h
                setTextIfDifferent(inputIsoSide, side)
                setTextIfDifferent(inputPerimeter, 2 * side + b)
                setTextIfDifferent(inputArea, (b * h) / 2)
                setTextIfDifferent(inputIsoAngleA, Math.toDegrees(angleAtA))
                setTextIfDifferent(inputIsoAngleC, Math.toDegrees(angleB))
            }
            // Известно основание b и угол при основании C
            b != null && b > 0 && angleC != null && angleC > 0 && angleC < PI/2 -> {
                val angleAtA = PI - 2 * angleC
                val side = b / (2 * sin(angleAtA / 2))
                val h = side * cos(angleAtA / 2)

                calculatedL = h
                setTextIfDifferent(inputIsoSide, side)
                setTextIfDifferent(inputPerimeter, 2 * side + b)
                setTextIfDifferent(inputArea, (b * h) / 2)
                setTextIfDifferent(inputIsoAngleA, Math.toDegrees(angleAtA))
                setTextIfDifferent(inputIsoAngleB, Math.toDegrees(angleC))
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