package com.harveymannering.habitbreaker

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.SystemClock
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.appbar.AppBarLayout
import com.prolificinteractive.materialcalendarview.*
import kotlinx.android.synthetic.main.activity_habit.*
import kotlinx.android.synthetic.main.dialog_relapses.view.*
import kotlinx.android.synthetic.main.popup_habit_options.view.*
import java.lang.Long.MAX_VALUE
import java.text.SimpleDateFormat
import java.util.*


class HabitActivity : AppCompatActivity(), OnDateSelectedListener {

    var habit_id: Int = 0
    var habit_info: Habit  = Habit(0)

    //Functions for converting Date to CalendarDate and via versa
    val date_converter = {d: Date -> CalendarDay.from(d.year + 1900, d.month + 1, d.date) }
    val date_converter_2 = {cd: CalendarDay -> Date(cd.year - 1900, cd.month - 1, cd.day) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Which habit is this?
        habit_id = intent.extras!!.getInt("HABIT_ID")

        //Access the habit from the database
        val database = Database(this)
        val db = database.readableDatabase
        habit_info = database.getHabit(db, habit_id)

        //Set the theme
        setNewTheme()
        setContentView(R.layout.activity_habit)

        //Populate the screen with data
        RefreshScreen()

        //Set up animation
        coordinateMotion()

        //Set up back button
        btnBack.setOnClickListener(View.OnClickListener {
            finish()
        })

        //Set up the more options button
        btnMore.setOnClickListener(View.OnClickListener {
            // inflate the layout of the popup window
            var popupView =
                (getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                .inflate(R.layout.popup_habit_options, null)

            // create the popup window
            val displayMetrics: DisplayMetrics = getResources().getDisplayMetrics()
            val width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200f, displayMetrics).toInt()
            val height = LinearLayout.LayoutParams.WRAP_CONTENT
            val focusable = true // lets taps outside the popup also dismiss it
            val popupWindow = PopupWindow(popupView, width, height, focusable)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
            {
                popupWindow.elevation = 10f
                popupWindow.setBackgroundDrawable(ColorDrawable(Color.WHITE));
            }
            popupWindow.setAnimationStyle(R.style.popup_window_animation);

            // show the popup window
            val location: Rect? = locateView(btnMore)
            popupWindow.showAtLocation(
                it,
                Gravity.TOP or Gravity.LEFT,
                location!!.right - width - 5,
                location!!.bottom + 10
            )

            //option strings
            val list_items = arrayOf("Edit", "Delete")

            //Get reference to list
            val options_list_view = popupView.habit_options_list

            //Set up the list
            options_list_view.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                list_items
            )
            options_list_view.setOnItemClickListener(OnItemClickListener { parent, view, position, id -> //Set what field the list will be ordered by
                //Edit option click
                if (position == 0){
                    val intent = Intent(this, EditHabit::class.java)
                    intent.putExtra("NEW_HABIT", false)
                    intent.putExtra("HABIT_ID", habit_id)
                    startActivity(intent)
                }
                //Delete option click
                else if (position == 1){
                    database.deleteHabit(db, habit_id)
                    finish()
                }
            })

            // dismiss the popup window when touched
            popupView.setOnTouchListener { v, event ->
                popupWindow.dismiss()
                true
            }
        })
    }

    override fun onResume() {
        super.onResume()
        RefreshScreen()
    }

    fun coordinateMotion() {
        val appBarLayout: AppBarLayout = findViewById(R.id.appbar_layout)
        val motionLayout: MotionLayout = findViewById(R.id.motion_layout)
        val listener = AppBarLayout.OnOffsetChangedListener { unused, verticalOffset ->
            val seekPosition = -verticalOffset / appBarLayout.totalScrollRange.toFloat()
            motionLayout.progress = seekPosition
        }
        appBarLayout.addOnOffsetChangedListener(listener)
    }

    fun RefreshScreen(){

        //Set icon ImageView
        val unwrappedDrawable = AppCompatResources.getDrawable(this, EditHabit.icons[habit_info.icon])
        val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable!!)
        DrawableCompat.setTint(wrappedDrawable, getResources().getColor(R.color.white))
        imgHabitIcon.setImageDrawable(wrappedDrawable)

        //Set title
        txtHabitTitle.setText(habit_info.title)

        //When was the last reset?
        var end_data : Date = habit_info.start
        if (habit_info.replases.size > 0)
            end_data = habit_info.replases.last().end

        //Start timer
        txtTimer.base = SystemClock.elapsedRealtime() + (end_data.time - Calendar.getInstance().time.time)
        setTimerText(txtTimer)
        txtTimer.setOnChronometerTickListener { chrononmeter ->
            setTimerText(txtTimer)
        }
        txtTimer.start()

        //Set the header colour
        appbar_layout.setBackgroundColor(this.getResources().getColor(EditHabit.colours[habit_info.colour]))

        //Setup button
        setButtonColour()
        btnReset.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        val unwrapped = AppCompatResources.getDrawable(baseContext, R.drawable.button_back)
                        val wrapped = DrawableCompat.wrap(unwrapped!!)
                        DrawableCompat.setTint(wrapped, fetchPrimaryColor())
                        btnReset.background = wrapped
                    }
                    MotionEvent.ACTION_UP -> {
                        setButtonColour()
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        setButtonColour()
                    }
                }

                return v?.onTouchEvent(event) ?: true
            }
        })

        //Set the statistics tables value
        RefreshStats()

        //Set up the calendar
        RefreshCalendar()
    }

    fun setButtonColour(){
        val unwrapped = AppCompatResources.getDrawable(baseContext, R.drawable.button_back)
        val wrapped = DrawableCompat.wrap(unwrapped!!)
        DrawableCompat.setTint(wrapped, fetchAccentColor())
        btnReset.background = wrapped
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

    fun setTimerText(chrononmeter: Chronometer){
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

    fun RefreshStats(){
        //Number of attempts
        txtAttempts.text = (habit_info.replases.size + 1).toString()

        //Set the start date
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) //HH:mm:ss
        txtStartDate.text = dateFormat.format(habit_info.start)

        //Set the previous period (if there is one)
        var previous : Long = 0
        var no_relapses : Boolean = true
        if (habit_info.replases.size > 0) {
            previous = habit_info.replases.last().end.time - habit_info.replases.last().beginning.time
            no_relapses = false
        }
        txtPrevious.text = getReadableTime(previous, no_relapses)

        //Find the min, max and average period
        var average: Long = 0
        var max: Long = 0
        var min: Long = if (habit_info.replases.size > 0) MAX_VALUE else 0
        for (r in habit_info.replases){
            val period = r.end.time - r.beginning.time
            average += period
            if (period < min)
                min = period
            if (period > max)
                max = period
        }

        //Perform the actual averaging
        if (habit_info.replases.size > 0)
            average /= habit_info.replases.size

        //Set labels text
        txtShortest.text = getReadableTime(min, no_relapses)
        txtAverages.text = getReadableTime(average, no_relapses)
        txtLongest.text = getReadableTime(max, no_relapses)

        //Set label sizes
        val width = Resources.getSystem().getDisplayMetrics().widthPixels / 3
        txtAttempts.width = width
        txtStartDate.width = width
        txtPrevious.width = width
        txtShortest.width = width
        txtAverages.width = width
        txtLongest.width = width
        txtHeader1.width = width
        txtHeader2.width = width
        txtHeader3.width = width
        txtHeader4.width = width
        txtHeader5.width = width
        txtHeader6.width = width
    }

    fun RefreshCalendar(){
        //CalendarDate objects
        val dates = HashSet<CalendarDay>()
        val start_date = date_converter(habit_info.start)
        for (r in habit_info.replases) {
            if (!date_converter(r.end).equals(start_date)) {
                dates.add(date_converter(r.end))
            }
        }

        //Decorate calendar
        calendar.addDecorator(RelapseDayDecorator(this, dates))
        calendar.addDecorator(StartDayDecorator(this, start_date))

        //calendar.setHeaderTextAppearance(R.style.CustomTextAppearance)
        //calendar.setDateTextAppearance(R.style.CustomTextAppearance)
        //calendar.setWeekDayTextAppearance(R.style.CustomTextAppearance)

        //make calendar interactive
        calendar.setOnDateChangedListener(this)

    }

    fun getReadableTime(time_period: Long, no_relapses: Boolean) : String{
        if (no_relapses == true)
            return "-"
        else if (time_period <= 0)
            return "0 seconds"

        //Get values for days, hours, minutes + seconds
        val d = (time_period / 86400000).toInt()
        val h = (time_period - d * 86400000).toInt() / 3600000
        val m = (time_period - d * 86400000 - h * 3600000).toInt() / 60000
        val s = (time_period - d * 86400000 - h * 3600000 - m * 60000).toInt() / 1000

        if (d > 1)
            return d.toString() + " days"
        else if (d == 1)
            return "1 day"
        else if (h > 1)
            return h.toString() + " hours"
        else if (h == 1)
            return "1 hour"
        else if (m > 1)
            return m.toString() + " minutes"
        else if (m == 1)
            return "1 minute"
        else if (s > 1)
            return s.toString() + " seconds"
        else if (s == 1)
            return "1 second"
        else
            return "-"
    }

    fun setNewTheme() {
        when (habit_info.colour){
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

    fun ResetClick(view: View){
        //When was the last reset?
        var beginning_data : Date = habit_info.start
        if (habit_info.replases.size > 0)
            beginning_data = habit_info.replases.last().end

        //Add a relapse to the database
        val database = Database(this)
        var db = database.writableDatabase
        database.addRelapse(db, habit_id, beginning_data, Calendar.getInstance().time, "")

        //Get updated habit
        db = database.readableDatabase
        habit_info = database.getHabit(db, habit_id)
        RefreshScreen()
    }

    fun locateView(v: View?): Rect? {
        val loc_int = IntArray(2)
        if (v == null) return null
        try {
            v.getLocationOnScreen(loc_int)
        } catch (npe: NullPointerException) {
            //Happens when the view doesn't exist on screen anymore.
            return null
        }
        val location = Rect()
        location.left = loc_int[0]
        location.top = loc_int[1]
        location.right = location.left + v.width
        location.bottom = location.top + v.height
        return location
    }

    override fun onDateSelected(
        widget: MaterialCalendarView,
        date: CalendarDay,
        selected: Boolean
    ) {
        //Get day after today and day before the start of the habit
        var c = Calendar.getInstance()
        c.add(Calendar.DATE, 1)
        var end_day = date_converter(c.time)
        c.time = habit_info.start
        c.add(Calendar.DATE, -1)
        var start_day = date_converter(c.time)

        //Get all dates from resets
        val dates = HashSet<CalendarDay>()
        for (r in habit_info.replases)
            dates.add(date_converter(r.end))

        if (dates.contains(date)) {
            //Dialog object
            var dialog_builder = AlertDialog.Builder(this)
            dialog_builder.setNegativeButton("CANCEL", null)
            dialog_builder.setPositiveButton("ADD", DialogInterface.OnClickListener
            { dialogInterface: DialogInterface, i: Int ->
                val intent = Intent(this, RelapseActivity::class.java)
                intent.putExtra("COLOUR", habit_info.colour)
                intent.putExtra("HABIT_ID", habit_id)
                intent.putExtra("RELAPSE_ID", 0)
                intent.putExtra("DATE", date_converter_2(date))
                startActivity(intent)
            })

            //Refresh habit info and update screen onDismiss
            dialog_builder.setOnDismissListener(DialogInterface.OnDismissListener {
                val database = Database(this)
                val db = database.readableDatabase
                habit_info = database.getHabit(db, habit_id)
                RefreshScreen()
            })
            var dialog = dialog_builder.create()
            val main_view = layoutInflater.inflate(R.layout.dialog_relapses, null)

            //Find which relapses apply for this specific date
            val todays_relapses = mutableListOf<Relapse>()
            for (r in habit_info.replases){
                if (date.equals(date_converter(r.end))) {
                    todays_relapses.add(r)
                }
            }

            //Add items to a list
            val list_view = main_view.relapse_list
            val arrayAdapter = RelapseDialogAdapter(this, habit_id, todays_relapses)
            list_view.adapter = arrayAdapter

            //dialogs lists onClick listener
            list_view.setOnItemClickListener(OnItemClickListener { parent, view, position, id -> //Set what field the list will be ordered by
                val intent = Intent(this, RelapseActivity::class.java)
                intent.putExtra("COLOUR", habit_info.colour)
                intent.putExtra("HABIT_ID", habit_id)
                intent.putExtra("RELAPSE_ID", todays_relapses[position].id)
                startActivity(intent)
            })

            //Set dialog view options
            dialog.setTitle("Relapses")
            dialog.setView(main_view)

            //Display dialog
            dialog.show()
        }
        //New relapses can only be added between the date the habit started and before the current date
        else if (date.isBefore(end_day) && date.isAfter(start_day)) {
            val intent = Intent(this, RelapseActivity::class.java)
            intent.putExtra("COLOUR", habit_info.colour)
            intent.putExtra("HABIT_ID", habit_id)
            intent.putExtra("RELAPSE_ID", 0)
            intent.putExtra("DATE", date_converter_2(date))
            startActivity(intent)
        }
    }

    inner class RelapseDayDecorator(context: Activity, val dates: HashSet<CalendarDay>) : DayViewDecorator {

        override fun shouldDecorate(day: CalendarDay): Boolean {
            return dates.contains(day)
        } 

        override fun decorate(view: DayViewFacade) {
            //Set icon ImageView
            view.addSpan(MultiDotSpan(5f, this@HabitActivity.getResources().getColor(EditHabit.colours[habit_info.colour])))
        }
    }

    inner class StartDayDecorator(context: Activity, val date: CalendarDay) : DayViewDecorator {

        override fun shouldDecorate(day: CalendarDay): Boolean {
            return day.equals(date)
        }

        override fun decorate(view: DayViewFacade) {
            //Set icon ImageView
            view.addSpan((TextSpan("Start", this@HabitActivity.getResources().getColor(EditHabit.colours[habit_info.colour]))))
        }
    }
}
