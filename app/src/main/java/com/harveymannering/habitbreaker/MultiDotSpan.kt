package com.harveymannering.habitbreaker

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.LineBackgroundSpan

class MultiDotSpan : LineBackgroundSpan {

    val DEFAULT_RADIUS = 3f
    var radius = 0f
    var color = 0

    constructor() {
        this.radius = DEFAULT_RADIUS
        this.color = 0
    }

    constructor(color: Int) {
        this.radius = DEFAULT_RADIUS
        this.color = color
    }

    constructor(radius: Float) {
        this.radius = radius
        this.color = 0
    }

    constructor(radius: Float, color: Int) {
        this.radius = radius
        this.color = color
    }

    override fun drawBackground(
        canvas: Canvas, paint: Paint,
        left: Int, right: Int , top: Int, baseline: Int, bottom: Int,
        charSequence: CharSequence,
        start: Int, end: Int, lineNum: Int
    ) {
        val oldColor = paint.color
        if (color != 0 && paint.color != -1) {
            paint.color = color
        }

        canvas.drawCircle((left + right) / 2.toFloat(), bottom + radius, radius, paint)
        paint.color = oldColor
    }
}