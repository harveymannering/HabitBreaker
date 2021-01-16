package com.harveymannering.habitbreaker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object{
        //Get icon size from screen dimensions
        fun getIconSize(context: Context) : Int{
            val displayMetrics = DisplayMetrics()
            (context as Activity).windowManager
                .defaultDisplay
                .getMetrics(displayMetrics)
            val size =
                if (displayMetrics.widthPixels > displayMetrics.heightPixels)
                    displayMetrics.heightPixels / 10
                else
                    displayMetrics.widthPixels / 10
            return size
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Add a new habit
        fabAddHabit.setOnClickListener { view ->
            val intent = Intent(this, EditHabit::class.java)
            intent.putExtra("NEW_HABIT", true)
            intent.putExtra("HABIT_ID", 0)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        //Get a list of all habits from the database
        val database = Database(this)
        val db = database.readableDatabase
        var habit_list = database.getHabits(db)

        //Set up list of habits on UI
        val habitListAdapter = HabtiListAdapter(habit_list)
        val recyclerView = findViewById(R.id.lstHabits) as RecyclerView
        recyclerView.setLayoutManager(LinearLayoutManager(this))
        recyclerView.setItemAnimator(DefaultItemAnimator())
        recyclerView.setAdapter(habitListAdapter)
    }
}
