package com.harveymannering.habitbreaker

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableRow
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.setMargins


class ColorAdapter(val context: Context, val colors: List<Int>): BaseAdapter() {

    public var size : Int = 0

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var view = convertView
        if (view == null) {

            //Create icon view
            val image_view = ImageView(context)
            var layout_params1 = LinearLayout.LayoutParams(size, size + size / 2)
            image_view.layoutParams = layout_params1

            //Change colour
            val unwrappedDrawable = AppCompatResources.getDrawable(context, R.drawable.colour_icon)
            val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable!!)
            DrawableCompat.setTint(wrappedDrawable, context.getResources().getColor(colors[position]))
            image_view.setImageDrawable(wrappedDrawable)

            //Create a parent LinearLayout object
            view = LinearLayout(context)
            var layout_params2 = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            layout_params2.gravity = Gravity.CENTER
            view.layoutParams = layout_params2
            view.addView(image_view)
        }

        return view
    }

    override fun getItem(position: Int): Any {
        return colors[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return colors.size
    }

}