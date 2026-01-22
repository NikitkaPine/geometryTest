// shapes/RightTriangleHandler.kt
package com.example.camera_test.shapes

import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
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
    private val resultRightBisectorValue: EditText,
    private val tvError: TextView
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
        tvError.visibility = View.GONE

        val a = inputRightA.text.toString().toDoubleOrNull()
        val b = inputRightB.text.toString().toDoubleOrNull()
        val c = inputRightC.text.toString().toDoubleOrNull()
        val l = resultRightBisectorValue.text.toString().toDoubleOrNull()
        val angleA = inputRightAngleA.text.toString().toDoubleOrNull()?.let { Math.toRadians(it) }
        val angleC = inputRightAngleC.text.toString().toDoubleOrNull()?.let { Math.toRadians(it) }

        // Checking the validity of angles
        val angleASet = angleA != null
        val angleCSet = angleC != null
        if (angleASet && angleCSet) {
            val sum = Math.toDegrees(angleA) + Math.toDegrees(angleC)
            if (abs(sum - 90) > 0.01 || Math.toDegrees(angleA) <= 0 || Math.toDegrees(angleC) <= 0) {
                tvError.visibility = View.VISIBLE
                return
            }
        }

        var calculatedL: Double? = null

        when {
            resultRightBisectorValue.hasFocus() && l != null && l > 0 -> {
                when {
                    a != null && a > 0 -> {
                        val sqrt2 = sqrt(2.0)
                        if (sqrt2 * a <= l) {
                            tvError.visibility = View.VISIBLE
                            return
                        }
                        val bCalc = (l * a) / (a * sqrt2 - l)
                        if (bCalc <= 0 || bCalc.isNaN()) {
                            tvError.visibility = View.VISIBLE
                            return
                        }
                        val hypotenuse = sqrt(a.pow(2) + bCalc.pow(2))
                        val angA = atan(a / bCalc)
                        val angC = atan(bCalc / a)

                        setTextIfDifferent(inputRightB, bCalc)
                        setTextIfDifferent(inputRightC, hypotenuse)
                        setTextIfDifferent(inputPerimeter, a + bCalc + hypotenuse)
                        setTextIfDifferent(inputArea, (a * bCalc) / 2)
                        setTextIfDifferent(inputRightAngleA, Math.toDegrees(angA))
                        setTextIfDifferent(inputRightAngleC, Math.toDegrees(angC))
                    }
                    b != null && b > 0 -> {
                        val sqrt2 = sqrt(2.0)
                        if (sqrt2 * b <= l) {
                            tvError.visibility = View.VISIBLE
                            return
                        }
                        val aCalc = (l * b) / (b * sqrt2 - l)
                        if (aCalc <= 0 || aCalc.isNaN()) {
                            tvError.visibility = View.VISIBLE
                            return
                        }
                        val hypotenuse = sqrt(aCalc.pow(2) + b.pow(2))
                        val angA = atan(aCalc / b)
                        val angC = atan(b / aCalc)

                        setTextIfDifferent(inputRightA, aCalc)
                        setTextIfDifferent(inputRightC, hypotenuse)
                        setTextIfDifferent(inputPerimeter, aCalc + b + hypotenuse)
                        setTextIfDifferent(inputArea, (aCalc * b) / 2)
                        setTextIfDifferent(inputRightAngleA, Math.toDegrees(angA))
                        setTextIfDifferent(inputRightAngleC, Math.toDegrees(angC))
                    }
                    else -> {
                        tvError.visibility = View.VISIBLE
                        return
                    }
                }
            }
            a != null && a > 0 && b != null && b > 0 -> {
                val hypotenuse = sqrt(a.pow(2) + b.pow(2))
                val bisector = (a * b * sqrt(2.0)) / (a + b)
                val angA = atan(a / b)
                val angC = atan(b / a)

                calculatedL = bisector
                setTextIfDifferent(inputRightC, hypotenuse)
                setTextIfDifferent(resultRightBisectorValue, bisector)
                setTextIfDifferent(inputPerimeter, a + b + hypotenuse)
                setTextIfDifferent(inputArea, (a * b) / 2)
                setTextIfDifferent(inputRightAngleA, Math.toDegrees(angA))
                setTextIfDifferent(inputRightAngleC, Math.toDegrees(angC))
            }
            a != null && a > 0 && c != null && c > a -> {
                val cathetB = sqrt(c.pow(2) - a.pow(2))
                if (cathetB <= 0 || cathetB.isNaN()) {
                    tvError.visibility = View.VISIBLE
                    return
                }
                val bisector = (a * cathetB * sqrt(2.0)) / (a + cathetB)
                val angA = asin(a / c)
                val angC = acos(a / c)

                calculatedL = bisector
                setTextIfDifferent(inputRightB, cathetB)
                setTextIfDifferent(resultRightBisectorValue, bisector)
                setTextIfDifferent(inputPerimeter, a + cathetB + c)
                setTextIfDifferent(inputArea, (a * cathetB) / 2)
                setTextIfDifferent(inputRightAngleA, Math.toDegrees(angA))
                setTextIfDifferent(inputRightAngleC, Math.toDegrees(angC))
            }
            b != null && b > 0 && c != null && c > b -> {
                val cathetA = sqrt(c.pow(2) - b.pow(2))
                if (cathetA <= 0 || cathetA.isNaN()) {
                    tvError.visibility = View.VISIBLE
                    return
                }
                val bisector = (cathetA * b * sqrt(2.0)) / (cathetA + b)
                val angA = asin(cathetA / c)
                val angC = acos(cathetA / c)

                calculatedL = bisector
                setTextIfDifferent(inputRightA, cathetA)
                setTextIfDifferent(resultRightBisectorValue, bisector)
                setTextIfDifferent(inputPerimeter, cathetA + b + c)
                setTextIfDifferent(inputArea, (cathetA * b) / 2)
                setTextIfDifferent(inputRightAngleA, Math.toDegrees(angA))
                setTextIfDifferent(inputRightAngleC, Math.toDegrees(angC))
            }
            a != null && a > 0 && angleA != null && angleA > 0 && angleA < PI/2 -> {
                val cathetB = a / tan(angleA)
                if (cathetB <= 0 || cathetB.isNaN()) {
                    tvError.visibility = View.VISIBLE
                    return
                }
                val hypotenuse = a / sin(angleA)
                val bisector = (a * cathetB * sqrt(2.0)) / (a + cathetB)
                val angC = PI / 2 - angleA

                calculatedL = bisector
                setTextIfDifferent(inputRightB, cathetB)
                setTextIfDifferent(inputRightC, hypotenuse)
                setTextIfDifferent(resultRightBisectorValue, bisector)
                setTextIfDifferent(inputPerimeter, a + cathetB + hypotenuse)
                setTextIfDifferent(inputArea, (a * cathetB) / 2)
                setTextIfDifferent(inputRightAngleC, Math.toDegrees(angC))
            }
            a != null && a > 0 && angleC != null && angleC > 0 && angleC < PI/2 -> {
                val cathetB = a * tan(angleC)
                if (cathetB <= 0 || cathetB.isNaN()) {
                    tvError.visibility = View.VISIBLE
                    return
                }
                val hypotenuse = a / cos(angleC)
                val bisector = (a * cathetB * sqrt(2.0)) / (a + cathetB)
                val angA = PI / 2 - angleC

                calculatedL = bisector
                setTextIfDifferent(inputRightB, cathetB)
                setTextIfDifferent(inputRightC, hypotenuse)
                setTextIfDifferent(resultRightBisectorValue, bisector)
                setTextIfDifferent(inputPerimeter, a + cathetB + hypotenuse)
                setTextIfDifferent(inputArea, (a * cathetB) / 2)
                setTextIfDifferent(inputRightAngleA, Math.toDegrees(angA))
            }
            b != null && b > 0 && angleA != null && angleA > 0 && angleA < PI/2 -> {
                val cathetA = b * tan(angleA)
                if (cathetA <= 0 || cathetA.isNaN()) {
                    tvError.visibility = View.VISIBLE
                    return
                }
                val hypotenuse = b / cos(angleA)
                val bisector = (cathetA * b * sqrt(2.0)) / (cathetA + b)
                val angC = PI / 2 - angleA

                calculatedL = bisector
                setTextIfDifferent(inputRightA, cathetA)
                setTextIfDifferent(inputRightC, hypotenuse)
                setTextIfDifferent(resultRightBisectorValue, bisector)
                setTextIfDifferent(inputPerimeter, cathetA + b + hypotenuse)
                setTextIfDifferent(inputArea, (cathetA * b) / 2)
                setTextIfDifferent(inputRightAngleC, Math.toDegrees(angC))
            }
            b != null && b > 0 && angleC != null && angleC > 0 && angleC < PI/2 -> {
                val cathetA = b / tan(angleC)
                if (cathetA <= 0 || cathetA.isNaN()) {
                    tvError.visibility = View.VISIBLE
                    return
                }
                val hypotenuse = b / sin(angleC)
                val bisector = (cathetA * b * sqrt(2.0)) / (cathetA + b)
                val angA = PI / 2 - angleC

                calculatedL = bisector
                setTextIfDifferent(inputRightA, cathetA)
                setTextIfDifferent(inputRightC, hypotenuse)
                setTextIfDifferent(resultRightBisectorValue, bisector)
                setTextIfDifferent(inputPerimeter, cathetA + b + hypotenuse)
                setTextIfDifferent(inputArea, (cathetA * b) / 2)
                setTextIfDifferent(inputRightAngleA, Math.toDegrees(angA))
            }
            c != null && c > 0 && angleA != null && angleA > 0 && angleA < PI/2 -> {
                val cathetA = c * sin(angleA)
                val cathetB = c * cos(angleA)
                if (cathetA <= 0 || cathetB <= 0 || cathetA.isNaN() || cathetB.isNaN()) {
                    tvError.visibility = View.VISIBLE
                    return
                }
                val bisector = (cathetA * cathetB * sqrt(2.0)) / (cathetA + cathetB)
                val angC = PI / 2 - angleA

                calculatedL = bisector
                setTextIfDifferent(inputRightA, cathetA)
                setTextIfDifferent(inputRightB, cathetB)
                setTextIfDifferent(resultRightBisectorValue, bisector)
                setTextIfDifferent(inputPerimeter, cathetA + cathetB + c)
                setTextIfDifferent(inputArea, (cathetA * cathetB) / 2)
                setTextIfDifferent(inputRightAngleC, Math.toDegrees(angC))
            }
            c != null && c > 0 && angleC != null && angleC > 0 && angleC < PI/2 -> {
                val cathetB = c * sin(angleC)
                val cathetA = c * cos(angleC)
                if (cathetA <= 0 || cathetB <= 0 || cathetA.isNaN() || cathetB.isNaN()) {
                    tvError.visibility = View.VISIBLE
                    return
                }
                val bisector = (cathetA * cathetB * sqrt(2.0)) / (cathetA + cathetB)
                val angA = PI / 2 - angleC

                calculatedL = bisector
                setTextIfDifferent(inputRightA, cathetA)
                setTextIfDifferent(inputRightB, cathetB)
                setTextIfDifferent(resultRightBisectorValue, bisector)
                setTextIfDifferent(inputPerimeter, cathetA + cathetB + c)
                setTextIfDifferent(inputArea, (cathetA * cathetB) / 2)
                setTextIfDifferent(inputRightAngleA, Math.toDegrees(angA))
            }
            else -> {
                tvError.visibility = View.VISIBLE
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