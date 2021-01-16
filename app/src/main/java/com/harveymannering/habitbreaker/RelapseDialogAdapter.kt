package com.harveymannering.habitbreaker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.list_item_relapse.view.*


class RelapseDialogAdapter(context: Context, var habit_id: Int, var list: MutableList<Relapse>) : ArrayAdapter<Relapse>(context, habit_id, list) {

    private lateinit var c: Context

    init {
        c = context
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = c.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val row_view: View = inflater.inflate(R.layout.list_item_relapse, parent, false)
        row_view.relapse_list_text.text = getTime(list[position])
        row_view.btn_delete_relapse.setOnClickListener(View.OnClickListener { v ->
            //Delete relapse in db

            //Readjust any other relapses time
            val database = Database(c)
            var db = database.writableDatabase
            database.deleteRelapse(db, list[position].id)
            val h = database.getHabit(db, habit_id)

            //Update the beginning of the first relapse
            if (h.replases.size > 0 && h.replases[0].beginning != h.start){
                database.editRelapse(db, h.replases[0].id, h.start, h.replases[0].end)
            }

            //Changed the beginning times of any relapse that dont match with the next relapse
            for (r in 0 until h.replases.size - 1){
                if (h.replases[r+1].beginning != h.replases[r].end){
                    database.editRelapse(db, h.replases[r+1].id, h.replases[r].end, h.replases[r+1].end)
                    h.replases[r+1].beginning = h.replases[r].end
                }
            }

            //Remove item from UI
            list.removeAt(position)
            notifyDataSetChanged()
        })
        return row_view
    }


    fun getTime(r : Relapse) : String {
        val mins = (if (r.end.minutes < 10) "0" else "") + r.end.minutes.toString()
        val hours = (if (r.end.hours < 10) "0" else "") + r.end.hours.toString()
        return hours + ":" + mins
    }
}