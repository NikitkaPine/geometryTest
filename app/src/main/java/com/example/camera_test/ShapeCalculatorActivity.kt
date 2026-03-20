package com.example.camera_test

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.camera_test.shapes.*

// Экран калькулятора геометрических фигур
// Пользователь выбирает фигуру, вводит параметры — всё остальное считается автоматически
class ShapeCalculatorActivity : AppCompatActivity() {

    // Холст для рисования выбранной фигуры
    private lateinit var shapeCanvas: ShapeCanvasView
    // Выпадающий список с выбором фигуры
    private lateinit var shapeSpinner: Spinner
    private lateinit var scrollContainer: ScrollView
    // Левая колонка полей ввода (основные параметры)
    private lateinit var inputContainerLeft: LinearLayout
    // Правая колонка (углы — только у треугольников)
    private lateinit var inputContainerRight: LinearLayout
    // Блок с результатами (периметр, площадь и т.д.)
    private lateinit var resultsContainer: LinearLayout
    // Кнопка сброса всех полей
    private lateinit var btnReset: ImageButton
    // Поле для вывода ошибок
    private lateinit var tvError: TextView

    // Текущая выбранная фигура
    private var currentShapeType: ShapeType = ShapeType.NONE
    // Флаг — идёт ли сейчас пересчёт (чтобы не зациклиться)
    private var isCalculating = false
    // Обработчик текущей фигуры — знает, как считать именно её
    private var currentHandler: ShapeHandler? = null
    // Все обработчики фигур, заготовленные заранее
    private lateinit var handlers: Map<ShapeType, ShapeHandler>

    // --- Поля ввода для каждой фигуры ---

    // Круг
    private lateinit var inputRadius: EditText
    private lateinit var inputDiameter: EditText

    // Квадрат
    private lateinit var inputSquareSide: EditText
    private lateinit var inputSquareDiagonal: EditText

    // Прямоугольник
    private lateinit var inputRectWidth: EditText
    private lateinit var inputRectHeight: EditText
    private lateinit var inputRectDiagonal: EditText

    // Равнобедренный треугольник
    private lateinit var inputIsoSide: EditText
    private lateinit var inputIsoBase: EditText
    private lateinit var inputIsoAngleA: EditText
    private lateinit var inputIsoAngleB: EditText
    private lateinit var inputIsoAngleC: EditText

    // Прямоугольный треугольник
    private lateinit var inputRightA: EditText
    private lateinit var inputRightB: EditText
    private lateinit var inputRightC: EditText
    private lateinit var inputRightAngleA: EditText
    private lateinit var inputRightAngleB: EditText
    private lateinit var inputRightAngleC: EditText

    // --- Поля результатов ---
    private lateinit var inputPerimeter: EditText
    private lateinit var inputArea: EditText
    private lateinit var resultIsoBisectorValue: EditText   // Биссектриса равнобедренного
    private lateinit var resultRightBisectorValue: EditText // Биссектриса прямоугольного

    // --- Контейнеры-обёртки для каждого поля (нужны для show/hide) ---
    private lateinit var fieldRadius: LinearLayout
    private lateinit var fieldDiameter: LinearLayout
    private lateinit var fieldSquareSide: LinearLayout
    private lateinit var fieldSquareDiagonal: LinearLayout
    private lateinit var fieldRectWidth: LinearLayout
    private lateinit var fieldRectHeight: LinearLayout
    private lateinit var fieldRectDiagonal: LinearLayout
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
    private lateinit var resultIsoBisector: LinearLayout
    private lateinit var resultRightBisector: LinearLayout

