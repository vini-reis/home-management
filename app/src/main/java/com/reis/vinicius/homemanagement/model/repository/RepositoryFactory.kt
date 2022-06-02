package com.reis.vinicius.homemanagement.model.repository

import android.app.Application

class RepositoryFactory {
    private lateinit var userDataRepository: UserDataRepository

    fun getInstance(application: Application, type: Repository.Type): FirebaseRepository<*> =
        when (type) {
            Repository.Type.UserData -> {
                if (!this::userDataRepository.isInitialized)
                    userDataRepository = UserDataRepository(application)

                userDataRepository
            }
        }
}
