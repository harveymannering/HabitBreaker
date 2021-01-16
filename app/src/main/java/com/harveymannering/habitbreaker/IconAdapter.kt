package com.harveymannering.habitbreaker

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TableRow
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import kotlinx.android.synthetic.main.activity_habit.*


class IconAdapter(val context: Context, val icons: List<Int>): BaseAdapter() {

    public var size : Int = 0

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var view = convertView
        if (view == null) {
            //Create icon view
            view = ImageView(context)
            var layout_params = TableRow.LayoutParams(size, size)
            view.layoutParams = layout_params
            layout_params.topMargin = size / 4

            //Change icons colour
            val unwrappedDrawable = AppCompatResources.getDrawable(context, icons[position])
            val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable!!)
            DrawableCompat.setTint(wrappedDrawable, context.getResources().getColor(R.color.black))
            view.setImageDrawable(wrappedDrawable)
        }

        return view
    }

    override fun getItem(position: Int): Any {
        return icons[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return icons.size
    }

}