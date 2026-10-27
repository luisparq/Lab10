package com.tecsup.lab10.view

import androidx.compose.runtime.Composable
import com.tecsup.lab10.data.SerieApiService
import com.tecsup.lab10.data.SerieModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ScreenSeries(servicio: SerieApiService) {
    CrudScreen(
        titulo = "Series · API laboratorio 8",
        cargar = { servicio.selectSeries() },
        identificar = { it.id },
        resumen = { "#${it.id} · ${it.name}\nEstreno: ${it.release_date}\nCalificación: ${it.rating} · ${it.category}" },
        consultar = { servicio.selectSerie(it).verificar() ?: error("Serie sin datos") },
        etiquetas = listOf("Nombre", "Fecha de estreno (AAAA-MM-DD)", "Calificación (entero)", "Categoría"),
        valores = { listOf(it?.name.orEmpty(), it?.release_date.orEmpty(), it?.rating?.toString() ?: "0", it?.category ?: "drama") },
        ayuda = "Categorías válidas: horror, comedy, action, drama.",
        guardar = { anterior, campos ->
            require(campos.all { it.isNotBlank() }) { "Completa todos los campos" }
            val formato = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
            val fecha = runCatching { formato.parse(campos[1]) }.getOrNull()
            require(fecha != null && formato.format(fecha) == campos[1]) { "Fecha inválida: usa AAAA-MM-DD" }
            val rating = campos[2].toIntOrNull() ?: error("La calificación debe ser un entero")
            require(campos[3] in listOf("horror", "comedy", "action", "drama")) { "Categoría inválida" }
            val serie = SerieModel(anterior?.id ?: 0, campos[0].trim(), campos[1], rating, campos[3])
            if (anterior == null) servicio.insertSerie(serie).verificar()
            else servicio.updateSerie(anterior.id, serie).verificar()
        },
        eliminar = { servicio.deleteSerie(it).verificar(); Unit }
    )
}
