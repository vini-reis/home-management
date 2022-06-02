package com.reis.vinicius.homemanagement.model.entity

import java.util.*

data class UserData(
    val userId: String,
    val name: String,
    val email: String,
    val photoUrl: String,
    val phoneNumber: String,
    val providerId: String,
    val lastAccessIn: Date,
)
