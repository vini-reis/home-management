package com.reis.vinicius.homemanagement.model.repository

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.reis.vinicius.homemanagement.model.firestore.UserDataFirestore
import com.reis.vinicius.homemanagement.model.entity.UserData
import kotlinx.coroutines.tasks.await

class UserDataRepository(application: Application): FirebaseRepository<UserData>(application) {
    private val db: FirebaseFirestore = Firebase.firestore
    private val collection = db.collection(Repository.Collection.USERS_DATA)

    suspend fun upsert(userData: UserData) {
        collection.document(userData.userId).get(getSource()).await()
            .reference.set(UserDataFirestore.fromEntity(userData))
    }
}