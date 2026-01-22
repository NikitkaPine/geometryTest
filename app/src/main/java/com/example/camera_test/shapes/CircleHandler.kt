// shapes/CircleHandler.kt
package com.example.camera_test.shapes

import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.abs

class CircleHandler(
    private val fieldRadius: LinearLayout,
    private val fieldDiameter: LinearLayout,
    private val inputRadius: EditText,
    private val inputDiameter: EditText,
    private val inputPerimeter: EditText,
    private val inputArea: EditText
) : ShapeHandler {

    override fun showFields() {
        fieldRadius.visibility = View.VISIBLE
        fieldDiameter.visibility = View.VISIBLE
    }

    override fun calculate() {
        val r = inputRadius.text.toString().toDoubleOrNull()
        val d = inputDiameter.text.toString().toDoubleOrNull()
        val p = inputPerimeter.text.toString().toDoubleOrNull()
        val s = inputArea.text.toString().toDoubleOrNull()

        when {
            r != null && r > 0 && inputRadius.hasFocus() -> {
                setTextIfDifferent(inputDiameter, 2 * r)
                setTextIfDifferent(inputPerimeter, 2 * PI * r)
                setTextIfDifferent(inputArea, PI * r.pow(2))
            }
            d != null && d > 0 && inputDiameter.hasFocus() -> {
                setTextIfDifferent(inputRadius, d / 2)
                setTextIfDifferent(inputPerimeter, PI * d)
                setTextIfDifferent(inputArea, PI * (d / 2).pow(2))
            }
            p != null && p > 0 && inputPerimeter.hasFocus() -> {
                setTextIfDifferent(inputRadius, p / (2 * PI))
                setTextIfDifferent(inputDiameter, p / PI)
                setTextIfDifferent(inputArea, PI * (p / (2 * PI)).pow(2))
            }
            s != null && s > 0 && inputArea.hasFocus() -> {
                setTextIfDifferent(inputRadius, sqrt(s / PI))
                setTextIfDifferent(inputDiameter, 2 * sqrt(s / PI))
                setTextIfDifferent(inputPerimeter, 2 * PI * sqrt(s / PI))
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