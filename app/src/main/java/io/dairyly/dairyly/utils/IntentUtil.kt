package io.dairyly.dairyly.utils

import android.content.Intent
import androidx.fragment.app.Fragment
import io.dairyly.dairyly.screens.entry.EntryEditFragment

fun Fragment.invokeImageSelectionIntent() {
    //Create an Intent with action as ACTION_PICK
    // val intent = Intent(Intent.ACTION_PICK)
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)

    // Sets the type as image/*. This ensures only components of type image are selected
    intent.type = "image/*"

    //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
    val mimeTypes = arrayOf("image/jpeg",
                            "image/png")
    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)

    // Launching the Intent
    startActivityForResult(intent, EntryEditFragment.REQUEST_CODE_GALLERY)
}