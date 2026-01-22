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
import android.widget.ScrollView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.*

enum class ShapeType {
    NONE, CIRCLE, SQUARE, RECTANGLE, ISOSCELES_TRIANGLE, RIGHT_TRIANGLE
}

class ShapeCalculatorActivity : AppCompatActivity() {

    private lateinit var shapeCanvas: ShapeCanvasView
    private lateinit var shapeSpinner: Spinner
    private lateinit var scrollContainer: ScrollView
    private lateinit var inputContainerLeft: LinearLayout
    private lateinit var inputContainerRight: LinearLayout
    private lateinit var resultsContainer: LinearLayout
    private lateinit var btnReset: ImageButton

    private var currentShapeType: ShapeType = ShapeType.NONE
    private var isCalculating = false

    // Поля ввода параметров - Круг
    private lateinit var inputRadius: EditText
    private lateinit var inputDiameter: EditText

    // Поля ввода параметров - Квадрат
    private lateinit var inputSquareSide: EditText
    private lateinit var inputSquareDiagonal: EditText

    // Поля ввода параметров - Прямоугольник
    private lateinit var inputRectWidth: EditText
    private lateinit var inputRectHeight: EditText
    private lateinit var inputRectDiagonal: EditText

    // Поля ввода параметров - Равнобедренный треугольник
    private lateinit var inputIsoSide: EditText
    private lateinit var inputIsoBase: EditText
    private lateinit var inputIsoBisector: EditText
    private lateinit var inputIsoAngleA: EditText
    private lateinit var inputIsoAngleB: EditText
    private lateinit var inputIsoAngleC: EditText

    // Поля ввода параметров - Прямоугольный треугольник
    private lateinit var inputRightA: EditText
    private lateinit var inputRightB: EditText
    private lateinit var inputRightC: EditText
    private lateinit var inputRightBisector: EditText
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
    private lateinit var fieldSquareDiagonal: LinearLayout
    private lateinit var fieldRectWidth: LinearLayout
    private lateinit var fieldRectHeight: LinearLayout
    private lateinit var fieldRectDiagonal: LinearLayout
    private lateinit var fieldIsoSide: LinearLayout
    private lateinit var fieldIsoBase: LinearLayout
    private lateinit var fieldIsoBisector: LinearLayout
    private lateinit var fieldIsoAngleA: LinearLayout
    private lateinit var fieldIsoAngleB: LinearLayout
    private lateinit var fieldIsoAngleC: LinearLayout
    private lateinit var fieldRightA: LinearLayout
    private lateinit var fieldRightB: LinearLayout
    private lateinit var fieldRightC: LinearLayout
    private lateinit var fieldRightBisector: LinearLayout
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
        scrollContainer = findViewById(R.id.scroll_container)
        inputContainerLeft = findViewById(R.id.input_container_left)
        inputContainerRight = findViewById(R.id.input_container_right)
        resultsContainer = findViewById(R.id.results_container)
        btnReset = findViewById(R.id.btn_reset)

        inputRadius = findViewById(R.id.input_radius)
        inputDiameter = findViewById(R.id.input_diameter)
        inputSquareSide = findViewById(R.id.input_square_side)
        inputSquareDiagonal = findViewById(R.id.input_square_diagonal)
        inputRectWidth = findViewById(R.id.input_rect_width)
        inputRectHeight = findViewById(R.id.input_rect_height)
        inputRectDiagonal = findViewById(R.id.input_rect_diagonal)
        inputIsoSide = findViewById(R.id.input_iso_side)
        inputIsoBase = findViewById(R.id.input_iso_base)
        inputIsoBisector = findViewById(R.id.input_iso_bisector)
        inputIsoAngleA = findViewById(R.id.input_iso_angle_a)
        inputIsoAngleB = findViewById(R.id.input_iso_angle_b)
        inputIsoAngleC = findViewById(R.id.input_iso_angle_c)
        inputRightA = findViewById(R.id.input_right_a)
        inputRightB = findViewById(R.id.input_right_b)
        inputRightC = findViewById(R.id.input_right_c)
        inputRightBisector = findViewById(R.id.input_right_bisector)
        inputRightAngleA = findViewById(R.id.input_right_angle_a)
        inputRightAngleB = findViewById(R.id.input_right_angle_b)
        inputRightAngleC = findViewById(R.id.input_right_angle_c)

