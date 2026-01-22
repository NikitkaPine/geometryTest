// shapes/RightTriangleHandler.kt
package com.example.camera_test.shapes

import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import kotlin.math.*

class RightTriangleHandler(
    private val fieldRightA: LinearLayout,
    private val fieldRightB: LinearLayout,
    private val fieldRightC: LinearLayout,
    private val fieldRightAngleA: LinearLayout,
    private val fieldRightAngleB: LinearLayout,
    private val fieldRightAngleC: LinearLayout,
    private val inputRightA: EditText,
    private val inputRightB: EditText,
    private val inputRightC: EditText,
    private val inputRightAngleA: EditText,
    private val inputRightAngleB: EditText,
    private val inputRightAngleC: EditText,
    private val inputPerimeter: EditText,
    private val inputArea: EditText,
    private val resultRightBisectorValue: EditText
) : ShapeHandler {

    override fun showFields() {
        fieldRightA.visibility = View.VISIBLE
        fieldRightB.visibility = View.VISIBLE
        fieldRightC.visibility = View.VISIBLE
        fieldRightAngleA.visibility = View.VISIBLE
        fieldRightAngleB.visibility = View.VISIBLE
        fieldRightAngleC.visibility = View.VISIBLE
    }

    override fun calculate() {
        val a = inputRightA.text.toString().toDoubleOrNull()
        val b = inputRightB.text.toString().toDoubleOrNull()
        val c = inputRightC.text.toString().toDoubleOrNull()
        val angleA = inputRightAngleA.text.toString().toDoubleOrNull()?.let { Math.toRadians(it) }
        val angleC = inputRightAngleC.text.toString().toDoubleOrNull()?.let { Math.toRadians(it) }

        var calculatedL: Double? = null

        when {
            // Известны оба катета a и b
            a != null && a > 0 && b != null && b > 0 -> {
                val hypotenuse = sqrt(a.pow(2) + b.pow(2))
                val bisector = (a * b) / hypotenuse
                val angA = atan(a / b)
                val angC = atan(b / a)

                calculatedL = bisector
                setTextIfDifferent(inputRightC, hypotenuse)
                setTextIfDifferent(inputPerimeter, a + b + hypotenuse)
                setTextIfDifferent(inputArea, (a * b) / 2)
                setTextIfDifferent(inputRightAngleA, Math.toDegrees(angA))
                setTextIfDifferent(inputRightAngleC, Math.toDegrees(angC))
            }
            // Известны катет a и гипотенуза c
            a != null && a > 0 && c != null && c > a -> {
                val cathetB = sqrt(c.pow(2) - a.pow(2))
                val bisector = (a * cathetB) / c
                val angA = asin(a / c)
                val angC = acos(a / c)

                calculatedL = bisector
                setTextIfDifferent(inputRightB, cathetB)
                setTextIfDifferent(inputPerimeter, a + cathetB + c)
                setTextIfDifferent(inputArea, (a * cathetB) / 2)
                setTextIfDifferent(inputRightAngleA, Math.toDegrees(angA))
                setTextIfDifferent(inputRightAngleC, Math.toDegrees(angC))
            }
            // Известны катет b и гипотенуза c
            b != null && b > 0 && c != null && c > b -> {
                val cathetA = sqrt(c.pow(2) - b.pow(2))
                val bisector = (cathetA * b) / c
                val angA = asin(cathetA / c)
                val angC = acos(cathetA / c)

                calculatedL = bisector
                setTextIfDifferent(inputRightA, cathetA)
                setTextIfDifferent(inputPerimeter, cathetA + b + c)
                setTextIfDifferent(inputArea, (cathetA * b) / 2)
                setTextIfDifferent(inputRightAngleA, Math.toDegrees(angA))
                setTextIfDifferent(inputRightAngleC, Math.toDegrees(angC))
            }
            // Известны катет a и угол A
            a != null && a > 0 && angleA != null && angleA > 0 && angleA < PI/2 -> {
                val cathetB = a / tan(angleA)
                val hypotenuse = a / sin(angleA)
                val bisector = (a * cathetB) / hypotenuse
                val angC = PI / 2 - angleA

                calculatedL = bisector
                setTextIfDifferent(inputRightB, cathetB)
                setTextIfDifferent(inputRightC, hypotenuse)
                setTextIfDifferent(inputPerimeter, a + cathetB + hypotenuse)
                setTextIfDifferent(inputArea, (a * cathetB) / 2)
                setTextIfDifferent(inputRightAngleC, Math.toDegrees(angC))
            }
            // Известны катет a и угол C
            a != null && a > 0 && angleC != null && angleC > 0 && angleC < PI/2 -> {
                val cathetB = a * tan(angleC)
                val hypotenuse = a / cos(angleC)
                val bisector = (a * cathetB) / hypotenuse
                val angA = PI / 2 - angleC

                calculatedL = bisector
                setTextIfDifferent(inputRightB, cathetB)
                setTextIfDifferent(inputRightC, hypotenuse)
                setTextIfDifferent(inputPerimeter, a + cathetB + hypotenuse)
                setTextIfDifferent(inputArea, (a * cathetB) / 2)
                setTextIfDifferent(inputRightAngleA, Math.toDegrees(angA))
            }
            // Известны катет b и угол A
            b != null && b > 0 && angleA != null && angleA > 0 && angleA < PI/2 -> {
                val cathetA = b * tan(angleA)
                val hypotenuse = b / cos(angleA)
                val bisector = (cathetA * b) / hypotenuse
                val angC = PI / 2 - angleA

                calculatedL = bisector
                setTextIfDifferent(inputRightA, cathetA)
                setTextIfDifferent(inputRightC, hypotenuse)
                setTextIfDifferent(inputPerimeter, cathetA + b + hypotenuse)
                setTextIfDifferent(inputArea, (cathetA * b) / 2)
                setTextIfDifferent(inputRightAngleC, Math.toDegrees(angC))
            }
            // Известны катет b и угол C
            b != null && b > 0 && angleC != null && angleC > 0 && angleC < PI/2 -> {
                val cathetA = b / tan(angleC)
                val hypotenuse = b / sin(angleC)
                val bisector = (cathetA * b) / hypotenuse
                val angA = PI / 2 - angleC

                calculatedL = bisector
                setTextIfDifferent(inputRightA, cathetA)
                setTextIfDifferent(inputRightC, hypotenuse)
                setTextIfDifferent(inputPerimeter, cathetA + b + hypotenuse)
                setTextIfDifferent(inputArea, (cathetA * b) / 2)
                setTextIfDifferent(inputRightAngleA, Math.toDegrees(angA))
            }
            // Известны гипотенуза c и угол A
            c != null && c > 0 && angleA != null && angleA > 0 && angleA < PI/2 -> {
                val cathetA = c * sin(angleA)
                val cathetB = c * cos(angleA)
                val bisector = (cathetA * cathetB) / c
                val angC = PI / 2 - angleA

                calculatedL = bisector
                setTextIfDifferent(inputRightA, cathetA)
                setTextIfDifferent(inputRightB, cathetB)
                setTextIfDifferent(inputPerimeter, cathetA + cathetB + c)
                setTextIfDifferent(inputArea, (cathetA * cathetB) / 2)
                setTextIfDifferent(inputRightAngleC, Math.toDegrees(angC))
            }
            // Известны гипотенуза c и угол C
            c != null && c > 0 && angleC != null && angleC > 0 && angleC < PI/2 -> {
                val cathetB = c * sin(angleC)
                val cathetA = c * cos(angleC)
                val bisector = (cathetA * cathetB) / c
                val angA = PI / 2 - angleC

                calculatedL = bisector
                setTextIfDifferent(inputRightA, cathetA)
                setTextIfDifferent(inputRightB, cathetB)
                setTextIfDifferent(inputPerimeter, cathetA + cathetB + c)
                setTextIfDifferent(inputArea, (cathetA * cathetB) / 2)
                setTextIfDifferent(inputRightAngleA, Math.toDegrees(angA))
            }
        }

        calculatedL?.let { setTextIfDifferent(resultRightBisectorValue, it) }
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