package com.example.payten_template.Data

import java.text.SimpleDateFormat

@kotlinx.serialization.Serializable
data class Rezervacija(
    var id: String? = null,
    var name: String,
    var reservation: String,
    var placeno: Boolean,
    var worker: String,
    var telegram: Boolean? = null,
    var services: String? = null
){
    companion object{
        val reservationDateFormat = SimpleDateFormat("dd.MM.yyyy/HH:mm")
    }
}