        inputPerimeter = findViewById(R.id.input_perimeter)
        inputArea = findViewById(R.id.input_area)

        fieldRadius = findViewById(R.id.field_radius)
        fieldDiameter = findViewById(R.id.field_diameter)
        fieldSquareSide = findViewById(R.id.field_square_side)
        fieldSquareDiagonal = findViewById(R.id.field_square_diagonal)
        fieldRectWidth = findViewById(R.id.field_rect_width)
        fieldRectHeight = findViewById(R.id.field_rect_height)
        fieldRectDiagonal = findViewById(R.id.field_rect_diagonal)
        fieldIsoSide = findViewById(R.id.field_iso_side)
        fieldIsoBase = findViewById(R.id.field_iso_base)
        fieldIsoBisector = findViewById(R.id.field_iso_bisector)
        fieldIsoAngleA = findViewById(R.id.field_iso_angle_a)
        fieldIsoAngleB = findViewById(R.id.field_iso_angle_b)
        fieldIsoAngleC = findViewById(R.id.field_iso_angle_c)
        fieldRightA = findViewById(R.id.field_right_a)
        fieldRightB = findViewById(R.id.field_right_b)
        fieldRightC = findViewById(R.id.field_right_c)
        fieldRightBisector = findViewById(R.id.field_right_bisector)
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

            override fun onNothingSelected(parent: AdapterView<*>?) {
                hideAllFields()
            }
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
            inputRadius, inputDiameter, inputSquareSide, inputSquareDiagonal,
            inputRectWidth, inputRectHeight, inputRectDiagonal,
            inputIsoSide, inputIsoBase, inputIsoBisector, inputIsoAngleA, inputIsoAngleB, inputIsoAngleC,
            inputRightA, inputRightB, inputRightC, inputRightBisector, inputRightAngleA, inputRightAngleC,
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

        listOf(
            inputRadius, inputDiameter, inputSquareSide, inputSquareDiagonal,
            inputRectWidth, inputRectHeight, inputRectDiagonal,
            inputIsoSide, inputIsoBase, inputIsoBisector, inputIsoAngleA, inputIsoAngleB, inputIsoAngleC,
            inputRightA, inputRightB, inputRightC, inputRightBisector, inputRightAngleA, inputRightAngleC,
            inputPerimeter, inputArea
        ).forEach { it.setText("") }

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

