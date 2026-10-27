package com.tecsup.lab10.view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import retrofit2.Response

fun <T> Response<T>.verificar(): T? {
    if (!isSuccessful) {
        val detalle = errorBody()?.string().orEmpty().take(500)
        error("HTTP ${code()}: $detalle")
    }
    return body()
}

// Pantalla compartida: espera la respuesta antes de cerrar y recargar.
@Composable
fun <T> CrudScreen(
    titulo: String,
    cargar: suspend () -> List<T>,
    identificar: (T) -> Int,
    resumen: (T) -> String,
    consultar: suspend (Int) -> T,
    etiquetas: List<String>,
    valores: (T?) -> List<String>,
    ayuda: String,
    guardar: suspend (T?, List<String>) -> Unit,
    eliminar: suspend (Int) -> Unit
) {
    var lista by remember { mutableStateOf<List<T>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var revision by remember { mutableIntStateOf(0) }
    var formulario by remember { mutableStateOf(false) }
    var actual by remember { mutableStateOf<T?>(null) }
    var campos by remember { mutableStateOf<List<String>>(emptyList()) }
    var pendiente by remember { mutableStateOf<T?>(null) }
    var ocupado by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(revision) {
        cargando = true
        error = null
        try { lista = cargar() }
        catch (e: CancellationException) { throw e }
        catch (e: Exception) { error = e.message ?: "No se pudo conectar" }
        finally { cargando = false }
    }

    fun ejecutar(accion: suspend () -> Unit) {
        ocupado = true
        error = null
        scope.launch {
            try { accion() }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { error = e.message ?: "Operación fallida" }
            finally { ocupado = false }
        }
    }

    BackHandler(formulario && !ocupado) { formulario = false; error = null }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(titulo, style = MaterialTheme.typography.headlineSmall)
        if (ocupado) LinearProgressIndicator(Modifier.fillMaxWidth())
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        mensaje?.let { Text(it, color = MaterialTheme.colorScheme.primary) }

        if (formulario) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(if (actual == null) "Nuevo registro" else "Editar registro #${identificar(actual!!)}")
                Text(ayuda, style = MaterialTheme.typography.bodySmall)
                etiquetas.forEachIndexed { indice, etiqueta ->
                    OutlinedTextField(
                        value = campos.getOrElse(indice) { "" },
                        onValueChange = { texto -> campos = campos.toMutableList().also { it[indice] = texto } },
                        label = { Text(etiqueta) },
                        enabled = !ocupado,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(enabled = !ocupado, onClick = {
                        ejecutar {
                            guardar(actual, campos)
                            formulario = false
                            mensaje = "Registro guardado correctamente"
                            revision++
                        }
                    }) { Text("Guardar") }
                    OutlinedButton(enabled = !ocupado, onClick = { formulario = false; error = null }) { Text("Cancelar") }
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(enabled = !ocupado && !cargando && error == null, onClick = {
                    actual = null; campos = valores(null); formulario = true; mensaje = null
                }) { Text("Nuevo") }
                OutlinedButton(enabled = !ocupado && !cargando, onClick = { revision++; mensaje = null }) { Text("Actualizar") }
            }
            if (cargando) CircularProgressIndicator()
            else if (lista.isEmpty()) Text("No hay registros disponibles.")
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(lista, key = { identificar(it) }) { item ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(resumen(item))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                TextButton(enabled = !ocupado, onClick = {
                                    ejecutar {
                                        actual = consultar(identificar(item))
                                        campos = valores(actual)
                                        formulario = true
                                        mensaje = null
                                    }
                                }) { Text("Ver / Editar") }
                                TextButton(enabled = !ocupado, onClick = { pendiente = item; error = null }) { Text("Eliminar") }
                            }
                        }
                    }
                }
            }
        }
    }

    pendiente?.let { item ->
        AlertDialog(
            onDismissRequest = { if (!ocupado) pendiente = null },
            title = { Text("Confirmar eliminación") },
            text = { Column { Text("¿Eliminar el registro #${identificar(item)}?"); error?.let { Text(it) } } },
            confirmButton = {
                TextButton(enabled = !ocupado, onClick = {
                    ejecutar {
                        eliminar(identificar(item))
                        pendiente = null
                        mensaje = "Registro eliminado correctamente"
                        revision++
                    }
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(enabled = !ocupado, onClick = { pendiente = null }) { Text("Cancelar") } }
        )
    }
}
