package com.aicso.data.di

import com.aicso.core.domain.ChatRepositoryImpl
import com.aicso.data.websocket.WebSocketManager
import com.aicso.domain.repository.ChatRepository
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideWebSocketManager(gson: Gson): WebSocketManager =
        WebSocketManager(gson)

    @Provides
    @Singleton
    fun provideChatRepository(webSocketManager: WebSocketManager): ChatRepository =
        ChatRepositoryImpl(webSocketManager)
}