    // Вызывается при создании экрана
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shape_calculator)

        initViews()            // Привязываем все View по id
        initHandlers()         // Создаём обработчики для каждой фигуры
        setupSpinner()         // Настраиваем выпадающий список фигур
        setupAutoCalculation() // Вешаем слушатели на поля — пересчёт на лету
        setupResetButton()     // Настраиваем кнопку сброса
    }

    /**
     * Находим все элементы интерфейса по их id из XML-разметки.
     */
    private fun initViews() {
        shapeCanvas         = findViewById(R.id.shape_canvas)
        shapeSpinner        = findViewById(R.id.shape_spinner)
        scrollContainer     = findViewById(R.id.scroll_container)
        inputContainerLeft  = findViewById(R.id.input_container_left)
        inputContainerRight = findViewById(R.id.input_container_right)
        resultsContainer    = findViewById(R.id.results_container)
        btnReset            = findViewById(R.id.btn_reset)
        tvError             = findViewById(R.id.tv_error)

        // Поля ввода
        inputRadius         = findViewById(R.id.input_radius)
        inputDiameter       = findViewById(R.id.input_diameter)
        inputSquareSide     = findViewById(R.id.input_square_side)
        inputSquareDiagonal = findViewById(R.id.input_square_diagonal)
        inputRectWidth      = findViewById(R.id.input_rect_width)
        inputRectHeight     = findViewById(R.id.input_rect_height)
        inputRectDiagonal   = findViewById(R.id.input_rect_diagonal)
        inputIsoSide        = findViewById(R.id.input_iso_side)
        inputIsoBase        = findViewById(R.id.input_iso_base)
        inputIsoAngleA      = findViewById(R.id.input_iso_angle_a)
        inputIsoAngleB      = findViewById(R.id.input_iso_angle_b)
        inputIsoAngleC      = findViewById(R.id.input_iso_angle_c)
        inputRightA         = findViewById(R.id.input_right_a)
        inputRightB         = findViewById(R.id.input_right_b)
        inputRightC         = findViewById(R.id.input_right_c)
        inputRightAngleA    = findViewById(R.id.input_right_angle_a)
        inputRightAngleB    = findViewById(R.id.input_right_angle_b)
        inputRightAngleC    = findViewById(R.id.input_right_angle_c)

        // Поля результатов
        inputPerimeter           = findViewById(R.id.input_perimeter)
        inputArea                = findViewById(R.id.input_area)
        resultIsoBisectorValue   = findViewById(R.id.result_iso_bisector_value)
        resultRightBisectorValue = findViewById(R.id.result_right_bisector_value)

        // Контейнеры-обёртки
        fieldRadius      = findViewById(R.id.field_radius)
        fieldDiameter    = findViewById(R.id.field_diameter)
        fieldSquareSide  = findViewById(R.id.field_square_side)
        fieldSquareDiagonal = findViewById(R.id.field_square_diagonal)
        fieldRectWidth   = findViewById(R.id.field_rect_width)
        fieldRectHeight  = findViewById(R.id.field_rect_height)
        fieldRectDiagonal = findViewById(R.id.field_rect_diagonal)
        fieldIsoSide     = findViewById(R.id.field_iso_side)
        fieldIsoBase     = findViewById(R.id.field_iso_base)
        fieldIsoAngleA   = findViewById(R.id.field_iso_angle_a)
        fieldIsoAngleB   = findViewById(R.id.field_iso_angle_b)
        fieldIsoAngleC   = findViewById(R.id.field_iso_angle_c)
        fieldRightA      = findViewById(R.id.field_right_a)
        fieldRightB      = findViewById(R.id.field_right_b)
        fieldRightC      = findViewById(R.id.field_right_c)
        fieldRightAngleA = findViewById(R.id.field_right_angle_a)
        fieldRightAngleB = findViewById(R.id.field_right_angle_b)
        fieldRightAngleC = findViewById(R.id.field_right_angle_c)
        resultIsoBisector  = findViewById(R.id.result_iso_bisector)
        resultRightBisector = findViewById(R.id.result_right_bisector)
    }

    /**
     * Создаём по одному обработчику для каждой фигуры.
     * Каждый обработчик знает свои поля и умеет считать параметры своей фигуры.
     */
    private fun initHandlers() {
        handlers = mapOf(
            ShapeType.CIRCLE to CircleHandler(
                fieldRadius, fieldDiameter, inputRadius, inputDiameter,
                inputPerimeter, inputArea, tvError
            ),
            ShapeType.SQUARE to SquareHandler(
                fieldSquareSide, fieldSquareDiagonal, inputSquareSide, inputSquareDiagonal,
                inputPerimeter, inputArea, tvError
            ),
            ShapeType.RECTANGLE to RectangleHandler(
                fieldRectWidth, fieldRectHeight, fieldRectDiagonal,
                inputRectWidth, inputRectHeight, inputRectDiagonal,
                inputPerimeter, inputArea, tvError
            ),
            ShapeType.ISOSCELES_TRIANGLE to IsoscelesTriangleHandler(
                fieldIsoSide, fieldIsoBase, fieldIsoAngleA, fieldIsoAngleB, fieldIsoAngleC,
                inputIsoSide, inputIsoBase, inputIsoAngleA, inputIsoAngleB, inputIsoAngleC,
                inputPerimeter, inputArea, resultIsoBisectorValue, tvError
            ),
            ShapeType.RIGHT_TRIANGLE to RightTriangleHandler(
                fieldRightA, fieldRightB, fieldRightC,
                fieldRightAngleA, fieldRightAngleB, fieldRightAngleC,
                inputRightA, inputRightB, inputRightC,
                inputRightAngleA, inputRightAngleB, inputRightAngleC,
                inputPerimeter, inputArea, resultRightBisectorValue, tvError
            )
        )
    }

    /**
     * Настраиваем спиннер (выпадающий список) с названиями фигур.
     * При выборе фигуры скрываем старые поля и показываем нужные для новой.
     */
    private fun setupSpinner() {
        val shapes = arrayOf(
            "Выберите фигуру", "Circle", "Square", "Rectangle",
            "Isosceles triangle", "Right-angled triangle"
        )

        // Кастомный адаптер — красим текст в чёрный, чтобы он был виден на любом фоне
        val adapter = object : ArrayAdapter<String>(
            this, android.R.layout.simple_spinner_item, shapes
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(Color.BLACK)
                view.textSize = 16f
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.setTextColor(Color.BLACK)
                view.setBackgroundColor(Color.WHITE)
                view.setPadding(32, 24, 32, 24)
                return view
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        shapeSpinner.adapter = adapter

        shapeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            // Пользователь выбрал фигуру из списка

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val newType = when (position) {
                    1 -> ShapeType.CIRCLE
                    2 -> ShapeType.SQUARE
                    3 -> ShapeType.RECTANGLE
                    4 -> ShapeType.ISOSCELES_TRIANGLE
                    5 -> ShapeType.RIGHT_TRIANGLE
                    else -> ShapeType.NONE
                }

                // Перерисовываем UI только если фигура действительно изменилась
                if (newType != currentShapeType) {
                    hideAllFields()            // Сначала прячем всё
                    currentShapeType = newType
                    currentHandler = handlers[newType]
                    currentHandler?.showFields() // Показываем только поля для этой фигуры

                    // Показываем/скрываем колонки ввода в зависимости от фигуры
                    inputContainerLeft.visibility =
                        if (newType != ShapeType.NONE) View.VISIBLE else View.GONE

                    // Правая колонка (углы) нужна только треугольникам
                    inputContainerRight.visibility =
                        if (newType == ShapeType.ISOSCELES_TRIANGLE || newType == ShapeType.RIGHT_TRIANGLE)
                            View.VISIBLE else View.GONE

                    resultsContainer.visibility =
                        if (newType != ShapeType.NONE) View.VISIBLE else View.GONE

                    // Строка с биссектрисой — только для нужного треугольника
                    resultIsoBisector.visibility =
                        if (newType == ShapeType.ISOSCELES_TRIANGLE) View.VISIBLE else View.GONE

                    resultRightBisector.visibility =
                        if (newType == ShapeType.RIGHT_TRIANGLE) View.VISIBLE else View.GONE

                    btnReset.visibility =
                        if (newType != ShapeType.NONE) View.VISIBLE else View.GONE

                    shapeCanvas.setShape(currentShapeType) // Обновляем рисунок фигуры
                    tvError.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                hideAllFields()
            }
        }
    }

    /**
     * Вешаем TextWatcher на все поля ввода.
     * Как только пользователь что-то вводит — сразу запускается пересчёт.
     * Флаг isCalculating защищает от бесконечного цикла:
     * пересчёт меняет поля → TextWatcher снова сработал бы → снова пересчёт...
     */
    private fun setupAutoCalculation() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!isCalculating) {
                    isCalculating = true
                    currentHandler?.calculate() // Запускаем расчёт текущей фигуры
                    isCalculating = false
                }
            }
        }

        // Подписываем watcher на все поля
        listOf(
            inputRadius, inputDiameter, inputSquareSide, inputSquareDiagonal,
            inputRectWidth, inputRectHeight, inputRectDiagonal,
            inputIsoSide, inputIsoBase, inputIsoAngleA, inputIsoAngleB, inputIsoAngleC,
            inputRightA, inputRightB, inputRightC, inputRightAngleA, inputRightAngleC,
            inputPerimeter, inputArea, resultIsoBisectorValue, resultRightBisectorValue
        ).forEach { it.addTextChangedListener(textWatcher) }
    }

    /**
     * Кнопка сброса — привязываем к ней очистку всех полей.
     */
    private fun setupResetButton() {
        btnReset.setOnClickListener {
            resetAllFields()
        }
    }

    /**
     * Очищаем все поля ввода и результатов.
     * Для прямоугольного треугольника угол B = 90° — восстанавливаем его сразу.
     */
    private fun resetAllFields() {
        listOf(
            inputRadius, inputDiameter, inputSquareSide, inputSquareDiagonal,
            inputRectWidth, inputRectHeight, inputRectDiagonal,
            inputIsoSide, inputIsoBase, inputIsoAngleA, inputIsoAngleB, inputIsoAngleC,
            inputRightA, inputRightB, inputRightC, inputRightAngleA, inputRightAngleC,
            inputPerimeter, inputArea, resultIsoBisectorValue, resultRightBisectorValue
        ).forEach { it.setText("") }

        // Угол 90° у прямоугольного треугольника — фиксированный, возвращаем его
        if (currentShapeType == ShapeType.RIGHT_TRIANGLE) {
            inputRightAngleB.setText("90")
        }

        tvError.visibility = View.GONE
        Toast.makeText(this, "All values reset", Toast.LENGTH_SHORT).show()
    }

    /**
     * Прячем вообще все поля и контейнеры.
     * Вызывается перед показом полей новой фигуры, чтобы не мешались старые.
     */
    private fun hideAllFields() {
        inputContainerLeft.visibility  = View.GONE
        inputContainerRight.visibility = View.GONE
        resultsContainer.visibility    = View.GONE
        resultIsoBisector.visibility   = View.GONE
        resultRightBisector.visibility = View.GONE
        btnReset.visibility            = View.GONE
        tvError.visibility             = View.GONE

        listOf(
            fieldRadius, fieldDiameter, fieldSquareSide, fieldSquareDiagonal,
            fieldRectWidth, fieldRectHeight, fieldRectDiagonal,
            fieldIsoSide, fieldIsoBase, fieldIsoAngleA, fieldIsoAngleB, fieldIsoAngleC,
            fieldRightA, fieldRightB, fieldRightC,
            fieldRightAngleA, fieldRightAngleB, fieldRightAngleC
        ).forEach { it.visibility = View.GONE }
    }
}