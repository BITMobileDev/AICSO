package com.aicso.data.di

import android.content.Context
import com.aicso.BuildConfig
import com.aicso.core.domain.ChatRepositoryImpl
import com.aicso.core.domain.VoiceRepositoryImpl
import com.aicso.core.util.AiCsoPreference
import com.aicso.data.api.ChatApiService
import com.aicso.data.api.VoiceApiService
import com.aicso.data.signalr.SignalRManager
import com.aicso.data.voice.VoiceStreamingManager
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
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import okhttp3.Dns
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
        .dns(Dns.SYSTEM)
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
    fun provideAiCsoPreference(@ApplicationContext context: Context, gson: Gson): AiCsoPreference {
        return AiCsoPreference(context, gson)
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



//    @Provides
//    @Singleton
//    fun provideManagedChannel(@ApplicationContext context: Context): ManagedChannel {
//        return io.grpc.android.AndroidChannelBuilder
//            .forAddress("http://localhost:5055/", 443)
////            .forAddress("aicso-dev-backend-ca.bluegrass-88201ab2.canadacentral.azurecontainerapps.io", 443)
//            .context(context)
//            .useTransportSecurity() // Use TLS since it's an external address
//            .build()
//    }

    @Provides
    @Singleton
    fun provideManagedChannel(@ApplicationContext context: Context): ManagedChannel {
        // LOCAL DEVELOPMENT - Use 10.0.2.2 for Android Emulator
        return io.grpc.android.AndroidChannelBuilder
            .forAddress("10.0.2.2", 7160)
            .context(context)
            .usePlaintext()  // No TLS for local development
            .keepAliveTime(30, TimeUnit.SECONDS)
            .keepAliveTimeout(10, TimeUnit.SECONDS)
            .keepAliveWithoutCalls(true)
            .build()

        // Production config (commented out for local dev):
        // return io.grpc.android.AndroidChannelBuilder
        //     .forAddress("aicso-dev-backend-ca.bluegrass-88201ab2.canadacentral.azurecontainerapps.io", 443)
        //     .context(context)
        //     .useTransportSecurity()
        //     .build()
    }

    @Provides
    @Singleton
    fun provideAudioRecorder(): com.aicso.core.audio.AudioRecorder {
        return com.aicso.core.audio.AudioRecorder()
    }

    @Provides
    @Singleton
    fun provideAudioPlayer(): com.aicso.core.audio.AudioPlayer {
        return com.aicso.core.audio.AudioPlayer()
    }

    @Provides
    @Singleton
    fun provideVoiceStreamingManager(
        audioRecorder: com.aicso.core.audio.AudioRecorder,
        audioPlayer: com.aicso.core.audio.AudioPlayer,
        channel: ManagedChannel
    ): VoiceStreamingManager {
        return VoiceStreamingManager(audioRecorder, audioPlayer, channel)
    }

    @Provides
    @Singleton
    fun provideVoiceRepository(
        voiceApiService: VoiceApiService,
        aiCsoPreference: AiCsoPreference,
        voiceStreamingManager: VoiceStreamingManager
    ) : VoiceRepository{
        return VoiceRepositoryImpl(voiceApiService, aiCsoPreference, voiceStreamingManager)
    }

    @Provides
    @Singleton
    fun provideSignalRManager(gson: Gson, client: OkHttpClient): SignalRManager {
        return SignalRManager(gson)
    }
}
