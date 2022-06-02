package com.reis.vinicius.homemanagement.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import kotlinx.coroutines.flow.flow

open class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val auth = Firebase.auth
    val currentUser = MutableLiveData<FirebaseUser?>(auth.currentUser)

    sealed class Status {
        object Loading: Status()
        class Failure(val e: Exception): Status()
        class Success<T>(val result: Result<T>): Status()
    }

    class Result<T> (
        val data: T
    )
}