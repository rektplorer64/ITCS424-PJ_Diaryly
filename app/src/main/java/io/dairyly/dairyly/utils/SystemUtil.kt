package io.dairyly.dairyly.utils

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.location.Geocoder
import android.net.Uri
import io.dairyly.dairyly.R
import java.util.*


const val CODE_PERMISSION_FINE_LOCATION = 400
const val CODE_PERMISSION_READ_STORAGE = 300

val CURRENT_LOCALE: Locale = Resources.getSystem().configuration.locales.get(0)

fun Context.getAreaNameByCoordinate(
        it: Pair<Double, Double>): Pair<String, String> {
    var areaName: String
    val coordinateString: String
    try {
        val address = Geocoder(this).getFromLocation(it.first, it.second, 1)[0]

        var subLocality = getString(R.string.unknown)
        if(address.subLocality != null || !address?.subLocality!!.contentEquals("null")) {
            subLocality = address.subLocality
        }

        areaName = "$subLocality, ${address.countryName}"
    } catch(e: IndexOutOfBoundsException) {
        areaName = getString(R.string.location_unknown)
    } catch(e: KotlinNullPointerException) {
        areaName = getString(R.string.location_unknown)
    }

    val formatter = Formatter(StringBuilder(), Locale.getDefault())
    coordinateString = formatter.format("%.2f, %.2f", it.first, it.second).toString()

    return Pair(areaName, coordinateString)
}

fun Context.openCoordinateInGoogleMap(coordinate: Pair<Double, Double>) {
    // Create a Uri from an intent string. Use the result to create an Intent.

    // Create a Uri from an intent string. Use the result to create an Intent.
    val gmmIntentUri: Uri = Uri.parse("geo:${coordinate.first},${coordinate.second}?z=18")

    // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)

    // Make the Intent explicit by setting the Google Maps package
    mapIntent.setPackage("com.google.android.apps.maps")

    if(mapIntent.resolveActivity(packageManager) != null) { // Attempt to start an activity that can handle the Intent
        startActivity(mapIntent)
    }
}