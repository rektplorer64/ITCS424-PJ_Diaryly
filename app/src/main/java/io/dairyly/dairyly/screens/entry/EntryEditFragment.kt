package io.dairyly.dairyly.screens.entry

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onCancel
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import es.dmoral.toasty.Toasty
import io.dairyly.dairyly.R
import io.dairyly.dairyly.models.DiaryRepo
import io.dairyly.dairyly.models.data.DiaryEntry
import io.dairyly.dairyly.models.data.DiaryImage
import io.dairyly.dairyly.models.data.DiaryTag
import io.dairyly.dairyly.screens.LocationActivity
import io.dairyly.dairyly.screens.entry.EntryEditFragment.Companion.FILE_READ_ONLY
import io.dairyly.dairyly.screens.entry.EntryEditorViewModel.Companion.DEFAULT_LOCATION
import io.dairyly.dairyly.utils.CODE_PERMISSION_FINE_LOCATION
import io.dairyly.dairyly.utils.TIME_FORMATTER_FULL
import io.dairyly.dairyly.utils.isGrantedFineLocationPermission
import io.noties.markwon.Markwon
import io.noties.markwon.editor.MarkwonEditor
import io.noties.markwon.editor.MarkwonEditorTextWatcher
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_entry_edit.*
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.Executors


class EntryEditFragment : Fragment(), OnMapReadyCallback {

    private val LOG_TAG = this::class.java.simpleName

    companion object {
        const val REQUEST_CODE_GALLERY = 100
        const val REQUEST_CODE_LOCATION_PICKER = 200
        const val FILE_READ_ONLY = "r"
    }

    private lateinit var viewModel: EntryEditorViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val markwon: Markwon = Markwon.create(context!!)
        val markwonEditor = MarkwonEditor.builder(markwon).build()

        viewModel = ViewModelProvider(requireActivity()).get(EntryEditorViewModel::class.java)

        viewModel.imageBitMap.observe(viewLifecycleOwner) {
            Toasty.info(context!!, "Image Changed: $it").show()
            if(it == null) {
                Glide.with(this@EntryEditFragment).load(R.color.colorPrimaryDark).centerCrop()
                        .into(heroImageView)
                imageBtn.setImageDrawable(context!!.getDrawable(R.drawable.ic_image_add_black_24dp))
            } else {
                Glide.with(this@EntryEditFragment).load(it).centerCrop().into(heroImageView)
                imageBtn.setImageDrawable(
                        context!!.getDrawable(R.drawable.ic_image_remove_black_24dp))
            }
        }

        viewModel.title.observe(viewLifecycleOwner) {
            titleTextView.apply {
                text = if(it.isNullOrBlank()){
                    context!!.getString(R.string.untitled)
                }else{
                    it
                }

                val textColorRes = when(it) {
                    null -> R.color.whiteAlpha50
                    else -> R.color.colorPrimary
                }
                setTextColor(context!!.getColor(textColorRes))
            }
        }

        viewModel.subtitle.observe(viewLifecycleOwner) {
            subtitleTextView.apply {
                text = if(it.isNullOrBlank()){
                    context!!.getString(R.string.untitled)
                }else{
                    it
                }

                val textColorRes = when(it) {
                    null -> R.color.whiteAlpha50
                    else -> R.color.colorPrimary
                }
                setTextColor(context!!.getColor(textColorRes))
            }
        }

        viewModel.dateText.observe(viewLifecycleOwner) {
            overlineTextView.text = it
        }

        // Configure the markdown editor
        diaryTextEditor.apply {
            addTextChangedListener(MarkwonEditorTextWatcher.withPreRender(markwonEditor,
                                                          Executors.newCachedThreadPool(),
                                                          this))
            doOnTextChanged { text, _, _, _ -> viewModel.content.value = text.toString() }
            this.text?.let { markwonEditor.process(it) }
        }

        saveBtn.setOnClickListener {
            MaterialDialog(context!!).show {
                cornerRadius(res = R.dimen.corner_radius)
                title(res = R.string.dialog_save_the_entry)
                positiveButton(android.R.string.ok) {
                    // TODO: save the data here!!!!
                    viewModel
                            .saveData()
                            .subscribe { it2, throwable ->
                                Log.d(LOG_TAG, "Data Saved Completed: $it2")
                                Toasty.success(context, getString(R.string.status_saved)).show()
                                // Toasty.success(context, it2.toString()).show()
                            }
                }
                negativeButton(android.R.string.cancel)
            }
        }

        MaterialDialog(context!!).apply {
            val prefillText = if(viewModel.title.value == null) "" else viewModel.title.value

            cornerRadius(res = R.dimen.corner_radius)
            title(R.string.dialog_edit_entry_title)
            input(waitForPositiveButton = true, prefill = prefillText,
                  hintRes = R.string.entry_title, inputType = InputType.TYPE_TEXT_FLAG_AUTO_CORRECT,
                  maxLength = 30) { dialog, inputText ->
                viewModel.title.value = inputText.toString()
            }

            onCancel { input(prefill = "") }

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                this.getInputField().setTextCursorDrawable(R.drawable.edit_text_cursor)
            }

