package com.via.request.di

import com.via.request.mock.TotallyRealRequestService
import com.via.request.service.RequestService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {

    @Binds
    abstract fun bindRequestService(
        totallyRealRequestService: TotallyRealRequestService
    ): RequestService

}