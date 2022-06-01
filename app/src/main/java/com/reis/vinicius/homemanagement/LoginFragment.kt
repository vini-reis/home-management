package com.reis.vinicius.homemanagement

import android.app.Activity
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.reis.vinicius.homemanagement.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var signUpRequest: BeginSignInRequest
    private lateinit var signInResultLauncher: ActivityResultLauncher<IntentSenderRequest>
    private var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        currentUser = auth.currentUser
        oneTapClient = Identity.getSignInClient(requireActivity())
        signInRequest = BeginSignInRequest.builder()
            .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
                .setSupported(true)
                .build())
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build())
            .setAutoSelectEnabled(true)
            .build()
        signUpRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build())
            .build()

        registerGoogleSignIn()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        if (currentUser != null)
            findNavController().navigate(LoginFragmentDirections.login())

        bindGoogleSignInButtonEvents()
    }

    private fun bindGoogleSignInButtonEvents(){
        binding.btnGoogleSignIn.setOnClickListener {
            launchGoogleSignIn()
        }
    }

    private fun registerGoogleSignIn(){
        signInResultLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            when (it.resultCode) {
                Activity.RESULT_OK -> {
                    try {
                        val googleCredential = oneTapClient.getSignInCredentialFromIntent(it.data)
                        val idToken = googleCredential.googleIdToken

                        if (idToken == null)
                            Log.e("AUTH", "No ID Token!")
                        else {
                            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                            auth.signInWithCredential(firebaseCredential)
                                .addOnCompleteListener { result ->
                                    if (result.isSuccessful)
                                        goToHome()
                                    else
                                        Log.e("AUTH", "Failed to sign in with credential. " +
                                                "${result.exception?.localizedMessage}")
                                }
                        }
                    } catch (e: ApiException) {
                        Log.e("AUTH", "Failed to get sign in result: $e")
                    }
                }
            }
        }
    }

    private fun launchGoogleSignUp(){
        oneTapClient.beginSignIn(signUpRequest)
            .addOnSuccessListener {
                try {
                    val intent = IntentSenderRequest.Builder(it.pendingIntent.intentSender)
                        .build()

                    signInResultLauncher.launch(intent)
                } catch (e: IntentSender.SendIntentException){
                    Log.e("AUTH", "Failed to launch sign up. $e.localizedMessage")
                }
            }
            .addOnFailureListener { e ->
                Log.e("AUTH", "Failed to begin sign up. $e.localizedMessage")
            }
    }

    private fun launchGoogleSignIn(){
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener {
                try {
                    val intent = IntentSenderRequest.Builder(it.pendingIntent.intentSender).build()

                    signInResultLauncher.launch(intent)
                } catch (e: IntentSender.SendIntentException){
                    Log.e("AUTH", "Failed to launch sign-in UI. $e.localizedMessage")
                }
            }
            .addOnFailureListener { e ->
                launchGoogleSignUp()
                Log.e("AUTH", "Failed to begin sign-in. $e.localizedMessage")
            }
    }

    private fun goToHome() {
        findNavController().navigate(LoginFragmentDirections.login())
    }
}