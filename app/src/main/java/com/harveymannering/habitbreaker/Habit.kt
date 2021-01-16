package com.harveymannering.habitbreaker

import java.util.*

data class Habit (
    var id : Int,
    var title : String = "",
    var start : Date = Calendar.getInstance().time,
    var icon : Int = 0,
    var colour: Int = 0,
    var replases : List<Relapse> = mutableListOf()
)