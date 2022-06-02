package com.reis.vinicius.homemanagement

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.reis.vinicius.homemanagement.databinding.ActivityMainBinding
import com.reis.vinicius.homemanagement.model.entity.UserData
import com.reis.vinicius.homemanagement.view.LoginFragmentDirections
import com.reis.vinicius.homemanagement.viewModel.AuthViewModel
import com.reis.vinicius.homemanagement.viewModel.MainViewModel
import java.util.*

class MainActivity : AppCompatActivity() {
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = getNavController()
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        observeUser()
    }

    override fun onStart() {
        super.onStart()

        if (viewModel.currentUser.value != null)
            getNavController().navigate(LoginFragmentDirections.login())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private fun observeUser(){
        viewModel.currentUser.observe(this) {
            it?.let { user ->
                val profile = if (user.providerData.isNotEmpty()) user.providerData.first() else null

                viewModel.updateUserData(UserData(
                    user.uid,
                    profile?.displayName ?: user.displayName ?: "",
                    profile?.email ?: user.email ?: "",
                    profile?.photoUrl?.toString() ?: user.photoUrl?.toString() ?: "",
                    profile?.phoneNumber ?: user.phoneNumber ?: "",
                    profile?.providerId ?: user.providerId,
                    Date()
                )).observe(this) { status ->
                    when (status) {
                        is MainViewModel.Status.Loading -> {}
                        is MainViewModel.Status.Failure -> {
                            Log.e("AUTH", "Failed to update user data from user ${user.uid}")
                        }
                        is MainViewModel.Status.Success<*> -> {
                            Log.d("AUTH", "User ${user.uid} data is updated")
                        }
                    }
                }
            }
        }
    }

    private fun getNavController() = (supportFragmentManager.findFragmentById(
        R.id.nav_host_fragment_content_main) as NavHostFragment).navController
}