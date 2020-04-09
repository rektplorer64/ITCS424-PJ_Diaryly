package io.dairyly.dairyly.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.*
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import es.dmoral.toasty.Toasty
import io.dairyly.dairyly.R
import io.dairyly.dairyly.screens.entry.EntryEditorViewModel
import io.dairyly.dairyly.utils.CODE_PERMISSION_FINE_LOCATION
import io.dairyly.dairyly.utils.isGrantedFineLocationPermission
import kotlinx.android.synthetic.main.activity_location.*
import kotlinx.android.synthetic.main.fragment_entry_edit.*

class LocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private var requestCode = 0
    private val LOG_TAG = this::class.java.simpleName
    private lateinit var viewModel: LocationSelectorViewModel

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        viewModel = ViewModelProvider(this).get(LocationSelectorViewModel::class.java)

        intent.extras?.let {
            LocationActivityArgs.fromBundle(it).apply {
                Log.d(LOG_TAG, "Received Location Bundle: ${coordinateLat.toDouble()}, ${coordinateLong.toDouble()}")
                viewModel.coordinate.value = Pair(coordinateLat.toDouble(), coordinateLong.toDouble())
            }
        }


        cancelBtn.setOnClickListener {
            onBackPressed()
        }

        locationConfirmBtn.setOnClickListener {
            MaterialDialog(this).show {
                cornerRadius(res = R.dimen.corner_radius)
                title(res = R.string.dialog_confirm_selected_location)
                positiveButton(android.R.string.ok) {
                    // TODO: save the data here!!!!
                    val intent = Intent().apply {
                        putExtra("lat", viewModel.coordinate.value!!.first)
                        putExtra("long", viewModel.coordinate.value!!.second)
                    }
                    Log.d(LOG_TAG, "Sending back Location Bundle: ${viewModel.coordinate.value!!.first}, ${viewModel.coordinate.value!!.second}")
                    if(parent == null) {
                        setResult(Activity.RESULT_OK, intent)
                    } else {
                        parent.setResult(Activity.RESULT_OK, intent)
                    }
                    finish()
                }
                negativeButton(android.R.string.cancel)
            }
        }

        prepareLocationServiceAndMap()
    }

    override fun onBackPressed() {
        MaterialDialog(this).show {
            cornerRadius(res = R.dimen.corner_radius)
            title(res = R.string.dialog_exit_location_selector)
            positiveButton(android.R.string.ok) {
                // TODO: save the data here!!!!
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
            negativeButton(android.R.string.cancel)
        }
    }

    @SuppressLint("MissingPermission")
    private fun prepareLocationServiceAndMap(){
        requestServiceAndCurrentLocation()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    @SuppressLint("MissingPermission")
    private fun requestServiceAndCurrentLocation() {
        val locationManager: LocationManager = this@LocationActivity.getSystemService(
                Context.LOCATION_SERVICE) as LocationManager

        // Here, thisActivity is the current activity
        if(!this@LocationActivity.isGrantedFineLocationPermission()) {

            // Permission is not granted, Should we show an explanation?
            if(ActivityCompat.shouldShowRequestPermissionRationale(this@LocationActivity,
                                                                   Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Snackbar.make(rootCoordinator, getString(R.string.permission_request_location),
                              Snackbar.LENGTH_INDEFINITE).show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this@LocationActivity,
                                                  arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                                  CODE_PERMISSION_FINE_LOCATION)

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        val locationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location?) {
                if(location == null || viewModel.coordinate.value != EntryEditorViewModel.DEFAULT_LOCATION) {
                    return
                }
                viewModel.coordinate.value = Pair(location.latitude, location.longitude)
                locationManager.removeUpdates(this)
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                // TODO("Not yet implemented")
            }

            override fun onProviderEnabled(provider: String?) {
                // TODO("Not yet implemented")
            }

            override fun onProviderDisabled(provider: String?) {
                Toasty.error(this@LocationActivity, "Location is disabled!").show()
            }
        }

        // Permission has already been granted
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f,
                                               locationListener)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(g: GoogleMap) {

        g.apply {
            setMaxZoomPreference(15f)
            uiSettings.setAllGesturesEnabled(true)

            isMyLocationEnabled = true

            setOnMapClickListener {
                g.clear()
                g.addMarker(MarkerOptions().draggable(false).position(it))
                viewModel.coordinate.value = Pair(it.latitude, it.longitude)
            }

            setOnMyLocationClickListener {
                g.clear()
                g.addMarker(MarkerOptions().draggable(false).position(LatLng(it.latitude, it.longitude)))
                viewModel.coordinate.value = Pair(it.latitude, it.longitude)
            }
        }

        viewModel.coordinate.observe(this, object : Observer<Pair<Double, Double>>{
            override fun onChanged(it: Pair<Double, Double>) {
                Log.d(LOG_TAG, "Updating the location to (${it.first}, ${it.second})")
                val position = LatLng(it.first, it.second)

                g.clear()
                g.addMarker(MarkerOptions().draggable(true).position(position))
                g.moveCamera(CameraUpdateFactory.newLatLng(position))

                // viewModel.coordinate.removeObserver(this)
            }
        })
    }

    class LocationSelectorViewModel : ViewModel(){
        val coordinate: MutableLiveData<Pair<Double, Double>> = MutableLiveData(Pair(0.0, 0.0))
    }
}
