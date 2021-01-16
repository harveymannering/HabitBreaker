package com.harveymannering.habitbreaker

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.startActivity
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.setMargins
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_habit.*
import kotlinx.android.synthetic.main.habit_list_item.view.*
import java.util.*

class HabtiListAdapter(val habits: List<Habit>) : RecyclerView.Adapter<HabtiListAdapter.ViewHolder>() {

    lateinit var context : Context


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.habit_list_item, parent, false)
        val viewHolder = ViewHolder(view)
        context = parent.getContext()
        return viewHolder
    }

    override fun getItemCount(): Int {
        return habits.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //Data object containing info about a habit
        val h: Habit = habits.get(position)

        //Set up and colour image view
        val size = MainActivity.getIconSize(context)
        val unwrappedDrawable = AppCompatResources.getDrawable(context, EditHabit.icons[h.icon])
        val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable!!)
        DrawableCompat.setTint(wrappedDrawable, context.getResources().getColor(EditHabit.colours[h.colour]))
        holder.icon.setImageDrawable(wrappedDrawable)
        //Format image
        val layout_params = TableRow.LayoutParams(size, size)
        layout_params.setMargins(10)
        holder.icon.layoutParams = layout_params

        //Set text views
        holder.title.setText(h.title)


        //When was the last reset?
        var end_data : Date = h.start
        if (h.replases.size > 0)
            end_data = h.replases.last().end
        //Start timer
        holder.timer.base = SystemClock.elapsedRealtime() + (end_data.time - Calendar.getInstance().time.time)
        holder.timer.setOnChronometerTickListener { chrononmeter ->
            //Find the time difference
            val time: Long = SystemClock.elapsedRealtime() - chrononmeter.getBase()

            //Get values for days, hours, minutes + secons
            val d = (time / 86400000).toInt()
            val h = (time - d * 86400000).toInt() / 3600000
            val m = (time - d * 86400000 - h * 3600000).toInt() / 60000
            val s = (time - d * 86400000 - h * 3600000 - m * 60000).toInt() / 1000

            //Format the time strings
            val dd = d.toString()
            val hh = h.toString()
            val mm = m.toString()
            val ss = if (s < 10) "0$s" else s.toString() + ""

            //Display the string
            chrononmeter.setText(dd + "d " + hh + "h " + mm + "m " + ss + "s ")
        }
        holder.timer.start()

        //On click listener
        holder.card.setOnClickListener(View.OnClickListener {
            //Open next activity
            val intent = Intent(context, HabitActivity::class.java)
            intent.putExtra("HABIT_ID", h.id)
            startActivity(context, intent, null)
        })
    }

    //Used to access a list item on the main screen
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var icon : ImageView
        var title : TextView
        var timer : Chronometer
        var card: CardView

        init {
            icon = itemView.habit_list_icon
            title = itemView.habit_list_name
            timer = itemView.habit_list_timer
            card = itemView.findViewById(R.id.habit_card)
        }
    }
}