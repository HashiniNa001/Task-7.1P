package com.example.lostandfoundapp

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lostandfoundapp.databinding.ActivityCreateAdvertBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import java.util.*
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CreateAdvertActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateAdvertBinding
    private lateinit var dbHelper: LostFoundDatabaseHelper
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var selectedLatLng: LatLng? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private val cancellationTokenSource = CancellationTokenSource()

    companion object {
        private const val AUTOCOMPLETE_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAdvertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = LostFoundDatabaseHelper(this)

        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "YOUR_GOOGLE_MAPS_API_KEY")
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Open place autocomplete
        binding.editTextLocation.setOnClickListener {
            val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this)
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }

        // Get current location
        binding.buttonGetLocation.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            } else {
                getCurrentLocation()
            }
        }


        // Save advert
        binding.buttonSave.setOnClickListener {
            val type = if (binding.radioLost.isChecked) "Lost" else "Found"
            val name = binding.editTextName.text.toString()
            val phone = binding.editTextPhone.text.toString()
            val description = binding.editTextDescription.text.toString()
            val date = binding.editTextDate.text.toString()
            val location = binding.editTextLocation.text.toString()
            val latitude = selectedLatLng?.latitude
            val longitude = selectedLatLng?.longitude

            val success = dbHelper.insertItem(type, name, phone, description, date, location, latitude, longitude)
            if (success) {
                Toast.makeText(this, "Item saved!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Error saving item", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val place = Autocomplete.getPlaceFromIntent(data)
            binding.editTextLocation.setText(place.address)
            selectedLatLng = place.latLng
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun getCurrentLocation() {
        try {
            binding.buttonGetLocation.isEnabled = false
            binding.buttonGetLocation.text = "Getting location..."

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                        .setWaitForAccurateLocation(false)
                        .setMinUpdateIntervalMillis(2000)
                        .setMaxUpdateDelayMillis(10000)
                        .build()

                    val location = fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        cancellationTokenSource.token
                    ).await()

                    withContext(Dispatchers.Main) {
                        if (location != null) {
                            try {
                                val geocoder = Geocoder(this@CreateAdvertActivity, Locale.getDefault())
                                val addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                if (!addressList.isNullOrEmpty()) {
                                    val address = addressList[0].getAddressLine(0)
                                    binding.editTextLocation.setText(address)
                                    selectedLatLng = LatLng(location.latitude, location.longitude)
                                    Toast.makeText(this@CreateAdvertActivity, "Location updated successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this@CreateAdvertActivity, "Could not get address for location", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(this@CreateAdvertActivity, "Error getting address: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@CreateAdvertActivity, "Location not available. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@CreateAdvertActivity, "Error getting location: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } finally {
                    withContext(Dispatchers.Main) {
                        binding.buttonGetLocation.isEnabled = true
                        binding.buttonGetLocation.text = "Get Current Location"
                    }
                }
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show()
            binding.buttonGetLocation.isEnabled = true
            binding.buttonGetLocation.text = "Get Current Location"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancellationTokenSource.cancel()
    }
}
