package com.harveymannering.habitbreaker

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import kotlinx.android.synthetic.main.content_edit_habit.*
import kotlinx.android.synthetic.main.dialog_grid.view.*
import java.util.*


class EditHabit : AppCompatActivity() {

    //Static objects
    companion object {
        var selected_icon : Int = 0
        var selected_colour : Int = 8
        //List of icon drawables
        val icons = listOf<Int>(
            R.drawable.habit_ic, R.drawable.habit1, R.drawable.habit2, R.drawable.habit3,
            R.drawable.habit4, R.drawable.habit5, R.drawable.habit6, R.drawable.habit7,
            R.drawable.habit8, R.drawable.habit9, R.drawable.habit10, R.drawable.habit11,
            R.drawable.habit12, R.drawable.habit14, R.drawable.habit15, R.drawable.habit13,
            R.drawable.habit19, R.drawable.habit21, R.drawable.habit24, R.drawable.habit22,
            R.drawable.habit23, R.drawable.habit20, R.drawable.habit26,  R.drawable.habit28,
            R.drawable.habit25, R.drawable.habit27, R.drawable.habit29,  R.drawable.habit30,
            R.drawable.habit31, R.drawable.habit32
        )

        //List of selectable colours
        val colours = listOf<Int>(
            R.color.color3, R.color.color4, R.color.color5, R.color.color6,
            R.color.color7, R.color.color8, R.color.color11, R.color.color13,
            R.color.color14, R.color.color15, R.color.color16,R.color.color18,
            R.color.color20,R.color.color23, R.color.color24, R.color.color27,
            R.color.color32,R.color.color33, R.color.color34, R.color.color35
        )
    }

    //Variables used if pre-existing habit is being edited
    var new_habit : Boolean = true
    var habit_id : Int  = 0
    var habit_title : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //get values passed to activity
        new_habit = intent.getBooleanExtra("NEW_HABIT", true)
        habit_id = intent.getIntExtra("HABIT_ID", 0)

        //If we're editing a habit, populate the form with values
        if (new_habit == false){
            //Access the habit from the database
            val database = Database(this)
            val db = database.readableDatabase
            val habit_info = database.getHabit(db, habit_id)

            //Set values on UI
            selected_icon = habit_info.icon
            selected_colour = habit_info.colour
            habit_title = habit_info.title
            setNewTheme()

        }

        //Set main layout
        setContentView(R.layout.activity_edit_habit)

        //Set the title (if necessary)
        if (new_habit == false)
            txtHabitName.setText(habit_title)

        //Set up the button
        setButtonColour()
        btnNext.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        val unwrapped = AppCompatResources.getDrawable(baseContext, R.drawable.button_back)
                        val wrapped = DrawableCompat.wrap(unwrapped!!)
                        DrawableCompat.setTint(wrapped, fetchPrimaryColor())
                        btnNext.background = wrapped
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

        //Setup icon and color imageviews
        IconAnimation(false)
        ColorChange(selected_colour)

