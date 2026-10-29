package com.vitta.app.data.remote

import com.vitta.app.data.local.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private const val BASE_URL = "http://10.0.2.2:3000/api/"

    private lateinit var appContext: android.content.Context

    /** Llamado una vez desde VittaApplication.onCreate() para poder leer el token guardado. */
    fun init(context: android.content.Context) {
        appContext = context.applicationContext
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = Interceptor { chain ->
        val token = runBlocking {
            TokenManager(appContext).tokenFlow.first()
        }
        val base = chain.request().newBuilder().apply {
            if (token != null) addHeader("Authorization", "Bearer $token")
        }.build()

        // El backend calcula el "día" de cada registro en la zona de `X-Timezone`
        // (por defecto America/Bogota) y solo acepta nombres IANA canónicos.
        val zone = if (timeZoneRejected) null else deviceTimeZone()
        var response = chain.proceed(if (zone == null) base else base.newBuilder().header("X-Timezone", zone).build())
        if (zone != null && response.code == 400 && response.peekBody(1024).string().contains("X-Timezone")) {
            // El servidor no reconoce la zona del dispositivo (p. ej. un alias como "GMT"):
            // se deja de enviar y se repite la petición con la zona por defecto del backend.
            timeZoneRejected = true
            response.close()
            response = chain.proceed(base)
        }
        // Token vencido o inválido: se borra la sesión y la navegación vuelve a Login.
        if (response.code == 401 && token != null) {
            runBlocking { TokenManager(appContext).clearToken() }
            SessionEvents.notifyExpired()
        }
        response
    }

    @Volatile
    private var timeZoneRejected = false

    /** Zona IANA del dispositivo; las variantes de UTC/GMT se envían como "UTC". */
    private fun deviceTimeZone(): String? {
        val id = runCatching { java.time.ZoneId.systemDefault().normalized().id }.getOrNull() ?: return null
        return when {
            id == "Z" || id.startsWith("GMT") || id.startsWith("UTC") || id.startsWith("Etc/") ||
                id.startsWith("+") || id.startsWith("-") -> if (id in UtcAliases) "UTC" else null
            else -> id
        }
    }

    private val UtcAliases = setOf("Z", "GMT", "UTC", "Etc/UTC", "Etc/GMT", "Etc/UCT", "Etc/Universal", "Etc/Zulu", "GMT0", "Etc/GMT0", "Etc/GMT+0", "Etc/GMT-0")

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApi: AuthApi = retrofit.create(AuthApi::class.java)
    val habitApi: HabitApi = retrofit.create(HabitApi::class.java)
    val logroApi: LogroApi = retrofit.create(LogroApi::class.java)
    val progressApi: ProgressApi = retrofit.create(ProgressApi::class.java)
    val userApi: UserApi = retrofit.create(UserApi::class.java)
}