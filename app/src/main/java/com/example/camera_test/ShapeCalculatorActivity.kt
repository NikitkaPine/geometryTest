package com.example.camera_test

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.*

enum class ShapeType {
    NONE, CIRCLE, SQUARE, RECTANGLE, ISOSCELES_TRIANGLE, RIGHT_TRIANGLE
}

class ShapeCalculatorActivity : AppCompatActivity() {

    private lateinit var shapeCanvas: ShapeCanvasView
    private lateinit var shapeSpinner: Spinner
    private lateinit var inputContainerLeft: LinearLayout
    private lateinit var inputContainerRight: LinearLayout
    private lateinit var resultsContainer: LinearLayout
    private lateinit var btnReset: ImageButton

    private var currentShapeType: ShapeType = ShapeType.NONE
    private var isCalculating = false

    // Поля ввода параметров
    private lateinit var inputRadius: EditText
    private lateinit var inputDiameter: EditText
    private lateinit var inputSquareSide: EditText
    private lateinit var inputRectWidth: EditText
    private lateinit var inputRectHeight: EditText
    private lateinit var inputIsoSide: EditText
    private lateinit var inputIsoBase: EditText
    private lateinit var inputIsoAngleA: EditText
    private lateinit var inputIsoAngleB: EditText
    private lateinit var inputIsoAngleC: EditText
    private lateinit var inputRightA: EditText
    private lateinit var inputRightB: EditText
    private lateinit var inputRightC: EditText
    private lateinit var inputRightAngleA: EditText
    private lateinit var inputRightAngleB: EditText
    private lateinit var inputRightAngleC: EditText

    // Поля результатов
    private lateinit var inputPerimeter: EditText
    private lateinit var inputArea: EditText

