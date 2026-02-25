package com.example.smarthabitcoach.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.example.smarthabitcoach.BuildConfig
import com.example.smarthabitcoach.data.ai.OpenAiService
import com.example.smarthabitcoach.data.di.ApiKey
import com.example.smarthabitcoach.data.local.DailyNudgeDao
import com.example.smarthabitcoach.data.local.HabitDao
import com.example.smarthabitcoach.data.local.HabitDatabase
import com.example.smarthabitcoach.data.local.WeeklyInsightDao
import com.example.smarthabitcoach.data.repository.AiRepositoryImpl
import com.example.smarthabitcoach.data.repository.DailyNudgeCacheRepositoryImpl
import com.example.smarthabitcoach.data.repository.HabitRepositoryImpl
import com.example.smarthabitcoach.data.repository.WeeklyInsightCacheRepositoryImpl
import com.example.smarthabitcoach.domain.repository.AiRepository
import com.example.smarthabitcoach.domain.repository.DailyNudgeCacheRepository
import com.example.smarthabitcoach.domain.repository.HabitRepository
import com.example.smarthabitcoach.domain.repository.WeeklyInsightCacheRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBindsModule {
    @Binds abstract fun bindHabitRepository(impl: HabitRepositoryImpl): HabitRepository
    @Binds abstract fun bindInsightCache(impl: WeeklyInsightCacheRepositoryImpl): WeeklyInsightCacheRepository
    @Binds abstract fun bindAiRepository(impl: AiRepositoryImpl): AiRepository
    @Binds abstract fun bindDailyNudgeCache(impl: DailyNudgeCacheRepositoryImpl): DailyNudgeCacheRepository
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryProvidesModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HabitDatabase =
        Room.databaseBuilder(context, HabitDatabase::class.java, "habits.db")
            .fallbackToDestructiveMigration(true)
            .build()

    @Provides fun provideHabitDao(db: HabitDatabase): HabitDao = db.habitDao()
    @Provides fun provideWeeklyInsightDao(db: HabitDatabase): WeeklyInsightDao = db.weeklyInsightDao()
    @Provides fun provideDailyNudgeDao(db: HabitDatabase): DailyNudgeDao = db.dailyNudgeDao()

    @Provides @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Provides @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    @Provides @Singleton
    fun provideOpenAiService(client: OkHttpClient, json: Json): OpenAiService =
        Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .client(client)
            .addConverterFactory(KotlinxSerializationConverterFactory(json))
            .build()
            .create(OpenAiService::class.java)

    @Provides @Singleton @ApiKey
    fun provideApiKey(): String = BuildConfig.OPENAI_API_KEY

    @Provides @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}

private class KotlinxSerializationConverterFactory(private val json: Json) : Converter.Factory() {
    private val contentType = "application/json; charset=UTF-8".toMediaType()

    override fun responseBodyConverter(
        type: Type, annotations: Array<out Annotation>, retrofit: Retrofit
    ): Converter<ResponseBody, *> = Converter<ResponseBody, Any> { body ->
        val deserializer = serializer(type)
        json.decodeFromString(deserializer, body.string())
    }

    override fun requestBodyConverter(
        type: Type, parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>, retrofit: Retrofit
    ): Converter<*, RequestBody> = Converter<Any, RequestBody> { value ->
        val ser = serializer(type)
        json.encodeToString(ser, value).toRequestBody(contentType)
    }
}
