package com.tecsup.lab10.data

import retrofit2.Response
import retrofit2.http.*

interface ProductoApiService {
    @GET("api/categorias/")
    suspend fun categorias(): List<CategoriaModel>

    @GET("api/productos/")
    suspend fun listar(): List<ProductoModel>

    @GET("api/productos/{id}/")
    suspend fun consultar(@Path("id") id: Int): Response<ProductoModel>

    @POST("api/productos/")
    suspend fun crear(@Body producto: ProductoModel): Response<ProductoModel>

    @PUT("api/productos/{id}/")
    suspend fun actualizar(@Path("id") id: Int, @Body producto: ProductoModel): Response<ProductoModel>

    // La API genérica devuelve 204 sin cuerpo al eliminar.
    @DELETE("api/productos/{id}/")
    suspend fun eliminar(@Path("id") id: Int): Response<Unit>
}
