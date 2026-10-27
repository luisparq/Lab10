package com.tecsup.lab10.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tecsup.lab10.data.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Composable
fun SeriesApp() {
    // 10.0.2.2 es la computadora anfitriona vista desde el emulador.
    val series = remember {
        Retrofit.Builder().baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(SerieApiService::class.java)
    }
    val productos = remember {
        Retrofit.Builder().baseUrl("http://10.0.2.2:8001/")
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(ProductoApiService::class.java)
    }
    var seccion by remember { mutableIntStateOf(0) }
    Scaffold { espacio ->
        Column(Modifier.fillMaxSize().padding(espacio)) {
            Text("Lab10 · CRUD con Retrofit", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
            TabRow(selectedTabIndex = seccion) {
                Tab(selected = seccion == 0, onClick = { seccion = 0 }, text = { Text("Series") })
                Tab(selected = seccion == 1, onClick = { seccion = 1 }, text = { Text("Productos") })
            }
            // Cambiar de sección descarta el formulario y carga la lista actual.
            key(seccion) {
                if (seccion == 0) ScreenSeries(series) else ScreenProductos(productos)
            }
        }
    }
}