    // Контейнеры полей
    private lateinit var fieldRadius: LinearLayout
    private lateinit var fieldDiameter: LinearLayout
    private lateinit var fieldSquareSide: LinearLayout
    private lateinit var fieldRectWidth: LinearLayout
    private lateinit var fieldRectHeight: LinearLayout
    private lateinit var fieldIsoSide: LinearLayout
    private lateinit var fieldIsoBase: LinearLayout
    private lateinit var fieldIsoAngleA: LinearLayout
    private lateinit var fieldIsoAngleB: LinearLayout
    private lateinit var fieldIsoAngleC: LinearLayout
    private lateinit var fieldRightA: LinearLayout
    private lateinit var fieldRightB: LinearLayout
    private lateinit var fieldRightC: LinearLayout
    private lateinit var fieldRightAngleA: LinearLayout
    private lateinit var fieldRightAngleB: LinearLayout
    private lateinit var fieldRightAngleC: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shape_calculator)

        try {
            initViews()
            setupSpinner()
            setupAutoCalculation()
            setupResetButton()
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(this, "Ошибка: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    private fun initViews() {
        shapeCanvas = findViewById(R.id.shape_canvas)
        shapeSpinner = findViewById(R.id.shape_spinner)
        inputContainerLeft = findViewById(R.id.input_container_left)
        inputContainerRight = findViewById(R.id.input_container_right)
        resultsContainer = findViewById(R.id.results_container)
        btnReset = findViewById(R.id.btn_reset)

        inputRadius = findViewById(R.id.input_radius)
        inputDiameter = findViewById(R.id.input_diameter)
        inputSquareSide = findViewById(R.id.input_square_side)
        inputRectWidth = findViewById(R.id.input_rect_width)
        inputRectHeight = findViewById(R.id.input_rect_height)
        inputIsoSide = findViewById(R.id.input_iso_side)
        inputIsoBase = findViewById(R.id.input_iso_base)
        inputIsoAngleA = findViewById(R.id.input_iso_angle_a)
        inputIsoAngleB = findViewById(R.id.input_iso_angle_b)
        inputIsoAngleC = findViewById(R.id.input_iso_angle_c)
        inputRightA = findViewById(R.id.input_right_a)
        inputRightB = findViewById(R.id.input_right_b)
        inputRightC = findViewById(R.id.input_right_c)
        inputRightAngleA = findViewById(R.id.input_right_angle_a)
        inputRightAngleB = findViewById(R.id.input_right_angle_b)
        inputRightAngleC = findViewById(R.id.input_right_angle_c)

        inputPerimeter = findViewById(R.id.input_perimeter)
        inputArea = findViewById(R.id.input_area)

        fieldRadius = findViewById(R.id.field_radius)
        fieldDiameter = findViewById(R.id.field_diameter)
        fieldSquareSide = findViewById(R.id.field_square_side)
        fieldRectWidth = findViewById(R.id.field_rect_width)
        fieldRectHeight = findViewById(R.id.field_rect_height)
        fieldIsoSide = findViewById(R.id.field_iso_side)
        fieldIsoBase = findViewById(R.id.field_iso_base)
        fieldIsoAngleA = findViewById(R.id.field_iso_angle_a)
        fieldIsoAngleB = findViewById(R.id.field_iso_angle_b)
        fieldIsoAngleC = findViewById(R.id.field_iso_angle_c)
        fieldRightA = findViewById(R.id.field_right_a)
        fieldRightB = findViewById(R.id.field_right_b)
        fieldRightC = findViewById(R.id.field_right_c)
        fieldRightAngleA = findViewById(R.id.field_right_angle_a)
        fieldRightAngleB = findViewById(R.id.field_right_angle_b)
        fieldRightAngleC = findViewById(R.id.field_right_angle_c)
    }

    private fun setupSpinner() {
        val shapes = arrayOf(
            "Выберите фигуру", "Круг", "Квадрат", "Прямоугольник",
            "Треугольник равнобедренный", "Треугольник прямоугольный"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, shapes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        shapeSpinner.adapter = adapter

        shapeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> { currentShapeType = ShapeType.NONE; hideAllFields() }
                    1 -> { currentShapeType = ShapeType.CIRCLE; showCircleFields() }
                    2 -> { currentShapeType = ShapeType.SQUARE; showSquareFields() }
                    3 -> { currentShapeType = ShapeType.RECTANGLE; showRectangleFields() }
                    4 -> { currentShapeType = ShapeType.ISOSCELES_TRIANGLE; showIsoscelesFields() }
                    5 -> { currentShapeType = ShapeType.RIGHT_TRIANGLE; showRightTriangleFields() }
                }
                shapeCanvas.setShape(currentShapeType)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { hideAllFields() }
        }
    }

    private fun setupAutoCalculation() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!isCalculating) {
                    calculateResults()
                }
            }
        }

        listOf(
            inputRadius, inputDiameter, inputSquareSide, inputRectWidth, inputRectHeight,
            inputIsoSide, inputIsoBase, inputIsoAngleA, inputIsoAngleB, inputIsoAngleC,
            inputRightA, inputRightB, inputRightC, inputRightAngleA, inputRightAngleC,
            inputPerimeter, inputArea
        ).forEach { it.addTextChangedListener(textWatcher) }
    }

    private fun setupResetButton() {
        btnReset.setOnClickListener {
            resetAllFields()
        }
    }

    private fun resetAllFields() {
        isCalculating = true

        // Очищаем все поля ввода
        listOf(
            inputRadius, inputDiameter, inputSquareSide, inputRectWidth, inputRectHeight,
            inputIsoSide, inputIsoBase, inputIsoAngleA, inputIsoAngleB, inputIsoAngleC,
            inputRightA, inputRightB, inputRightC, inputRightAngleA, inputRightAngleC,
            inputPerimeter, inputArea
        ).forEach { it.setText("") }

        // Восстанавливаем значение 90° для прямого угла
        if (currentShapeType == ShapeType.RIGHT_TRIANGLE) {
            inputRightAngleB.setText("90")
        }

        isCalculating = false

        android.widget.Toast.makeText(this, "Все значения сброшены", android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun calculateResults() {
        isCalculating = true
        try {
            when (currentShapeType) {
                ShapeType.CIRCLE -> calculateCircle()
                ShapeType.SQUARE -> calculateSquare()
                ShapeType.RECTANGLE -> calculateRectangle()
                ShapeType.ISOSCELES_TRIANGLE -> calculateIsoscelesTriangle()
                ShapeType.RIGHT_TRIANGLE -> calculateRightTriangle()
                else -> {}
            }
        } catch (e: Exception) {
            // Игнорируем ошибки при неполном вводе
        } finally {
            isCalculating = false
        }
    }

    private fun calculateCircle() {
        val r = inputRadius.text.toString().toDoubleOrNull()
        val d = inputDiameter.text.toString().toDoubleOrNull()
        val p = inputPerimeter.text.toString().toDoubleOrNull()
        val s = inputArea.text.toString().toDoubleOrNull()

        when {
            r != null && inputRadius.hasFocus() -> {
                setTextIfDifferent(inputDiameter, 2 * r)
                setTextIfDifferent(inputPerimeter, 2 * PI * r)
                setTextIfDifferent(inputArea, PI * r.pow(2))
            }
            d != null && inputDiameter.hasFocus() -> {
                setTextIfDifferent(inputRadius, d / 2)
                setTextIfDifferent(inputPerimeter, PI * d)
                setTextIfDifferent(inputArea, PI * (d / 2).pow(2))
            }
            p != null && inputPerimeter.hasFocus() -> {
                setTextIfDifferent(inputRadius, p / (2 * PI))
                setTextIfDifferent(inputDiameter, p / PI)
                setTextIfDifferent(inputArea, PI * (p / (2 * PI)).pow(2))
            }
            s != null && inputArea.hasFocus() -> {
                setTextIfDifferent(inputRadius, sqrt(s / PI))
                setTextIfDifferent(inputDiameter, 2 * sqrt(s / PI))
                setTextIfDifferent(inputPerimeter, 2 * PI * sqrt(s / PI))
            }
        }
    }

    private fun calculateSquare() {
        val a = inputSquareSide.text.toString().toDoubleOrNull()
        val p = inputPerimeter.text.toString().toDoubleOrNull()
        val s = inputArea.text.toString().toDoubleOrNull()

        when {
            a != null && inputSquareSide.hasFocus() -> {
                setTextIfDifferent(inputPerimeter, 4 * a)
                setTextIfDifferent(inputArea, a.pow(2))
            }
            p != null && inputPerimeter.hasFocus() -> {
                setTextIfDifferent(inputSquareSide, p / 4)
                setTextIfDifferent(inputArea, (p / 4).pow(2))
            }
            s != null && inputArea.hasFocus() -> {
                setTextIfDifferent(inputSquareSide, sqrt(s))
                setTextIfDifferent(inputPerimeter, 4 * sqrt(s))
            }
        }
    }

    private fun calculateRectangle() {
        val a = inputRectWidth.text.toString().toDoubleOrNull()
        val b = inputRectHeight.text.toString().toDoubleOrNull()
        val p = inputPerimeter.text.toString().toDoubleOrNull()
        val s = inputArea.text.toString().toDoubleOrNull()

        when {
            a != null && b != null -> {
                setTextIfDifferent(inputPerimeter, 2 * (a + b))
                setTextIfDifferent(inputArea, a * b)
            }
            a != null && p != null && inputPerimeter.hasFocus() -> {
                setTextIfDifferent(inputRectHeight, p / 2 - a)
                setTextIfDifferent(inputArea, a * (p / 2 - a))
            }
            a != null && s != null && inputArea.hasFocus() -> {
                setTextIfDifferent(inputRectHeight, s / a)
                setTextIfDifferent(inputPerimeter, 2 * (a + s / a))
            }
            b != null && p != null && inputPerimeter.hasFocus() -> {
                setTextIfDifferent(inputRectWidth, p / 2 - b)
                setTextIfDifferent(inputArea, (p / 2 - b) * b)
            }
            b != null && s != null && inputArea.hasFocus() -> {
                setTextIfDifferent(inputRectWidth, s / b)
                setTextIfDifferent(inputPerimeter, 2 * (s / b + b))
            }
        }
    }

    private fun calculateIsoscelesTriangle() {
        val a = inputIsoSide.text.toString().toDoubleOrNull()
        val b = inputIsoBase.text.toString().toDoubleOrNull()
        val angleA = inputIsoAngleA.text.toString().toDoubleOrNull()?.let { Math.toRadians(it) }
        val angleB = inputIsoAngleB.text.toString().toDoubleOrNull()?.let { Math.toRadians(it) }
        val angleC = inputIsoAngleC.text.toString().toDoubleOrNull()?.let { Math.toRadians(it) }

        when {
            a != null && b != null && a > b/2 -> {
                val h = sqrt(a.pow(2) - (b / 2).pow(2))
                val angleAtA = 2 * asin(b / (2 * a))
                val angleAtBase = (PI - angleAtA) / 2

                setTextIfDifferent(inputPerimeter, 2 * a + b)
                setTextIfDifferent(inputArea, (b * h) / 2)
                setTextIfDifferent(inputIsoAngleA, Math.toDegrees(angleAtA))
                setTextIfDifferent(inputIsoAngleB, Math.toDegrees(angleAtBase))
                setTextIfDifferent(inputIsoAngleC, Math.toDegrees(angleAtBase))
            }
            a != null && angleA != null && angleA > 0 && angleA < PI -> {
                val base = 2 * a * sin(angleA / 2)
                val h = a * cos(angleA / 2)
                val angleAtBase = (PI - angleA) / 2

                setTextIfDifferent(inputIsoBase, base)
                setTextIfDifferent(inputPerimeter, 2 * a + base)
                setTextIfDifferent(inputArea, (base * h) / 2)
                setTextIfDifferent(inputIsoAngleB, Math.toDegrees(angleAtBase))
                setTextIfDifferent(inputIsoAngleC, Math.toDegrees(angleAtBase))
            }
            a != null && angleB != null && angleB > 0 && angleB < PI/2 -> {
                val angleAtA = PI - 2 * angleB
                val base = 2 * a * sin(angleAtA / 2)
                val h = a * cos(angleAtA / 2)

                setTextIfDifferent(inputIsoBase, base)
                setTextIfDifferent(inputPerimeter, 2 * a + base)
                setTextIfDifferent(inputArea, (base * h) / 2)
                setTextIfDifferent(inputIsoAngleA, Math.toDegrees(angleAtA))
                setTextIfDifferent(inputIsoAngleC, Math.toDegrees(angleB))
            }
            a != null && angleC != null && angleC > 0 && angleC < PI/2 -> {
                val angleAtA = PI - 2 * angleC
                val base = 2 * a * sin(angleAtA / 2)
                val h = a * cos(angleAtA / 2)

                setTextIfDifferent(inputIsoBase, base)
                setTextIfDifferent(inputPerimeter, 2 * a + base)
                setTextIfDifferent(inputArea, (base * h) / 2)
                setTextIfDifferent(inputIsoAngleA, Math.toDegrees(angleAtA))
                setTextIfDifferent(inputIsoAngleB, Math.toDegrees(angleC))
            }
            b != null && angleA != null && angleA > 0 && angleA < PI -> {
                val side = b / (2 * sin(angleA / 2))
                val h = side * cos(angleA / 2)
                val angleAtBase = (PI - angleA) / 2

                setTextIfDifferent(inputIsoSide, side)
                setTextIfDifferent(inputPerimeter, 2 * side + b)
                setTextIfDifferent(inputArea, (b * h) / 2)
                setTextIfDifferent(inputIsoAngleB, Math.toDegrees(angleAtBase))
                setTextIfDifferent(inputIsoAngleC, Math.toDegrees(angleAtBase))
            }
            b != null && angleB != null && angleB > 0 && angleB < PI/2 -> {
                val angleAtA = PI - 2 * angleB
                val side = b / (2 * sin(angleAtA / 2))
                val h = side * cos(angleAtA / 2)

                setTextIfDifferent(inputIsoSide, side)
                setTextIfDifferent(inputPerimeter, 2 * side + b)
                setTextIfDifferent(inputArea, (b * h) / 2)
                setTextIfDifferent(inputIsoAngleA, Math.toDegrees(angleAtA))
                setTextIfDifferent(inputIsoAngleC, Math.toDegrees(angleB))
            }
            b != null && angleC != null && angleC > 0 && angleC < PI/2 -> {
                val angleAtA = PI - 2 * angleC
                val side = b / (2 * sin(angleAtA / 2))
                val h = side * cos(angleAtA / 2)

                setTextIfDifferent(inputIsoSide, side)
                setTextIfDifferent(inputPerimeter, 2 * side + b)
                setTextIfDifferent(inputArea, (b * h) / 2)
                setTextIfDifferent(inputIsoAngleA, Math.toDegrees(angleAtA))
                setTextIfDifferent(inputIsoAngleB, Math.toDegrees(angleC))
            }
        }
    }

    private fun calculateRightTriangle() {
        val a = inputRightA.text.toString().toDoubleOrNull()
        val b = inputRightB.text.toString().toDoubleOrNull()
        val c = inputRightC.text.toString().toDoubleOrNull()
        val angleA = inputRightAngleA.text.toString().toDoubleOrNull()?.let { Math.toRadians(it) }
        val angleC = inputRightAngleC.text.toString().toDoubleOrNull()?.let { Math.toRadians(it) }

        when {
            a != null && b != null -> {
                val hypotenuse = sqrt(a.pow(2) + b.pow(2))
                val angA = atan(a / b)
                val angC = atan(b / a)

                setTextIfDifferent(inputRightC, hypotenuse)
                setTextIfDifferent(inputPerimeter, a + b + hypotenuse)
                setTextIfDifferent(inputArea, (a * b) / 2)
                setTextIfDifferent(inputRightAngleA, Math.toDegrees(angA))
                setTextIfDifferent(inputRightAngleC, Math.toDegrees(angC))
            }
            a != null && c != null && c > a -> {
                val cathetB = sqrt(c.pow(2) - a.pow(2))
                val angA = asin(a / c)
                val angC = acos(a / c)

                setTextIfDifferent(inputRightB, cathetB)
                setTextIfDifferent(inputPerimeter, a + cathetB + c)
                setTextIfDifferent(inputArea, (a * cathetB) / 2)
                setTextIfDifferent(inputRightAngleA, Math.toDegrees(angA))
                setTextIfDifferent(inputRightAngleC, Math.toDegrees(angC))
            }
            b != null && c != null && c > b -> {
                val cathetA = sqrt(c.pow(2) - b.pow(2))
                val angA = asin(cathetA / c)
                val angC = acos(cathetA / c)

                setTextIfDifferent(inputRightA, cathetA)
                setTextIfDifferent(inputPerimeter, cathetA + b + c)
                setTextIfDifferent(inputArea, (cathetA * b) / 2)
                setTextIfDifferent(inputRightAngleA, Math.toDegrees(angA))
                setTextIfDifferent(inputRightAngleC, Math.toDegrees(angC))
            }
            a != null && angleA != null && angleA > 0 && angleA < PI/2 -> {
                val cathetB = a / tan(angleA)
                val hypotenuse = a / sin(angleA)
                val angC = PI / 2 - angleA

                setTextIfDifferent(inputRightB, cathetB)
                setTextIfDifferent(inputRightC, hypotenuse)
                setTextIfDifferent(inputPerimeter, a + cathetB + hypotenuse)
                setTextIfDifferent(inputArea, (a * cathetB) / 2)
                setTextIfDifferent(inputRightAngleC, Math.toDegrees(angC))
            }
            a != null && angleC != null && angleC > 0 && angleC < PI/2 -> {
                val cathetB = a * tan(angleC)
                val hypotenuse = a / cos(angleC)
                val angA = PI / 2 - angleC

                setTextIfDifferent(inputRightB, cathetB)
                setTextIfDifferent(inputRightC, hypotenuse)
                setTextIfDifferent(inputPerimeter, a + cathetB + hypotenuse)
                setTextIfDifferent(inputArea, (a * cathetB) / 2)
                setTextIfDifferent(inputRightAngleA, Math.toDegrees(angA))
            }
            b != null && angleA != null && angleA > 0 && angleA < PI/2 -> {
                val cathetA = b * tan(angleA)
                val hypotenuse = b / cos(angleA)
                val angC = PI / 2 - angleA

                setTextIfDifferent(inputRightA, cathetA)
                setTextIfDifferent(inputRightC, hypotenuse)
                setTextIfDifferent(inputPerimeter, cathetA + b + hypotenuse)
                setTextIfDifferent(inputArea, (cathetA * b) / 2)
                setTextIfDifferent(inputRightAngleC, Math.toDegrees(angC))
            }
            b != null && angleC != null && angleC > 0 && angleC < PI/2 -> {
                val cathetA = b / tan(angleC)
                val hypotenuse = b / sin(angleC)
                val angA = PI / 2 - angleC

                setTextIfDifferent(inputRightA, cathetA)
                setTextIfDifferent(inputRightC, hypotenuse)
                setTextIfDifferent(inputPerimeter, cathetA + b + hypotenuse)
                setTextIfDifferent(inputArea, (cathetA * b) / 2)
                setTextIfDifferent(inputRightAngleA, Math.toDegrees(angA))
            }
        }
    }

    private fun setTextIfDifferent(editText: EditText, value: Double) {
        if (value.isNaN() || value.isInfinite()) return

        val formattedValue = String.format("%.2f", value)
        val currentText = editText.text.toString()

        if (currentText.isEmpty() || !editText.hasFocus()) {
            val currentValue = currentText.toDoubleOrNull()
            if (currentValue == null || abs(currentValue - value) > 0.01) {
                editText.setText(formattedValue)
            }
        }
    }

    private fun hideAllFields() {
        inputContainerLeft.visibility = View.GONE
        inputContainerRight.visibility = View.GONE
        resultsContainer.visibility = View.GONE
        btnReset.visibility = View.GONE

        listOf(
            fieldRadius, fieldDiameter, fieldSquareSide, fieldRectWidth, fieldRectHeight,
            fieldIsoSide, fieldIsoBase, fieldIsoAngleA, fieldIsoAngleB, fieldIsoAngleC,
            fieldRightA, fieldRightB, fieldRightC, fieldRightAngleA, fieldRightAngleB, fieldRightAngleC
        ).forEach { it.visibility = View.GONE }
    }

    private fun showCircleFields() {
        hideAllFields()
        inputContainerLeft.visibility = View.VISIBLE
        resultsContainer.visibility = View.VISIBLE
        btnReset.visibility = View.VISIBLE
        fieldRadius.visibility = View.VISIBLE
        fieldDiameter.visibility = View.VISIBLE
    }

    private fun showSquareFields() {
        hideAllFields()
        inputContainerLeft.visibility = View.VISIBLE
        resultsContainer.visibility = View.VISIBLE
        btnReset.visibility = View.VISIBLE
        fieldSquareSide.visibility = View.VISIBLE
    }

    private fun showRectangleFields() {
        hideAllFields()
        inputContainerLeft.visibility = View.VISIBLE
        resultsContainer.visibility = View.VISIBLE
        btnReset.visibility = View.VISIBLE
        fieldRectWidth.visibility = View.VISIBLE
        fieldRectHeight.visibility = View.VISIBLE
    }

    private fun showIsoscelesFields() {
        hideAllFields()
        inputContainerLeft.visibility = View.VISIBLE
        inputContainerRight.visibility = View.VISIBLE
        resultsContainer.visibility = View.VISIBLE
        btnReset.visibility = View.VISIBLE

        fieldIsoSide.visibility = View.VISIBLE
        fieldIsoBase.visibility = View.VISIBLE
        fieldIsoAngleA.visibility = View.VISIBLE
        fieldIsoAngleB.visibility = View.VISIBLE
        fieldIsoAngleC.visibility = View.VISIBLE
    }

    private fun showRightTriangleFields() {
        hideAllFields()
        inputContainerLeft.visibility = View.VISIBLE
        inputContainerRight.visibility = View.VISIBLE
        resultsContainer.visibility = View.VISIBLE
        btnReset.visibility = View.VISIBLE

        fieldRightA.visibility = View.VISIBLE
        fieldRightB.visibility = View.VISIBLE
        fieldRightC.visibility = View.VISIBLE
        fieldRightAngleA.visibility = View.VISIBLE
        fieldRightAngleB.visibility = View.VISIBLE
        fieldRightAngleC.visibility = View.VISIBLE
    }
}