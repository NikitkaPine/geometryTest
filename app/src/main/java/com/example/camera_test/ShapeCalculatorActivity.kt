package com.example.camera_test

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.pow
import kotlin.math.sqrt

// Enum для типов фигур
enum class ShapeType {
    NONE,
    CIRCLE,
    SQUARE,
    RECTANGLE,
    ISOSCELES_TRIANGLE,
    RIGHT_TRIANGLE
}

class ShapeCalculatorActivity : AppCompatActivity() {

    private lateinit var shapeCanvas: ShapeCanvasView
    private lateinit var shapeSpinner: Spinner
    private lateinit var inputContainerLeft: LinearLayout
    private lateinit var inputContainerRight: LinearLayout
    private lateinit var buttonSolve: Button
    private lateinit var resultsContainer: LinearLayout

    private lateinit var resultPerimeter: TextView
    private lateinit var resultArea: TextView
    private lateinit var resultSides: TextView

    private var currentShapeType: ShapeType = ShapeType.NONE

    // Поля ввода
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
            setupSolveButton()
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(
                this,
                "Ошибка: ${e.message}",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun initViews() {
        shapeCanvas = findViewById(R.id.shape_canvas)
        shapeSpinner = findViewById(R.id.shape_spinner)
        inputContainerLeft = findViewById(R.id.input_container_left)
        inputContainerRight = findViewById(R.id.input_container_right)
        buttonSolve = findViewById(R.id.button_solve)
        resultsContainer = findViewById(R.id.results_container)

        resultPerimeter = findViewById(R.id.result_perimeter)
        resultArea = findViewById(R.id.result_area)
        resultSides = findViewById(R.id.result_sides)

        // Поля ввода
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

        // Контейнеры
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

        // Связываем радиус и диаметр
        setupRadiusDiameterSync()
    }

    private fun setupRadiusDiameterSync() {
        inputRadius.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (inputRadius.hasFocus() && s.toString().isNotEmpty()) {
                    try {
                        val r = s.toString().toDouble()
                        inputDiameter.setText(String.format("%.2f", r * 2))
                    } catch (e: Exception) {}
                }
            }
        })

        inputDiameter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (inputDiameter.hasFocus() && s.toString().isNotEmpty()) {
                    try {
                        val d = s.toString().toDouble()
                        inputRadius.setText(String.format("%.2f", d / 2))
                    } catch (e: Exception) {}
                }
            }
        })
    }

    private fun setupSpinner() {
        val shapes = arrayOf(
            "Выберите фигуру",
            "Круг",
            "Квадрат",
            "Прямоугольник",
            "Треугольник равнобедренный",
            "Треугольник прямоугольный"
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

    private fun setupSolveButton() {
        buttonSolve.setOnClickListener {
            calculateResults()
        }
    }

    private fun calculateResults() {
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
            android.widget.Toast.makeText(this, "Ошибка расчёта", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculateCircle() {
        val r = inputRadius.text.toString().toDoubleOrNull() ?: return

        val perimeter = 2 * Math.PI * r
        val area = Math.PI * r.pow(2)

        showResults(
            perimeter = String.format("P = %.2f", perimeter),
            area = String.format("S = %.2f", area),
            sides = ""
        )
    }

    private fun calculateSquare() {
        val a = inputSquareSide.text.toString().toDoubleOrNull() ?: return

        val perimeter = 4 * a
        val area = a.pow(2)

        showResults(
            perimeter = String.format("P = %.2f", perimeter),
            area = String.format("S = %.2f", area),
            sides = ""
        )
    }

    private fun calculateRectangle() {
        val a = inputRectWidth.text.toString().toDoubleOrNull()
        val b = inputRectHeight.text.toString().toDoubleOrNull()

        if (a != null && b != null) {
            val perimeter = 2 * (a + b)
            val area = a * b

            showResults(
                perimeter = String.format("P = %.2f", perimeter),
                area = String.format("S = %.2f", area),
                sides = ""
            )
        }
    }

    private fun calculateIsoscelesTriangle() {
        val a = inputIsoSide.text.toString().toDoubleOrNull()
        val b = inputIsoBase.text.toString().toDoubleOrNull()

        if (a != null && b != null) {
            val perimeter = 2 * a + b
            val h = sqrt(a.pow(2) - (b / 2).pow(2))
            val area = (b * h) / 2

            showResults(
                perimeter = String.format("P = %.2f", perimeter),
                area = String.format("S = %.2f", area),
                sides = String.format("h = %.2f", h)
            )
        } else if (a != null) {
            // Если есть только боковая сторона, ждём основание
            android.widget.Toast.makeText(this, "Введите основание b", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculateRightTriangle() {
        val a = inputRightA.text.toString().toDoubleOrNull()
        val b = inputRightB.text.toString().toDoubleOrNull()
        var c = inputRightC.text.toString().toDoubleOrNull()

        // Вычисляем гипотенузу если есть оба катета
        if (a != null && b != null && c == null) {
            c = sqrt(a.pow(2) + b.pow(2))
            inputRightC.setText(String.format("%.2f", c))
        }

        if (a != null && b != null && c != null) {
            val perimeter = a + b + c
            val area = (a * b) / 2

            showResults(
                perimeter = String.format("P = %.2f", perimeter),
                area = String.format("S = %.2f", area),
                sides = String.format("c = %.2f", c)
            )
        }
    }

    private fun showResults(perimeter: String, area: String, sides: String) {
        resultsContainer.visibility = View.VISIBLE
        resultPerimeter.visibility = View.VISIBLE
        resultArea.visibility = View.VISIBLE

        resultPerimeter.text = perimeter
        resultArea.text = area

        if (sides.isNotEmpty()) {
            resultSides.visibility = View.VISIBLE
            resultSides.text = sides
        } else {
            resultSides.visibility = View.GONE
        }
    }

    private fun hideAllFields() {
        inputContainerLeft.visibility = View.GONE
        inputContainerRight.visibility = View.GONE
        buttonSolve.visibility = View.GONE
        resultsContainer.visibility = View.GONE

        fieldRadius.visibility = View.GONE
        fieldDiameter.visibility = View.GONE
        fieldSquareSide.visibility = View.GONE
        fieldRectWidth.visibility = View.GONE
        fieldRectHeight.visibility = View.GONE
        fieldIsoSide.visibility = View.GONE
        fieldIsoBase.visibility = View.GONE
        fieldIsoAngleA.visibility = View.GONE
        fieldIsoAngleB.visibility = View.GONE
        fieldIsoAngleC.visibility = View.GONE
        fieldRightA.visibility = View.GONE
        fieldRightB.visibility = View.GONE
        fieldRightC.visibility = View.GONE
        fieldRightAngleA.visibility = View.GONE
        fieldRightAngleB.visibility = View.GONE
        fieldRightAngleC.visibility = View.GONE
    }

    private fun showCircleFields() {
        hideAllFields()
        inputContainerLeft.visibility = View.VISIBLE
        buttonSolve.visibility = View.VISIBLE
        fieldRadius.visibility = View.VISIBLE
        fieldDiameter.visibility = View.VISIBLE
    }

    private fun showSquareFields() {
        hideAllFields()
        inputContainerLeft.visibility = View.VISIBLE
        buttonSolve.visibility = View.VISIBLE
        fieldSquareSide.visibility = View.VISIBLE
    }

    private fun showRectangleFields() {
        hideAllFields()
        inputContainerLeft.visibility = View.VISIBLE
        buttonSolve.visibility = View.VISIBLE
        fieldRectWidth.visibility = View.VISIBLE
        fieldRectHeight.visibility = View.VISIBLE
    }

    private fun showIsoscelesFields() {
        hideAllFields()
        inputContainerLeft.visibility = View.VISIBLE
        inputContainerRight.visibility = View.VISIBLE
        buttonSolve.visibility = View.VISIBLE

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
        buttonSolve.visibility = View.VISIBLE

        fieldRightA.visibility = View.VISIBLE
        fieldRightB.visibility = View.VISIBLE
        fieldRightC.visibility = View.VISIBLE
        fieldRightAngleA.visibility = View.VISIBLE
        fieldRightAngleB.visibility = View.VISIBLE
        fieldRightAngleC.visibility = View.VISIBLE
    }
}