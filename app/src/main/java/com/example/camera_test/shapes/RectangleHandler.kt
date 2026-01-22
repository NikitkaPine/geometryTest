// shapes/RectangleHandler.kt
package com.example.camera_test.shapes

import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.abs

class RectangleHandler(
    private val fieldRectWidth: LinearLayout,
    private val fieldRectHeight: LinearLayout,
    private val fieldRectDiagonal: LinearLayout,
    private val inputRectWidth: EditText,
    private val inputRectHeight: EditText,
    private val inputRectDiagonal: EditText,
    private val inputPerimeter: EditText,
    private val inputArea: EditText
) : ShapeHandler {

    override fun showFields() {
        fieldRectWidth.visibility = View.VISIBLE
        fieldRectHeight.visibility = View.VISIBLE
        fieldRectDiagonal.visibility = View.VISIBLE
    }

    override fun calculate() {
        val a = inputRectWidth.text.toString().toDoubleOrNull()
        val b = inputRectHeight.text.toString().toDoubleOrNull()
        val d = inputRectDiagonal.text.toString().toDoubleOrNull()
        val p = inputPerimeter.text.toString().toDoubleOrNull()
        val s = inputArea.text.toString().toDoubleOrNull()

        when {
            a != null && a > 0 && b != null && b > 0 -> {
                setTextIfDifferent(inputRectDiagonal, sqrt(a.pow(2) + b.pow(2)))
                setTextIfDifferent(inputPerimeter, 2 * (a + b))
                setTextIfDifferent(inputArea, a * b)
            }
            a != null && a > 0 && d != null && d > a -> {
                val bCalc = sqrt(d.pow(2) - a.pow(2))
                setTextIfDifferent(inputRectHeight, bCalc)
                setTextIfDifferent(inputPerimeter, 2 * (a + bCalc))
                setTextIfDifferent(inputArea, a * bCalc)
            }
            b != null && b > 0 && d != null && d > b -> {
                val aCalc = sqrt(d.pow(2) - b.pow(2))
                setTextIfDifferent(inputRectWidth, aCalc)
                setTextIfDifferent(inputPerimeter, 2 * (aCalc + b))
                setTextIfDifferent(inputArea, aCalc * b)
            }
            a != null && a > 0 && p != null && p > 2 * a && inputPerimeter.hasFocus() -> {
                val bCalc = p / 2 - a
                setTextIfDifferent(inputRectHeight, bCalc)
                setTextIfDifferent(inputRectDiagonal, sqrt(a.pow(2) + bCalc.pow(2)))
                setTextIfDifferent(inputArea, a * bCalc)
            }
            a != null && a > 0 && s != null && s > 0 && inputArea.hasFocus() -> {
                val bCalc = s / a
                setTextIfDifferent(inputRectHeight, bCalc)
                setTextIfDifferent(inputRectDiagonal, sqrt(a.pow(2) + bCalc.pow(2)))
                setTextIfDifferent(inputPerimeter, 2 * (a + bCalc))
            }
            b != null && b > 0 && p != null && p > 2 * b && inputPerimeter.hasFocus() -> {
                val aCalc = p / 2 - b
                setTextIfDifferent(inputRectWidth, aCalc)
                setTextIfDifferent(inputRectDiagonal, sqrt(aCalc.pow(2) + b.pow(2)))
                setTextIfDifferent(inputArea, aCalc * b)
            }
            b != null && b > 0 && s != null && s > 0 && inputArea.hasFocus() -> {
                val aCalc = s / b
                setTextIfDifferent(inputRectWidth, aCalc)
                setTextIfDifferent(inputRectDiagonal, sqrt(aCalc.pow(2) + b.pow(2)))
                setTextIfDifferent(inputPerimeter, 2 * (aCalc + b))
            }
        }
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