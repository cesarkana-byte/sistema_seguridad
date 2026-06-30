package com.example.seguridad.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.seguridad.data.model.Incident
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import kotlin.math.roundToInt

@Composable
fun MapScreen(
    incidents: List<Incident>
) {
    val context = LocalContext.current

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

            controller.setZoom(15.0)
            controller.setCenter(arequipa)
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
            .padding(16.dp)
    ) {
        Text(
            text = "Mapa de vigilancia",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Marcadores registrados: ${incidents.size}"
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(430.dp)
                .padding(top = 10.dp),
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
                        val oldMarkers = view.overlays.filterIsInstance<Marker>()
                        oldMarkers.forEach { marker ->
                            view.overlays.remove(marker)
                        }

                        incidents.forEach { incident ->
                            val markerPoint = GeoPoint(
                                incident.latitud,
                                incident.longitud
                            )

                            val marker = Marker(view).apply {
                                position = markerPoint
                                icon = redMarkerIcon
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                title = if (incident.estado.isNotBlank()) {
                                    incident.estado
                                } else {
                                    "Incidencia"
                                }
                                subDescription = if (incident.descripcion.isNotBlank()) {
                                    incident.descripcion
                                } else {
                                    incident.ubicacion
                                }
                            }

                            view.overlays.add(marker)
                        }

                        val signature = incidents.joinToString("|") {
                            "${it.id}:${it.latitud}:${it.longitud}"
                        }

                        if (view.tag != signature) {
                            val centerPoint = incidents.firstOrNull()?.let {
                                GeoPoint(it.latitud, it.longitud)
                            } ?: arequipa

                            view.controller.setZoom(15.0)
                            view.controller.setCenter(centerPoint)
                            view.tag = signature
                        }

                        view.invalidate()
                    }
                )
            }
        }
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
