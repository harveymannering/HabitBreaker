package com.harveymannering.habitbreaker

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.LineBackgroundSpan

class TextSpan : LineBackgroundSpan {
    private val text: String
    private val color: Int

    constructor() {
        text = "Start"
        color = 0
    }

    constructor(color: Int) {
        text = "Start"
        this.color = color
    }

    constructor(text: String) {
        this.text = text
        color = 0
    }

    constructor(text: String, color: Int) {
        this.text = text
        this.color = color
    }

    override fun drawBackground(
        canvas: Canvas, paint: Paint,
        left: Int, right: Int , top: Int, baseline: Int, bottom: Int,
        charSequence: CharSequence,
        start: Int, end: Int, lineNum: Int
    ) {
        val width = paint.measureText(text)
        val height = paint.textSize
        canvas.drawText(text,((left.toFloat() + right.toFloat() - width) / 2), bottom.toFloat() + (3 * height / 4), paint)
    }
}