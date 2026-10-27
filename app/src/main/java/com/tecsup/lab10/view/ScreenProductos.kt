package com.tecsup.lab10.view

import androidx.compose.runtime.*
import com.tecsup.lab10.data.*
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ScreenProductos(servicio: ProductoApiService) {
    var categorias by remember { mutableStateOf<List<CategoriaModel>>(emptyList()) }
    CrudScreen(
        titulo = "Productos · API laboratorio 6",
        cargar = {
            categorias = servicio.categorias()
            servicio.listar()
        },
        identificar = { it.id },
        resumen = { producto ->
            val nombre = categorias.find { it.id == producto.categoria }?.nombre ?: producto.categoria.toString()
            "#${producto.id} · ${producto.nombre}\nPrecio: S/ ${producto.precio} · Stock: ${producto.stock}\nCategoría: $nombre"
        },
        consultar = { servicio.consultar(it).verificar() ?: error("Producto sin datos") },
        etiquetas = listOf("Nombre", "Precio (ejemplo: 8.50)", "Stock", "ID de categoría"),
        valores = { listOf(it?.nombre.orEmpty(), it?.precio ?: "0.00", it?.stock?.toString() ?: "0", (it?.categoria ?: categorias.firstOrNull()?.id)?.toString().orEmpty()) },
        ayuda = "Categorías: " + categorias.joinToString { "${it.id}: ${it.nombre}" },
        guardar = { anterior, campos ->
            require(campos[0].isNotBlank()) { "Ingresa el nombre" }
            val precio = campos[1].toBigDecimalOrNull() ?: error("Precio inválido: usa punto decimal")
            require(precio >= BigDecimal.ZERO && precio <= BigDecimal("9999.99") && precio.scale() <= 2) { "Precio entre 0 y 9999.99 con máximo dos decimales" }
            val stock = campos[2].toIntOrNull() ?: error("El stock debe ser un entero")
            require(stock >= 0) { "El stock no puede ser negativo" }
            val categoria = campos[3].toIntOrNull() ?: error("Indica el ID de categoría")
            require(categorias.any { it.id == categoria }) { "La categoría no existe" }
            val fecha = anterior?.pub_date ?: SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(Date())
            val producto = ProductoModel(anterior?.id ?: 0, categoria, campos[0].trim(), precio.toPlainString(), stock, fecha)
            // No se envía imagen: conserva la existente durante una edición.
            if (anterior == null) servicio.crear(producto).verificar()
            else servicio.actualizar(anterior.id, producto).verificar()
        },
        eliminar = { servicio.eliminar(it).verificar(); Unit }
    )
}
