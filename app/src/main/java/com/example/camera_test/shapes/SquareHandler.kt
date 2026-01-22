// shapes/SquareHandler.kt
package com.example.camera_test.shapes

import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.abs

class SquareHandler(
    private val fieldSquareSide: LinearLayout,
    private val fieldSquareDiagonal: LinearLayout,
    private val inputSquareSide: EditText,
    private val inputSquareDiagonal: EditText,
    private val inputPerimeter: EditText,
    private val inputArea: EditText
) : ShapeHandler {

    override fun showFields() {
        fieldSquareSide.visibility = View.VISIBLE
        fieldSquareDiagonal.visibility = View.VISIBLE
    }

    override fun calculate() {
        val a = inputSquareSide.text.toString().toDoubleOrNull()
        val d = inputSquareDiagonal.text.toString().toDoubleOrNull()
        val p = inputPerimeter.text.toString().toDoubleOrNull()
        val s = inputArea.text.toString().toDoubleOrNull()

        when {
            a != null && a > 0 && inputSquareSide.hasFocus() -> {
                setTextIfDifferent(inputSquareDiagonal, a * sqrt(2.0))
                setTextIfDifferent(inputPerimeter, 4 * a)
                setTextIfDifferent(inputArea, a.pow(2))
            }
            d != null && d > 0 && inputSquareDiagonal.hasFocus() -> {
                setTextIfDifferent(inputSquareSide, d / sqrt(2.0))
                setTextIfDifferent(inputPerimeter, 4 * (d / sqrt(2.0)))
                setTextIfDifferent(inputArea, (d.pow(2)) / 2)
            }
            p != null && p > 0 && inputPerimeter.hasFocus() -> {
                setTextIfDifferent(inputSquareSide, p / 4)
                setTextIfDifferent(inputSquareDiagonal, (p / 4) * sqrt(2.0))
                setTextIfDifferent(inputArea, (p / 4).pow(2))
            }
            s != null && s > 0 && inputArea.hasFocus() -> {
                setTextIfDifferent(inputSquareSide, sqrt(s))
                setTextIfDifferent(inputSquareDiagonal, sqrt(s) * sqrt(2.0))
                setTextIfDifferent(inputPerimeter, 4 * sqrt(s))
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