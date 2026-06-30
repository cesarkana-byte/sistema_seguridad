package com.example.seguridad.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import kotlin.math.roundToInt

@Composable
fun IncidenceFormScreen(
    user: Any? = null,
    state: Any? = null,
    onRegister: (String, String, String, String) -> Unit
) {
    val context = LocalContext.current

    var descripcion by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("Plaza de Armas de Arequipa") }
    var latitud by remember { mutableStateOf("-16.398866") }
    var longitud by remember { mutableStateOf("-71.536961") }

    val arequipa = remember {
        GeoPoint(-16.398866, -71.536961)
    }

    val arequipaLimit = remember {
        BoundingBox(
            -15.40,
            -70.40,
            -17.70,
            -72.90
        )
    }

    val redMarkerIcon = remember {
        createRedMarkerIcon(context)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val grantedFine = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val grantedCoarse = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (grantedFine || grantedCoarse) {
            obtenerUbicacionActual(
                context = context,
                onLocationReady = { point ->
                    latitud = "%.6f".format(point.latitude)
                    longitud = "%.6f".format(point.longitude)
                    ubicacion = "Mi ubicación actual"
                }
            )
        } else {
            Toast.makeText(
                context,
                "Permiso de ubicación denegado",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val mapView = remember {
        Configuration.getInstance().userAgentValue = context.packageName

        MapView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            setBuiltInZoomControls(false)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

            setMinZoomLevel(11.0)
            setMaxZoomLevel(19.0)
            setScrollableAreaLimitDouble(arequipaLimit)

            controller.setZoom(16.0)
            controller.setCenter(arequipa)

            overlays.add(
                MapEventsOverlay(
                    object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                            latitud = "%.6f".format(p.latitude)
                            longitud = "%.6f".format(p.longitude)
                            ubicacion = "Ubicación marcada en el mapa"
                            controller.animateTo(p)
                            return true
                        }

                        override fun longPressHelper(p: GeoPoint): Boolean {
                            latitud = "%.6f".format(p.latitude)
                            longitud = "%.6f".format(p.longitude)
                            ubicacion = "Ubicación marcada en el mapa"
                            controller.animateTo(p)
                            return true
                        }
                    }
                )
            )
        }
    }

    DisposableEffect(mapView) {
        mapView.onResume()

        onDispose {
            mapView.onPause()
            mapView.onDetach()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Registrar incidencia",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Completa la descripción y selecciona la ubicación del incidente."
        )

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Descripción de la incidencia") },
            minLines = 3
        )

        OutlinedTextField(
            value = ubicacion,
            onValueChange = { ubicacion = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Ubicación escrita") }
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (hasLocationPermission(context)) {
                    obtenerUbicacionActual(
                        context = context,
                        onLocationReady = { point ->
                            latitud = "%.6f".format(point.latitude)
                            longitud = "%.6f".format(point.longitude)
                            ubicacion = "Mi ubicación actual"
                            mapView.controller.setZoom(17.0)
                            mapView.controller.animateTo(point)
                        }
                    )
                } else {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }
        ) {
            Text("Detectar mi ubicación actual")
        }

        Text(
            text = "También puedes tocar el mapa para marcar manualmente la ubicación.",
            style = MaterialTheme.typography.bodyMedium
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = {
                        mapView
                    },
                    update = { view ->
                        val selectedPoint = GeoPoint(
                            latitud.toDoubleOrNull() ?: -16.398866,
                            longitud.toDoubleOrNull() ?: -71.536961
                        )

                        val oldMarkers = view.overlays.filterIsInstance<Marker>()
                        oldMarkers.forEach { marker ->
                            view.overlays.remove(marker)
                        }

                        val marker = Marker(view).apply {
                            position = selectedPoint
                            icon = redMarkerIcon
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            title = "Ubicación de la incidencia"
                            subDescription = ubicacion
                        }

                        view.overlays.add(marker)
                        view.invalidate()
                    }
                )
            }
        }

        Text(
            text = "Latitud: $latitud"
        )

        Text(
            text = "Longitud: $longitud"
        )

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                onRegister(
                    descripcion,
                    ubicacion,
                    latitud,
                    longitud
                )
            }
        ) {
            Text("Guardar incidencia")
        }
    }
}

private fun hasLocationPermission(context: Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val coarse = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    return fine || coarse
}

@SuppressLint("MissingPermission")
private fun obtenerUbicacionActual(
    context: Context,
    onLocationReady: (GeoPoint) -> Unit
) {
    if (!hasLocationPermission(context)) {
        Toast.makeText(
            context,
            "Falta permiso de ubicación",
            Toast.LENGTH_SHORT
        ).show()
        return
    }

    val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    val cancellationTokenSource = CancellationTokenSource()

    fusedClient.getCurrentLocation(
        Priority.PRIORITY_HIGH_ACCURACY,
        cancellationTokenSource.token
    ).addOnSuccessListener { location ->
        if (location != null) {
            onLocationReady(
                GeoPoint(
                    location.latitude,
                    location.longitude
                )
            )

            Toast.makeText(
                context,
                "Ubicación actual detectada",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                context,
                "No se pudo obtener ubicación. Activa el GPS.",
                Toast.LENGTH_LONG
            ).show()
        }
    }.addOnFailureListener {
        Toast.makeText(
            context,
            "Error al obtener ubicación actual",
            Toast.LENGTH_LONG
        ).show()
    }
}

private fun createRedMarkerIcon(context: Context): BitmapDrawable {
    val density = context.resources.displayMetrics.density

    val size = (24 * density).roundToInt()
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    paint.color = Color.WHITE
    canvas.drawCircle(
        size / 2f,
        size / 2f,
        size / 2f,
        paint
    )

    paint.color = Color.rgb(220, 0, 0)
    canvas.drawCircle(
        size / 2f,
        size / 2f,
        size * 0.34f,
        paint
    )

    return BitmapDrawable(context.resources, bitmap)
}

