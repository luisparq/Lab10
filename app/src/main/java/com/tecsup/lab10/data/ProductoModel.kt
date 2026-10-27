package com.tecsup.lab10.data

data class CategoriaModel(val id: Int, val nombre: String)

data class ProductoModel(
    val id: Int = 0,
    val categoria: Int,
    val nombre: String,
    val precio: String,
    val stock: Int,
    val pub_date: String,
    val imagen: String? = null
)
