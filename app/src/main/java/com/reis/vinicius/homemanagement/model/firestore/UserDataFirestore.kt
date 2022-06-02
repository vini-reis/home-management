package com.reis.vinicius.homemanagement.model.firestore

import com.reis.vinicius.homemanagement.model.entity.UserData
import java.util.*

data class UserDataFirestore (
    val name: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val phoneNumber: String? = null,
    val providerId: String? = null,
    val lastAccessIn: Date = Date(),
){
    companion object {
        fun fromEntity(entity: UserData) = UserDataFirestore(
            name = entity.name,
            email = entity.email,
            photoUrl = entity.photoUrl,
            phoneNumber = entity.phoneNumber,
            providerId = entity.providerId,
            lastAccessIn = entity.lastAccessIn,
        )
    }

    object Fields {
        const val name = "name"
        const val email = "email"
        const val photoUrl = "photoUrl"
        const val phoneNumber = "phoneNumber"
        const val providerId = "providerId"
        const val lastAccessIn = "lastAccessIn"
    }
}