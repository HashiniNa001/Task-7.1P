package com.example.lostandfoundapp.model

data class Item(
    val id: Int,
    val type: String,
    val name: String,
    val phone: String,
    val description: String,
    val date: String,
    val location: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)
