package com.example.rinconsur.data.remote

import retrofit2.http.GET

interface RolApi {
    @GET("api/roles")
    suspend fun listarRoles(): List<RolDto>
}
