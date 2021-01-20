package com.harveymannering.habitbreaker

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*

const val DBName = "BreakTheHabitDB"
const val DBVersion = 1

class Database(context : Context) : SQLiteOpenHelper(context, DBName, null, DBVersion) {

    //Used very first time database is created
    override fun onCreate(db: SQLiteDatabase?) {
        updateDatabase(db, 0, DBVersion)
    }

    //Used when the database changes structure
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        updateDatabase(db, oldVersion, newVersion)
    }


    fun updateDatabase(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int){
        if (oldVersion < 1){

            //Creates the habit table
            db!!.execSQL(
                "CREATE TABLE HABIT " +
                        "(_id INTEGER PRIMARY KEY, " +
                        "HABIT_TITLE TEXT, " +
                        "START DATETIME, " +
                        "ICON INT, " +
                        "COLOUR INT)"
            )

            //Create the relapse table
            db!!.execSQL(
                "CREATE TABLE RELAPSE " +
                        "(_id INTEGER PRIMARY KEY, " +
                        "HABIT_ID INT, " +
                        "BEGINNING DATETIME, " +
                        "END DATETIME, " +
                        "NOTES TEXT)"
            )
        }
    }

    fun addHabit(db: SQLiteDatabase, title: String, start: Date, icon: Int, colour: Int) : Long{
        //Useful objects
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val habitValues = ContentValues()

        //Add parameters
        habitValues.put("HABIT_TITLE", title)
        habitValues.put("START", dateFormat.format(start))
        habitValues.put("ICON", icon)
        habitValues.put("COLOUR", colour)

        //Run the SQL (and returns the id)
        return db.insert("HABIT", null, habitValues)
    }

    fun deleteHabit(db: SQLiteDatabase, habitId: Int){
        db.delete("HABIT", "_id = " + habitId, null)
        db.delete("RELAPSE", "HABIT_ID = " + habitId, null)
    }

    fun editHabit(db: SQLiteDatabase, id: Int, title: String, icon: Int, colour: Int) {
        //Define input parameters
        val habitValues = ContentValues()
        habitValues.put("HABIT_TITLE", title)
        habitValues.put("ICON", icon)
        habitValues.put("COLOUR", colour)

        //Update database
        db.update("HABIT", habitValues, "_id = $id", null)
    }

    fun editHabit(db: SQLiteDatabase, id: Int, start: Date) {
        //Define input parameters
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val habitValues = ContentValues()
        habitValues.put("START", dateFormat.format(start))

        //Update database
        db.update("HABIT", habitValues, "_id = $id", null)
    }

    fun getHabits(db: SQLiteDatabase) : List<Habit>{
        var habits = mutableListOf<Habit>()
        //Used to read dates from the database
        val dateFormat = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.getDefault()
        )

        try {
            //Define cursors for accessing data
            val cursor = db.query(
                "HABIT", arrayOf(
                    "_id",
                    "HABIT_TITLE",
                    "START",
                    "ICON",
                    "COLOUR"
                ),
                null, null, null, null, null
            )

            //Move cursor to any subsequent records
            while (cursor.moveToNext()) {
                //Get habit data
                habits.add(
                    Habit(
                        cursor.getInt(0),
                        cursor.getString(1),
                        dateFormat.parse(cursor.getString(2)),
                        cursor.getInt(3),
                        cursor.getInt(4),
                        getRelapses(db, cursor.getInt(0))
                    )
                )
            }

            //Close off database access object
            cursor.close()
        }
        catch (ex : Exception){
            ex.printStackTrace()
        }

        return habits
    }

    fun getHabit(db: SQLiteDatabase, id: Int) : Habit{
        //Return object
        var habit = Habit(0)

        //Used to read dates from the database
        val dateFormat = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.getDefault()
        )

        try {
            //Define cursors for accessing data
            val cursor = db.query(
                "HABIT", arrayOf(
                    "_id",
                    "HABIT_TITLE",
                    "START",
                    "ICON",
                    "COLOUR"
                ),
                "_id=?", arrayOf(id.toString()), null, null, null
            )

            //Move cursor to first record
            if (cursor.moveToFirst()) {
                //Get habit data
                habit = Habit(
                        cursor.getInt(0),
                        cursor.getString(1),
                        dateFormat.parse(cursor.getString(2)),
                        cursor.getInt(3),
                        cursor.getInt(4),
                        getRelapses(db, id)
                )
            }

            //Close off database access object
            cursor.close()
        }
        catch (ex : Exception){
            ex.printStackTrace()
        }

        return habit
    }

    fun addRelapse(db: SQLiteDatabase, habit_id: Int, beginning: Date, end: Date, notes: String) : Long {
        //Useful object
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val relapseValues = ContentValues()

        //Add parameters
        relapseValues.put("HABIT_ID", habit_id)
        relapseValues.put("BEGINNING", dateFormat.format(beginning))
        relapseValues.put("END", dateFormat.format(end))
        relapseValues.put("NOTES", notes)

        //Run the SQL (and returns the id)
        return db.insert("RELAPSE", null, relapseValues)
    }

    fun addRelapse(db: SQLiteDatabase, habit_id: Int, end: Date, notes: String) : Long {
        //Find the beginning date
        val habit = getHabit(db, habit_id)
        var relapse_id : Int = 0
        var start_date : Date = habit.start
        for (r in habit.replases){
            if (r.beginning <= end && r.end >= end){
                relapse_id = r.id
                start_date = r.beginning
                break
            }
        }

        //if the end date was not in between any relapses
        if (relapse_id == 0) {
            //After the final relapse
            if (habit.replases.size > 0)
                return addRelapse(db, habit_id, habit.replases.last().end, end, notes)
            //No relapses
            else
                return addRelapse(db, habit_id, habit.start, end, notes)
        }
        else {
            editRelapse(db, relapse_id, start_date)
            return addRelapse(db, habit_id, start_date, end, notes)
        }
    }

    fun editRelapse(db: SQLiteDatabase, relapse_id: Int, end: Date) {
        //Define input parameters
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val relapseValues = ContentValues()
        relapseValues.put("END", dateFormat.format(end))

        //Update database
        db.update("RELAPSE", relapseValues, "_id = $relapse_id", null)
    }

    fun editRelapse(db: SQLiteDatabase, relapse_id: Int, beginning: Date, end: Date) {
        //Define input parameters
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val relapseValues = ContentValues()

        relapseValues.put("BEGINNING", dateFormat.format(beginning))
        relapseValues.put("END", dateFormat.format(end))

        //Update database
        db.update("RELAPSE", relapseValues, "_id = $relapse_id", null)
    }

    fun deleteRelapse(db: SQLiteDatabase, relapse_id: Int) {
        db.delete("RELAPSE", "_id = " + relapse_id, null);
    }


    fun getRelapses(db: SQLiteDatabase, habit_id: Int) : List<Relapse> {
        //Useful object
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        var relapses = mutableListOf<Relapse>()

        try {
            //Define cursors for accessing data
            val cursor = db.query(
                "RELAPSE", arrayOf(
                    "_id",
                    "BEGINNING",
                    "END",
                    "NOTES"
                ),
                "HABIT_ID=?", arrayOf(habit_id.toString()), null, null, "END ASC"
            )

            //Move cursor to any subsequent records
            while (cursor.moveToNext()) {
                //Get habit data
                relapses.add(
                    Relapse(
                        cursor.getInt(0),
                        dateFormat.parse(cursor.getString(1)),
                        dateFormat.parse(cursor.getString(2)),
                        cursor.getString(3)
                    )
                )
            }

            //Close off database access object
            cursor.close()
        }
        catch (ex : Exception){
            ex.printStackTrace()
        }

        return relapses
    }

    fun getRelapse(db: SQLiteDatabase, id: Int) : Relapse{
        //Return object
        var relapse = Relapse(id, Calendar.getInstance().time, Calendar.getInstance().time, "")

        //Used to read dates from the database
        val dateFormat = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.getDefault()
        )

        try {
            //Define cursors for accessing data
            val cursor = db.query(
                "RELAPSE", arrayOf(
                    "_id",
                    "BEGINNING",
                    "END",
                    "NOTES"
                ),
                "_id=?", arrayOf(id.toString()), null, null, null
            )

            //Move cursor to first record
            if (cursor.moveToFirst()) {
                //Get habit data
                relapse = Relapse(
                    cursor.getInt(0),
                    dateFormat.parse(cursor.getString(1)),
                    dateFormat.parse(cursor.getString(2)),
                    cursor.getString(3)
                )
            }

            //Close off database access object
            cursor.close()
        }
        catch (ex : Exception){
            ex.printStackTrace()
        }

        return relapse
    }

}