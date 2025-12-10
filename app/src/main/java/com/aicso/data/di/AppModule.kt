package com.aicso.data.di

import android.content.Context
import com.aicso.BuildConfig
import com.aicso.core.domain.ChatRepositoryImpl
import com.aicso.core.domain.VoiceRepositoryImpl
import com.aicso.core.util.AiCsoPreference
import com.aicso.data.api.ChatApiService
import com.aicso.data.api.VoiceApiService
import com.aicso.data.signalr.SignalRManager
//import com.aicso.data.signalr.SignalRService
import com.aicso.data.websocket.WebSocketManager
import com.aicso.domain.repository.ChatRepository
import com.aicso.domain.repository.VoiceRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlin.jvm.java


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

//    Retrofit -> HttpLogging
//    API service -> Retrofit
//    RepositoryImpl; -> APi service
//    Repository impl -> Repository (interface)

    @Provides
    @Singleton
    fun provideOkhttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor (HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .pingInterval(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()


    @Provides
    @Singleton
    fun provideChatApiService(retrofit: Retrofit): ChatApiService {
        return retrofit.create(ChatApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideVoiceApiService(retrofit: Retrofit): VoiceApiService {
        return retrofit.create(VoiceApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAiCsoPreference(@ApplicationContext context: Context): AiCsoPreference {
        return AiCsoPreference(context)
    }

//    @Provides
//    @Singleton
//    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .setLenient() // Accept malformed JSON
        .serializeNulls()
        .setPrettyPrinting()
        .create()

    @Provides
    @Singleton
    fun provideWebSocketManager(gson: Gson, aiCsoPreference: AiCsoPreference,client : OkHttpClient): WebSocketManager =
        WebSocketManager(gson, client,aiCsoPreference)

    @Provides
    @Singleton
    fun provideChatRepository(chatApiService: ChatApiService, webSocketManager: WebSocketManager, aiCsoPreference : AiCsoPreference): ChatRepository {
        return ChatRepositoryImpl( webSocketManager, chatApiService, aiCsoPreference)

    }



    @Provides
    @Singleton
    fun provideVoiceRepository(voiceApiService: VoiceApiService, aiCsoPreference: AiCsoPreference) : VoiceRepository{
        return VoiceRepositoryImpl(voiceApiService, aiCsoPreference)
    }

    @Provides
    @Singleton
    fun provideSignalRManager(gson: Gson, client: OkHttpClient): SignalRManager {
        return SignalRManager(gson)
    }
}



//
//package com.aicso.di
//
//import com.aicso.BuildConfig
//import com.aicso.data.api.ChatApiService
//import com.aicso.data.signalr.SignalRManager
//import com.google.gson.Gson
//import com.google.gson.GsonBuilder
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.components.SingletonComponent
//import okhttp3.OkHttpClient
//import okhttp3.logging.HttpLoggingInterceptor
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import java.util.concurrent.TimeUnit
//import javax.inject.Singleton
//
//@Module
//@InstallIn(SingletonComponent::class)
//object NetworkModule {
//
//    @Provides
//    @Singleton
//    fun provideGson(): Gson {
//        return GsonBuilder()
//            .setLenient()
//            .create()
//    }
//
//    @Provides
//    @Singleton
//    fun provideOkHttpClient(): OkHttpClient {
//        val loggingInterceptor = HttpLoggingInterceptor().apply {
//            level = if (BuildConfig.DEBUG) {
//                HttpLoggingInterceptor.Level.BODY
//            } else {
//                HttpLoggingInterceptor.Level.NONE
//            }
//        }
//
//        return OkHttpClient.Builder()
//            .addInterceptor(loggingInterceptor)
//            .connectTimeout(30, TimeUnit.SECONDS)
//            .readTimeout(30, TimeUnit.SECONDS)
//            .writeTimeout(30, TimeUnit.SECONDS)
//            .build()
//    }
//
//    @Provides
//    @Singleton
//    fun provideRetrofit(
//        okHttpClient: OkHttpClient,
//        gson: Gson
//    ): Retrofit {
//        return Retrofit.Builder()
//            .baseUrl(BuildConfig.BASE_URL)
//            .client(okHttpClient)
//            .addConverterFactory(GsonConverterFactory.create(gson))
//            .build()
//    }
//
//    @Provides
//    @Singleton
//    fun provideChatApiService(retrofit: Retrofit): ChatApiService {
//        return retrofit.create(ChatApiService::class.java)
//    }
//
//    @Provides
//    @Singleton
//    fun provideSignalRManager(): SignalRManager {
//        return SignalRManager()
//    }
//}