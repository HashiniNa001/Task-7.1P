package com.example.lostandfoundapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private lateinit var dbHelper: LostFoundDatabaseHelper
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        try {
            // Initialize Maps SDK
            MapsInitializer.initialize(applicationContext)
            
            dbHelper = LostFoundDatabaseHelper(this)

            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.mapFragment) as? SupportMapFragment
                ?: throw IllegalStateException("Map fragment not found")

            mapFragment.getMapAsync(this)
        } catch (e: Exception) {
            Toast.makeText(this, "Error initializing map: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        
        // Move database operations to background thread
        scope.launch {
            try {
                val items = withContext(Dispatchers.IO) {
                    dbHelper.getAllItems()
                }
                
                if (items.isEmpty()) {
                    Toast.makeText(this@MapActivity, "No items to display on map", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Add markers on the main thread
                for (item in items) {
                    val lat = item.latitude
                    val lng = item.longitude
                    if (lat != null && lng != null) {
                        val position = LatLng(lat, lng)
                        googleMap?.addMarker(MarkerOptions().position(position).title(item.name))
                    }
                }

                // Move camera to first item
                val firstLat = items[0].latitude
                val firstLng = items[0].longitude

                if (firstLat != null && firstLng != null) {
                    val first = LatLng(firstLat, firstLng)
                    googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(first, 10f))
                }
            } catch (e: Exception) {
                Toast.makeText(this@MapActivity, "Error loading map data: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Ensure map is properly initialized when activity resumes
        googleMap?.let { map ->
            try {
                map.clear()
                // Reload markers if needed
                scope.launch {
                    try {
                        val items = withContext(Dispatchers.IO) {
                            dbHelper.getAllItems()
                        }
                        
                        for (item in items) {
                            val lat = item.latitude
                            val lng = item.longitude
                            if (lat != null && lng != null) {
                                val position = LatLng(lat, lng)
                                map.addMarker(MarkerOptions().position(position).title(item.name))
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@MapActivity, "Error reloading map data: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error refreshing map: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Clean up map resources when activity is paused
        googleMap?.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up resources
        googleMap = null
        scope.launch {
            // Cancel any ongoing coroutines
        }
    }
}