            positiveButton { R.string.submit }
            negativeButton { R.string.cancel }


        }.also { dialog ->
            titleTextView.setOnClickListener { dialog.show() }
            titleEditBtn.setOnClickListener { dialog.show() }
        }

        MaterialDialog(context!!).apply {
            val prefillText = if(viewModel.subtitle.value == null) "" else viewModel.subtitle.value

            cornerRadius(res = R.dimen.corner_radius)
            title(R.string.dialog_edit_entry_subtitle)
            input(waitForPositiveButton = true, prefill = prefillText,
                  hintRes = R.string.entry_subtitle,
                  inputType = InputType.TYPE_TEXT_FLAG_AUTO_CORRECT,
                  maxLength = 50) { _, inputText ->
                viewModel.subtitle.value = inputText.toString()
            }

            onCancel { input(prefill = "") }

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                this.getInputField().setTextCursorDrawable(R.drawable.edit_text_cursor)
            }

            positiveButton { R.string.submit }
            negativeButton { R.string.cancel }

        }.also { dialog ->
            subtitleTextView.setOnClickListener { dialog.show() }
            subtitleEditBtn.setOnClickListener { dialog.show() }
        }

        overlayImageView.setOnClickListener {
            invokeImageSelectionIntent()
        }

        viewModel.tagCount.observe(viewLifecycleOwner) {
            if(it == 0) {
                tagOverlineText.visibility = View.GONE
            } else {
                tagOverlineText.visibility = View.VISIBLE
            }
        }

        tagBtn.setOnClickListener {
            MaterialDialog(context!!).show {
                cornerRadius(res = R.dimen.corner_radius)
                title(R.string.dialog_add_entry_tag)
                input(waitForPositiveButton = true,
                      hintRes = R.string.entry_tag,
                      inputType = InputType.TYPE_TEXT_FLAG_AUTO_CORRECT,
                      maxLength = 20) { _, inputText ->

                    // Add a new chip
                    val chip = Chip(context).apply {
                        isCloseIconVisible = true
                        val rawText = inputText.toString().trim().also {
                            text = it
                            viewModel.tagSet.add(it)
                        }
                        setChipBackgroundColorResource(R.color.colorPrimaryDark)
                        setCloseIconTintResource(android.R.color.white)
                        setTextColor(context.getColor(android.R.color.white))
                        setOnCloseIconClickListener {
                            viewModel.tagCount.value = viewModel.tagCount.value?.minus(1)
                            viewModel.tagSet.remove(rawText)
                            this@EntryEditFragment.tagEditContainer.removeView(this)
                        }
                    }
                    viewModel.tagCount.value = viewModel.tagCount.value?.plus(1)
                    this@EntryEditFragment.tagEditContainer.addView(chip)
                }

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    this.getInputField().setTextCursorDrawable(R.drawable.edit_text_cursor)
                }

                cancelOnTouchOutside(false)
                negativeButton { R.string.cancel }
            }
        }

        imageBtn.setOnClickListener {
            if(viewModel.imageUri.value == null) {
                invokeImageSelectionIntent()
            } else {
                MaterialDialog(context!!).show {
                    title(res = R.string.dialog_remove_the_image)
                    positiveButton(android.R.string.ok) {
                        viewModel.imageUri.value = null
                    }
                    negativeButton(android.R.string.cancel)
                }
            }
        }
        prepareLocationServiceAndMap()
    }

    private fun invokeImageSelectionIntent() {
        // TODO: Invoke an Image selector

        //Create an Intent with action as ACTION_PICK
        val intent = Intent(Intent.ACTION_PICK)

        // Sets the type as image/*. This ensures only components of type image are selected
        intent.type = "image/*"

        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        val mimeTypes = arrayOf("image/jpeg",
                                "image/png")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)

        // Launching the Intent
        startActivityForResult(intent, REQUEST_CODE_GALLERY)
    }

    @SuppressLint("MissingPermission")
    private fun prepareLocationServiceAndMap() {

        val locationManager: LocationManager = activity!!.getSystemService(
                Context.LOCATION_SERVICE) as LocationManager

        // Here, thisActivity is the current activity
        if(!activity!!.isGrantedFineLocationPermission()) {

            // Permission is not granted, Should we show an explanation?
            if(ActivityCompat.shouldShowRequestPermissionRationale(activity!!,
                                                                   Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Snackbar.make(rootCoordinator, getString(R.string.permission_request_location),
                              Snackbar.LENGTH_INDEFINITE).show()
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity!!,
                                                  arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                                  CODE_PERMISSION_FINE_LOCATION)

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        val locationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location?) {
                if(location == null || viewModel.coordinate.value != DEFAULT_LOCATION) {
                    return
                }
                progressMap.visibility = View.GONE
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
                Toasty.error(context!!, "Location is disabled!").show()
            }
        }

        // Permission has already been granted
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300L, 0f,
                                               locationListener)

        // val mapFragment = map.findFragment<Fragment>() as SupportMapFragment
        val mapFragment = this.childFragmentManager
                .findFragmentByTag("mapPreview") as SupportMapFragment
        mapFragment.getMapAsync(this)
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
            setMinZoomPreference(15f)
            uiSettings.setAllGesturesEnabled(false)
            setOnMapClickListener {

                if(!activity!!.isGrantedFineLocationPermission()) {
                    prepareLocationServiceAndMap()
                } else {
                    val lat = viewModel.coordinate.value!!.first
                    val long = viewModel.coordinate.value!!.second
                    if(Pair(lat, long) != DEFAULT_LOCATION) {
                        val action = EntryEditFragmentDirections.actionEntryEditFragmentToLocationActivity(
                                lat.toFloat(), long.toFloat()).arguments
                        Log.d(LOG_TAG, "Sent Location Bundle: $lat, $long")

                        val intent = Intent(activity!!, LocationActivity::class.java).putExtras(
                                action)
                        startActivityForResult(intent, REQUEST_CODE_LOCATION_PICKER)
                    }

                }
            }
        }
        viewModel.coordinate.observe(viewLifecycleOwner) {
            Log.d(LOG_TAG, "Updating the location to (${it.first}, ${it.second})")
            val position = LatLng(it.first, it.second)
            g.clear()
            g.addMarker(MarkerOptions().position(position))
            g.moveCamera(CameraUpdateFactory.newLatLng(position))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_entry_edit, container, false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK)
            when(requestCode) {
                REQUEST_CODE_GALLERY         -> {
                    //data.getData return the content URI for the selected Image
                    val selectedImage: Uri = data?.data ?: return
                    viewModel.imageUri.value = selectedImage
                }

                REQUEST_CODE_LOCATION_PICKER -> {
                    Toasty.success(context!!, getString(R.string.location_saved)).show()
                    Log.d(LOG_TAG, "Returned Location Extra: ${data!!.extras?.get(
                            "lat")}, ${data.extras?.get("long")}")

                    val lat = data.extras?.get("lat")?.toString()?.toDouble()!!
                    val long = data.extras?.get("long")?.toString()?.toDouble()!!
                    // val long = data.getFloatExtra("long", viewModel.coordinate.value!!.second.toFloat())
                    Log.d(LOG_TAG, "Returned Location: $lat  $long")
                    viewModel.coordinate.value = Pair(lat, long)
                }
            }
    }
}

class EntryEditorViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        val DEFAULT_LOCATION = Pair(0.0, 0.0)
    }

    private val LOG_TAG = this::class.java.simpleName

    val title = MutableLiveData("")
    val subtitle = MutableLiveData("")
    val content = MutableLiveData("")

    val date = MutableLiveData(Calendar.getInstance().time)
    val dateText = Transformations.map(date) {
        TIME_FORMATTER_FULL.format(it)
    }

    val imageUri = MutableLiveData<Uri?>(null)
    val imageBitMap: LiveData<Bitmap?> = Transformations.switchMap(imageUri) {
        Log.d(LOG_TAG, "imageBitMap LiveData invoked")
        it ?: return@switchMap liveData<Bitmap?> { emit(null) }
        val descriptor = application.applicationContext!!.contentResolver.openAssetFileDescriptor(
                it, FILE_READ_ONLY)!!

        var bitmap: Bitmap? = null
        viewModelScope.launch {
            bitmap = BitmapFactory.decodeFileDescriptor(descriptor.fileDescriptor, null, null)
        }
        descriptor.close()
        return@switchMap liveData { emit(bitmap) }
    }

    val tagCount = MutableLiveData(0)
    val tagSet = TreeSet<String>()

    val coordinate = MutableLiveData(DEFAULT_LOCATION)

    fun saveData(): Single<Boolean> {
        val c = Calendar.getInstance()
        val tags = tagSet.toList().map {
            DiaryTag(it)
        }

        val diaryImage = imageUri.value?.let { listOf(DiaryImage(uri = it, timeCreated = c.time)) }

        Log.d(LOG_TAG, "Saving data...")
        val entry = DiaryEntry("-1", c.time, c.time, 0, tags, title.value!!
                               , subtitle.value!!, content.value!!, diaryImage,
                               coordinate.value!!.first
                               , coordinate.value!!.second)
        return DiaryRepo
                .addNewEntry(getApplication(), entry)
                .observeOn(AndroidSchedulers.mainThread())
    }

}
