package com.reis.vinicius.homemanagement.viewModel

import android.app.Application
import androidx.lifecycle.liveData
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class AuthViewModel(application: Application): MainViewModel(application) {
    enum class AuthProvider {
        GOOGLE,
        FACEBOOK
    }

    private var auth: FirebaseAuth = Firebase.auth
    private var oneTapClient: SignInClient

    init {
        oneTapClient = Identity.getSignInClient(application.applicationContext)
    }

    fun handleAuthToken(token: String?, provider: AuthProvider) = liveData {
        try {
            emit(Status.Loading)

            token?.let {
                when (provider) {
                    AuthProvider.GOOGLE -> {
                        val credential = GoogleAuthProvider.getCredential(token, null)

                        emit(Status.Success(Result(auth.signInWithCredential(credential).await())))
                    }
                    AuthProvider.FACEBOOK -> {
                        val credential = FacebookAuthProvider.getCredential(token)

                        emit(Status.Success(Result(auth.signInWithCredential(credential).await())))
                        currentUser.value = auth.currentUser
                    }
                }
            } ?: throw Exception("No token found for authentication")
        } catch (e: Exception){
            emit(Status.Failure(e))
        }
    }

    fun launchGoogleOneTap(request: BeginSignInRequest) = liveData {
        try {
            emit(Status.Loading)
            emit(Status.Success(Result(oneTapClient.beginSignIn(request).await())))
        } catch (e: Exception) {
            emit(Status.Failure(e))
        }
    }
}
