package com.reis.vinicius.homemanagement.view

import android.app.Activity
import android.app.PendingIntent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.reis.vinicius.homemanagement.R
import com.reis.vinicius.homemanagement.databinding.FragmentLoginBinding
import com.reis.vinicius.homemanagement.viewModel.AuthViewModel
import com.reis.vinicius.homemanagement.viewModel.MainViewModel

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private val viewModel: AuthViewModel by activityViewModels()
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var signUpRequest: BeginSignInRequest
    private lateinit var signInResultLauncher: ActivityResultLauncher<IntentSenderRequest>
    private val facebookCallbackManager = CallbackManager.Factory.create()

    companion object {
        object FacebookPermissions {
            const val EMAIL = "email"
            const val PUBLIC_PROFILE = "public_profile"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        oneTapClient = Identity.getSignInClient(requireActivity())
        signInRequest = BeginSignInRequest.builder()
            .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
                .setSupported(true)
                .build())
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.google_oauth_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build())
            .setAutoSelectEnabled(true)
            .build()
        signUpRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.google_oauth_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build())
            .build()

        createGoogleSignInLauncher()
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

        bindGoogleSignInButtonEvents()
        registerFacebookLoginCallback()
    }

    private fun bindGoogleSignInButtonEvents(){
        binding.btnGoogleSignIn.setOnClickListener {
            Log.d("AUTH", "Button pressed")
            launchGoogleOneTap(signInRequest, true)
        }
    }

    private fun createGoogleSignInLauncher(){
        signInResultLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            when (it.resultCode) {
                Activity.RESULT_OK -> {
                    val credential = oneTapClient.getSignInCredentialFromIntent(it.data)

                    viewModel.handleAuthToken(credential.googleIdToken, AuthViewModel.AuthProvider.GOOGLE)
                        .observe(viewLifecycleOwner) { status ->
                        when (status) {
                            is MainViewModel.Status.Loading -> {}
                            is MainViewModel.Status.Success<*> -> goToHome()
                            is MainViewModel.Status.Failure -> {
                                Log.e("AUTH", "Failed to sign in with credential. " +
                                        status.e.localizedMessage)
                                showMessage("Failed to authenticate. Please, try again later")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun launchGoogleOneTap(request: BeginSignInRequest, launchSignUp: Boolean){
        Log.d("AUTH", "One Tap launched")

        viewModel.launchGoogleOneTap(request).observe(viewLifecycleOwner){ status ->
            when (status) {
                is MainViewModel.Status.Loading -> {}
                is MainViewModel.Status.Failure -> {
                    Log.e("AUTH", "Failed to begin sign up. ${status.e.localizedMessage}")
                    if (launchSignUp)
                        launchGoogleOneTap(signUpRequest, false)
                }
                is MainViewModel.Status.Success<*> -> {
                    val result = status.result.data as BeginSignInResult
                    val intent = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()

                    signInResultLauncher.launch(intent)
                }
            }
        }
    }

    private fun registerFacebookLoginCallback(){
        binding.btnFacebookSignIn.setPermissions(
            listOf(
                FacebookPermissions.EMAIL,
                FacebookPermissions.PUBLIC_PROFILE
            )
        )
        binding.btnFacebookSignIn.registerCallback(facebookCallbackManager, object : FacebookCallback<LoginResult>{
            override fun onSuccess(result: LoginResult) {
                Log.d("AUTH", "Login with Facebook succeeded")

                viewModel.handleAuthToken(result.accessToken.token, AuthViewModel.AuthProvider.FACEBOOK)
                    .observe(viewLifecycleOwner) { status ->
                        when (status) {
                            is MainViewModel.Status.Loading -> {}
                            is MainViewModel.Status.Success<*> -> goToHome()
                            is MainViewModel.Status.Failure -> {
                                Log.e("AUTH", "Failed to sign in with Facebook. ${status.e.localizedMessage}")
                                showMessage("Failed to login with Facebook. Please, try again later.")
                            }
                        }
                    }
            }

            override fun onCancel() {
                Log.d("AUTH", "Login with Facebook cancelled")
                showMessage("Login cancelled")
            }

            override fun onError(error: FacebookException) {
                Log.e("AUTH", "Failed to login with Facebook. ${error.localizedMessage}")
            }
        })
    }

    private fun goToHome() {
        findNavController().navigate(LoginFragmentDirections.login())
    }

    private fun showMessage(message: String){
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}