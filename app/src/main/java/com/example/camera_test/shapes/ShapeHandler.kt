// shapes/ShapeHandler.kt
package com.example.camera_test.shapes

import android.widget.EditText

interface ShapeHandler {
    fun showFields()
    fun calculate()
    fun setTextIfDifferent(editText: EditText, value: Double)
}