        //set dimensions
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        scroll_layout.layoutParams.height = displayMetrics.heightPixels - dpToPx(80) - getStatusBarHeight()
    }

    fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }

    fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }

    fun pxToDp(px: Int): Int {
        return (px / Resources.getSystem().getDisplayMetrics().density).toInt()
    }

    override fun onResume() {
        super.onResume()

        //Unfocus text box
        txtHabitName.clearFocus()
    }

    fun setButtonColour(){
        val unwrapped = AppCompatResources.getDrawable(baseContext, R.drawable.button_back)
        val wrapped = DrawableCompat.wrap(unwrapped!!)
        DrawableCompat.setTint(wrapped, fetchAccentColor())
        btnNext.background = wrapped
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

    fun IconClick(v : View){
        //Dialog creation
        var dialog = LoadIconDialog()

        //Unfocus text box
        txtHabitName.clearFocus()
    }

    fun LoadIconDialog() : AlertDialog {
        //Dialog object
        var dialog_builder = AlertDialog.Builder(this)
        dialog_builder.setNegativeButton("CANCEL", null)
        var dialog = dialog_builder.create()
        val scroll_view = layoutInflater.inflate(R.layout.dialog_grid, null)

        //Set up the grid view
        var grid = scroll_view.dialog_grid
        grid.adapter = IconAdapter(this, icons)
        grid.setOnItemClickListener(OnItemClickListener { parent, view, position, id ->
            IconAnimation(true, position)
            dialog.dismiss()
        })

        //Set dimensions
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val size =
            if (displayMetrics.widthPixels > displayMetrics.heightPixels)
                displayMetrics.heightPixels / 10
            else
                displayMetrics.widthPixels / 10
        (grid.adapter as IconAdapter).size = size

        //Display dialog
        dialog.setTitle("Pick an Icon")
        dialog.setView(scroll_view)

        //Display dialog
        dialog.show()
        dialog.window!!.setLayout(4 * displayMetrics.widthPixels / 5, ViewGroup.LayoutParams.WRAP_CONTENT)

        return dialog
    }

    fun ColourClick(v : View){
        //Dialog creation
        var dialog = LoadColourDialog()

        //Unfocus text box
        txtHabitName.clearFocus()
    }

    fun LoadColourDialog() : AlertDialog {
        //Dialog object
        var dialog_builder = AlertDialog.Builder(this)
        dialog_builder.setNegativeButton("CANCEL", null)
        var dialog = dialog_builder.create()
        val scroll_view = layoutInflater.inflate(R.layout.dialog_grid, null)

        //Set up the grid view
        var grid = scroll_view.dialog_grid
        grid.adapter = ColorAdapter(this, colours)
        grid.setOnItemClickListener(OnItemClickListener { parent, view, position, id ->
            ColorChange(position)
            dialog.dismiss()
        })

        //Set dimensions
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val size =
            if (displayMetrics.widthPixels > displayMetrics.heightPixels)
                displayMetrics.heightPixels / 10
            else
                displayMetrics.widthPixels / 10
        (grid.adapter as ColorAdapter).size = size

        //Display dialog
        dialog.setTitle("Pick a Colour")
        dialog.setView(scroll_view)

        //Set window dimensions
        dialog.show()
        dialog.window!!.setLayout(4 * displayMetrics.widthPixels / 5, ViewGroup.LayoutParams.WRAP_CONTENT)

        return dialog
    }

    fun IconAnimation(change_icon: Boolean, new_icon: Int = 0){
        //Change image sources for image views

        if (change_icon == true) {
            //Change current icon to old icon
            old_icon.setImageResource(icons[selected_icon])

            //Set the image source
            val unwrappedDrawable = AppCompatResources.getDrawable(this, icons[new_icon])
            val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable!!)
            DrawableCompat.setTint(wrappedDrawable, getResources().getColor(R.color.black))
            icon.setImageDrawable(wrappedDrawable)

            //Save the index of the new icon
            selected_icon = new_icon
        }
        else{
            //Set the image source
            val unwrappedDrawable = AppCompatResources.getDrawable(this, icons[selected_icon])
            val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable!!)
            DrawableCompat.setTint(wrappedDrawable, getResources().getColor(R.color.black))
            icon.setImageDrawable(wrappedDrawable)
        }


        //Animation objects for growth
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0f, 1f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f, 1f)
        val scaler = ObjectAnimator.ofPropertyValuesHolder(icon, scaleX, scaleY)
        val fader = ObjectAnimator.ofFloat(icon, View.ALPHA,0f, 1f)

        //Animation set plays two animations together
        val set = AnimatorSet()

        if (change_icon == true){
            //Animation for shrinking the old icon
            val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X,1f, 0f)
            val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y,1f, 0f)
            val scaler_shrink = ObjectAnimator.ofPropertyValuesHolder(old_icon, scaleX, scaleY)
            val fader_shrink = ObjectAnimator.ofFloat(old_icon, View.ALPHA, 1f, 0f)
            set.playTogether(scaler, fader, scaler_shrink, fader_shrink)
        }
        else {
            set.playTogether(scaler, fader)
        }

        //Sets time animation takes and plays animations
        set.duration = (300).toLong()
        set.start()
    }

    fun getBitmapFromVectorDrawable(context: Context, drawable: Drawable): Bitmap {
        var d: Drawable = drawable
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            d = DrawableCompat.wrap(drawable).mutate()
        }
        val bitmap: Bitmap = Bitmap.createBitmap(
            drawable.getIntrinsicWidth(),
            drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
        drawable.draw(canvas)
        return bitmap
    }

    private fun ColorChange(new_colour : Int) {
        //Change tint for the colour image view
        val unwrappedDrawable = AppCompatResources.getDrawable(this, R.drawable.colour_icon)
        val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable!!)
        DrawableCompat.setTint(wrappedDrawable, getResources().getColor(colours[new_colour]))
        colour_picker.setImageDrawable(wrappedDrawable)
        selected_colour = new_colour
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

    fun NextClick(view: View){
        //Access database
        val database = Database(this)
        val db_helper = database.writableDatabase

        if (new_habit == true) {
            //Save habit to database
            database.addHabit(
                db_helper,
                txtHabitName.text.toString(),
                Calendar.getInstance().getTime(),
                selected_icon,
                selected_colour
            )

            //Close activity
            finish()
        }
        else {
            //Edit pre-existing habit
            database.editHabit(
                db_helper,
                habit_id,
                txtHabitName.text.toString(),
                selected_icon,
                selected_colour
            )

            //Open next activity
            val new_intent = Intent(this, HabitActivity::class.java)
            new_intent.putExtra("HABIT_ID", habit_id)
            new_intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            ContextCompat.startActivity(this, new_intent, null)
        }


    }
}
