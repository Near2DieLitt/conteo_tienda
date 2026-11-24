package com.example.rinconsur.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val URL_GATEWAY = "https://ks8m5sc1-8800.brs.devtunnels.ms/"

    private val clienteHttp: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(URL_GATEWAY)
            .client(clienteHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val rolApi: RolApi by lazy { retrofit.create(RolApi::class.java) }
    val usuarioRemotoApi: UsuarioRemotoApi by lazy { retrofit.create(UsuarioRemotoApi::class.java) }
    val notificacionesApi: NotificacionesApi by lazy { retrofit.create(NotificacionesApi::class.java) }
    val sesionRemotaApi: SesionRemotaApi by lazy { retrofit.create(SesionRemotaApi::class.java) }
    val visitanteRemotoApi: VisitanteRemotoApi by lazy { retrofit.create(VisitanteRemotoApi::class.java) }
    val analiticaRemotaApi: AnaliticaRemotaApi by lazy { retrofit.create(AnaliticaRemotaApi::class.java) }
}