    private fun calculateSquare() {
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

    private fun calculateRectangle() {
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

    private fun calculateIsoscelesTriangle() {
        val a = inputIsoSide.text.toString().toDoubleOrNull()
        val b = inputIsoBase.text.toString().toDoubleOrNull()
        val l = inputIsoBisector.text.toString().toDoubleOrNull()
        val angleA = inputIsoAngleA.text.toString().toDoubleOrNull()?.let { Math.toRadians(it) }
        val angleB = inputIsoAngleB.text.toString().toDoubleOrNull()?.let { Math.toRadians(it) }
        val angleC = inputIsoAngleC.text.toString().toDoubleOrNull()?.let { Math.toRadians(it) }

        when {
            // Известны обе стороны a и b
            a != null && a > 0 && b != null && b > 0 && a > b/2 -> {
                val h = sqrt(a.pow(2) - (b / 2).pow(2))
                val angleAtA = 2 * asin(b / (2 * a))
                val angleAtBase = (PI - angleAtA) / 2

                setTextIfDifferent(inputIsoBisector, h)
                setTextIfDifferent(inputPerimeter, 2 * a + b)
                setTextIfDifferent(inputArea, (b * h) / 2)
                setTextIfDifferent(inputIsoAngleA, Math.toDegrees(angleAtA))
                setTextIfDifferent(inputIsoAngleB, Math.toDegrees(angleAtBase))
                setTextIfDifferent(inputIsoAngleC, Math.toDegrees(angleAtBase))
            }
            // Известна боковая сторона a и бисектриса l
            a != null && a > 0 && l != null && l > 0 && a > l && inputIsoBisector.hasFocus() -> {
                val base = 2 * sqrt(a.pow(2) - l.pow(2))
                val angleAtA = 2 * asin(base / (2 * a))
                val angleAtBase = (PI - angleAtA) / 2

                setTextIfDifferent(inputIsoBase, base)
                setTextIfDifferent(inputPerimeter, 2 * a + base)
                setTextIfDifferent(inputArea, (base * l) / 2)
                setTextIfDifferent(inputIsoAngleA, Math.toDegrees(angleAtA))
                setTextIfDifferent(inputIsoAngleB, Math.toDegrees(angleAtBase))
                setTextIfDifferent(inputIsoAngleC, Math.toDegrees(angleAtBase))
            }
            // Известно основание b и бисектриса l
            b != null && b > 0 && l != null && l > 0 && inputIsoBisector.hasFocus() -> {
                val side = sqrt(l.pow(2) + (b / 2).pow(2))
                val angleAtA = 2 * asin(b / (2 * side))
                val angleAtBase = (PI - angleAtA) / 2

                setTextIfDifferent(inputIsoSide, side)
                setTextIfDifferent(inputPerimeter, 2 * side + b)
                setTextIfDifferent(inputArea, (b * l) / 2)
                setTextIfDifferent(inputIsoAngleA, Math.toDegrees(angleAtA))
                setTextIfDifferent(inputIsoAngleB, Math.toDegrees(angleAtBase))
                setTextIfDifferent(inputIsoAngleC, Math.toDegrees(angleAtBase))
            }
            // Известна боковая сторона a и угол при вершине A
            a != null && a > 0 && angleA != null && angleA > 0 && angleA < PI -> {
                val base = 2 * a * sin(angleA / 2)
                val h = a * cos(angleA / 2)
                val angleAtBase = (PI - angleA) / 2

                setTextIfDifferent(inputIsoBase, base)
                setTextIfDifferent(inputIsoBisector, h)
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

                setTextIfDifferent(inputIsoBase, base)
                setTextIfDifferent(inputIsoBisector, h)
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

                setTextIfDifferent(inputIsoBase, base)
                setTextIfDifferent(inputIsoBisector, h)
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

                setTextIfDifferent(inputIsoSide, side)
                setTextIfDifferent(inputIsoBisector, h)
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

                setTextIfDifferent(inputIsoSide, side)
                setTextIfDifferent(inputIsoBisector, h)
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

                setTextIfDifferent(inputIsoSide, side)
                setTextIfDifferent(inputIsoBisector, h)
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
            // Известны оба катета a и b
            a != null && a > 0 && b != null && b > 0 -> {
                val hypotenuse = sqrt(a.pow(2) + b.pow(2))
                val bisector = (a * b) / hypotenuse
                val angA = atan(a / b)
                val angC = atan(b / a)

                setTextIfDifferent(inputRightC, hypotenuse)
                setTextIfDifferent(inputRightBisector, bisector)
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

                setTextIfDifferent(inputRightB, cathetB)
                setTextIfDifferent(inputRightBisector, bisector)
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

                setTextIfDifferent(inputRightA, cathetA)
                setTextIfDifferent(inputRightBisector, bisector)
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

                setTextIfDifferent(inputRightB, cathetB)
                setTextIfDifferent(inputRightC, hypotenuse)
                setTextIfDifferent(inputRightBisector, bisector)
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

                setTextIfDifferent(inputRightB, cathetB)
                setTextIfDifferent(inputRightC, hypotenuse)
                setTextIfDifferent(inputRightBisector, bisector)
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

                setTextIfDifferent(inputRightA, cathetA)
                setTextIfDifferent(inputRightC, hypotenuse)
                setTextIfDifferent(inputRightBisector, bisector)
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

                setTextIfDifferent(inputRightA, cathetA)
                setTextIfDifferent(inputRightC, hypotenuse)
                setTextIfDifferent(inputRightBisector, bisector)
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

                setTextIfDifferent(inputRightA, cathetA)
                setTextIfDifferent(inputRightB, cathetB)
                setTextIfDifferent(inputRightBisector, bisector)
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

                setTextIfDifferent(inputRightA, cathetA)
                setTextIfDifferent(inputRightB, cathetB)
                setTextIfDifferent(inputRightBisector, bisector)
                setTextIfDifferent(inputPerimeter, cathetA + cathetB + c)
                setTextIfDifferent(inputArea, (cathetA * cathetB) / 2)
                setTextIfDifferent(inputRightAngleA, Math.toDegrees(angA))
            }
        }
    }

    private fun setTextIfDifferent(editText: EditText, value: Double) {
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

    private fun hideAllFields() {
        scrollContainer.visibility = View.GONE
        btnReset.visibility = View.GONE
        inputContainerLeft.visibility = View.GONE
        inputContainerRight.visibility = View.GONE
        resultsContainer.visibility = View.GONE

        listOf(
            fieldRadius, fieldDiameter, fieldSquareSide, fieldSquareDiagonal,
            fieldRectWidth, fieldRectHeight, fieldRectDiagonal,
            fieldIsoSide, fieldIsoBase, fieldIsoBisector, fieldIsoAngleA, fieldIsoAngleB, fieldIsoAngleC,
            fieldRightA, fieldRightB, fieldRightC, fieldRightBisector,
            fieldRightAngleA, fieldRightAngleB, fieldRightAngleC
        ).forEach { it.visibility = View.GONE }
    }

    private fun showCircleFields() {
        hideAllFields()
        scrollContainer.visibility = View.VISIBLE
        inputContainerLeft.visibility = View.VISIBLE
        resultsContainer.visibility = View.VISIBLE
        btnReset.visibility = View.VISIBLE
        fieldRadius.visibility = View.VISIBLE
        fieldDiameter.visibility = View.VISIBLE
    }

    private fun showSquareFields() {
        hideAllFields()
        scrollContainer.visibility = View.VISIBLE
        inputContainerLeft.visibility = View.VISIBLE
        resultsContainer.visibility = View.VISIBLE
        btnReset.visibility = View.VISIBLE
        fieldSquareSide.visibility = View.VISIBLE
        fieldSquareDiagonal.visibility = View.VISIBLE
    }

    private fun showRectangleFields() {
        hideAllFields()
        scrollContainer.visibility = View.VISIBLE
        inputContainerLeft.visibility = View.VISIBLE
        resultsContainer.visibility = View.VISIBLE
        btnReset.visibility = View.VISIBLE
        fieldRectWidth.visibility = View.VISIBLE
        fieldRectHeight.visibility = View.VISIBLE
        fieldRectDiagonal.visibility = View.VISIBLE
    }

    private fun showIsoscelesFields() {
        hideAllFields()
        scrollContainer.visibility = View.VISIBLE
        inputContainerLeft.visibility = View.VISIBLE
        inputContainerRight.visibility = View.VISIBLE
        resultsContainer.visibility = View.VISIBLE
        btnReset.visibility = View.VISIBLE

        fieldIsoSide.visibility = View.VISIBLE
        fieldIsoBase.visibility = View.VISIBLE
        fieldIsoBisector.visibility = View.VISIBLE
        fieldIsoAngleA.visibility = View.VISIBLE
        fieldIsoAngleB.visibility = View.VISIBLE
        fieldIsoAngleC.visibility = View.VISIBLE
    }

    private fun showRightTriangleFields() {
        hideAllFields()
        scrollContainer.visibility = View.VISIBLE
        inputContainerLeft.visibility = View.VISIBLE
        inputContainerRight.visibility = View.VISIBLE
        resultsContainer.visibility = View.VISIBLE
        btnReset.visibility = View.VISIBLE

        fieldRightA.visibility = View.VISIBLE
        fieldRightB.visibility = View.VISIBLE
        fieldRightC.visibility = View.VISIBLE
        fieldRightBisector.visibility = View.VISIBLE
        fieldRightAngleA.visibility = View.VISIBLE
        fieldRightAngleB.visibility = View.VISIBLE
        fieldRightAngleC.visibility = View.VISIBLE
    }
}