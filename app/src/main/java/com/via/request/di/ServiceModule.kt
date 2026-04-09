package com.via.request.di

import com.via.request.mock.MockRequestService
import com.via.request.service.RequestService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {

    @Binds
    abstract fun bindRequestService(
        mockRequestService: MockRequestService
    ): RequestService

}