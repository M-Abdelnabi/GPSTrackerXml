package com.example.gpstrackerxml

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.gps_tracker.ui.showDialog
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity() , OnMapReadyCallback {

    private val requestGPSPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
            if (map[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                // Precise location access granted.
                getUserLocation()
            } else if (map[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                // Only approximate location access granted.
            } else {
                // No location access granted.
            }
        }

    var userLatLng: LatLng? = null
    var googleMap: GoogleMap? = null
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    var marker: Marker? = null
    val locationCallback = object : LocationCallback() {

        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.locations.forEach { location ->
                userLatLng = LatLng(location.latitude, location.longitude)
                Log.e("Tag", "User Location latitude: ${location.latitude}")
                Log.e("Tag", "User Location longitude: ${location.longitude}")
                drawMarkerOnMAp()
            }
        }
    }

    fun drawMarkerOnMAp() {

        if (marker == null) {
            val markerOptions = MarkerOptions()
            markerOptions.title("Your Location")
            if (userLatLng != null)
                markerOptions.position(userLatLng!!)
            marker = googleMap?.addMarker(markerOptions)
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng!!, 16F))
        } else {
            if (userLatLng != null)
                marker?.position = userLatLng!!
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng!!, 16F))

        }
    }

    @SuppressLint("MissingPermission")
    private fun getUserLocation() {
        val currentLocationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 8_000)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY).build()
        fusedLocationProviderClient.requestLocationUpdates(
            currentLocationRequest, locationCallback, Looper.getMainLooper()
        )
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun isGPSPermissionAllowed(requestedPermission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this, requestedPermission
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        if (isGPSPermissionAllowed(Manifest.permission.ACCESS_FINE_LOCATION) || isGPSPermissionAllowed(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            getUserLocation()

        } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) || shouldShowRequestPermissionRationale(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {

            showDialog(title = "We Need To Access Ur Location To Find The Nearest Driver",
                positiveText = "I Understand",
                onPositiveClickListener = { dialog, which ->
                    dialog?.dismiss()
                    requestGPSPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                negativeText = "Cancel",
                onNegativeClickListener = { dialog, which ->
                    dialog?.dismiss()

                })
        } else {
            requestGPSPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
    }

}