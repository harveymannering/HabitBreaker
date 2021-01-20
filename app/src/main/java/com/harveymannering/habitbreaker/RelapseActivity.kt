package com.harveymannering.habitbreaker

import android.content.Intent
import android.content.res.Resources
import android.content.res.TypedArray
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import kotlinx.android.synthetic.main.activity_habit.*
import kotlinx.android.synthetic.main.activity_relapse.*
import kotlinx.android.synthetic.main.content_edit_habit.*
import java.util.*

class RelapseActivity : AppCompatActivity() {

    var selected_colour : Int = 0
    var habit_id : Int = 0
    var relapse: Relapse? = null
    var date : Date = Calendar.getInstance().time

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Get data passed to this activity
        selected_colour = intent.extras!!.getInt("COLOUR")
        habit_id = intent.extras!!.getInt("HABIT_ID")
        val relapse_id = intent.extras!!.getInt("RELAPSE_ID")

        //Set the colours of this activity
        setNewTheme()

        setContentView(R.layout.activity_relapse)

        //Set time picker
        simpleTimePicker.setIs24HourView(true)
        if (relapse_id == 0) {
            date = intent.extras!!.get("DATE") as Date
        }
        else {
            //Get a relapse to the database
            val database = Database(this)
            var db = database.readableDatabase
            relapse = database.getRelapse(db, relapse_id)
            date = relapse!!.end
            simpleTimePicker.currentHour = date.hours
            simpleTimePicker.currentMinute = date.minutes
            btnRelapseAdd.text = "Okay"
        }

        //Set add button colour
        setButtonColour(btnRelapseAdd)
        btnRelapseAdd.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        val unwrapped = AppCompatResources.getDrawable(baseContext, R.drawable.button_back)
                        val wrapped = DrawableCompat.wrap(unwrapped!!)
                        DrawableCompat.setTint(wrapped, fetchPrimaryColor())
                        btnRelapseAdd.background = wrapped
                    }
                    MotionEvent.ACTION_UP -> {
                        setButtonColour(btnRelapseAdd)
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        setButtonColour(btnRelapseAdd)
                    }
                }

                return v?.onTouchEvent(event) ?: true
            }
        })

        //Set cancel button colour
        setButtonColour(btnRelapseCancel)
        btnRelapseCancel.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        val unwrapped = AppCompatResources.getDrawable(baseContext, R.drawable.button_back)
                        val wrapped = DrawableCompat.wrap(unwrapped!!)
                        DrawableCompat.setTint(wrapped, fetchPrimaryColor())
                        btnRelapseCancel.background = wrapped
                    }
                    MotionEvent.ACTION_UP -> {
                        setButtonColour(btnRelapseCancel)
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        setButtonColour(btnRelapseCancel)
                    }
                }

                return v?.onTouchEvent(event) ?: true
            }
        })

        //set dimensions
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        relapse_layout.layoutParams.height = displayMetrics.heightPixels - dpToPx(80) - getStatusBarHeight()
    }

    fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }

    fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }

    fun setButtonColour(btn: Button){
        val unwrapped = AppCompatResources.getDrawable(baseContext, R.drawable.button_back)
        val wrapped = DrawableCompat.wrap(unwrapped!!)
        DrawableCompat.setTint(wrapped, fetchAccentColor())
        btn.background = wrapped
    }

    fun fetchAccentColor(): Int {
        val typedValue = TypedValue()
        val a: TypedArray = obtainStyledAttributes(typedValue.data, intArrayOf(android.R.attr.colorAccent))
        val color = a.getColor(0, 0)
        a.recycle()
        return color
    }

    fun fetchPrimaryColor(): Int {
        val typedValue = TypedValue()
        val a: TypedArray = obtainStyledAttributes(typedValue.data, intArrayOf(android.R.attr.colorPrimaryDark))
        val color = a.getColor(0, 0)
        a.recycle()
        return color
    }

    fun setNewTheme() {
        when (selected_colour){
            0 -> setTheme(R.style.style3)
            1 -> setTheme(R.style.style4)
            2 -> setTheme(R.style.style5)
            3 -> setTheme(R.style.style6)
            4 -> setTheme(R.style.style7)
            5 -> setTheme(R.style.style8)
            6 -> setTheme(R.style.style11)
            7 -> setTheme(R.style.style13)
            8 -> setTheme(R.style.style14)
            9 -> setTheme(R.style.style15)
            10 -> setTheme(R.style.style16)
            11 -> setTheme(R.style.style18)
            12 -> setTheme(R.style.style20)
            13 -> setTheme(R.style.style23)
            14 -> setTheme(R.style.style24)
            15 -> setTheme(R.style.style27)
            16 -> setTheme(R.style.style32)
            17 -> setTheme(R.style.style33)
            18 -> setTheme(R.style.style34)
            19 -> setTheme(R.style.style35)
            else -> setTheme(R.style.style14)
        }
    }

    fun AddClick(view: View) {
        //Get date
        date.hours = simpleTimePicker.currentHour
        date.minutes = simpleTimePicker.currentMinute

        //Get access to database
        val database = Database(this)
        var db = database.writableDatabase

        if (relapse == null) {
            //Add a relapse to the database
            val h = database.getHabit(db, habit_id)
            database.addRelapse(db, habit_id, h.start,  date, "")
            ReadjustRelapses(database, db)
        }
        else {
            //Edit the end date of a relapse
            database.editRelapse(db, relapse!!.id, date)
            ReadjustRelapses(database, db)
        }
        //Open next activity
        val new_intent = Intent(this, HabitActivity::class.java)
        new_intent.putExtra("HABIT_ID", habit_id)
        new_intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        ContextCompat.startActivity(this, new_intent, null)
    }

    fun ReadjustRelapses(database: Database, db: SQLiteDatabase){
        val h = database.getHabit(db, habit_id)

        //If new time is before the start time, correct for this
        if (h.start > date){
            database.editHabit(db, h.id, date)
            h.start = date
        }

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
    }

    fun CancelClick(view: View) {
        finish()
    }


}