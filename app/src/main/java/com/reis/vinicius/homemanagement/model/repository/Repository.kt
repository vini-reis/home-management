package com.reis.vinicius.homemanagement.model.repository

interface Repository<T> {
    object Collection {
        const val USERS_DATA = "usersData"
    }

    enum class Type {
        UserData,
    }
}