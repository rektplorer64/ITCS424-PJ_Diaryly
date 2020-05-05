package io.dairyly.dairyly.screens.entry

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.children
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.color.colorChooser
import com.afollestad.materialdialogs.datetime.dateTimePicker
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import es.dmoral.toasty.Toasty
import io.dairyly.dairyly.R
import io.dairyly.dairyly.models.data.DiaryTag
import io.dairyly.dairyly.screens.LocationActivity
import io.dairyly.dairyly.ui.recyclerview.MarginItemDecoration
import io.dairyly.dairyly.utils.*
import io.dairyly.dairyly.viewmodels.EntryEditorViewModel
import io.dairyly.dairyly.viewmodels.EntryEditorViewModel.Companion.EMPTY_LOCATION
import io.noties.markwon.Markwon
import io.noties.markwon.editor.MarkwonEditor
import io.noties.markwon.editor.MarkwonEditorTextWatcher
import kotlinx.android.synthetic.main.btm_sheet_entry_edit.*
import kotlinx.android.synthetic.main.fragment_entry_edit.*
import java.io.IOException
import java.util.*
import java.util.concurrent.Executors

/**
 * A Fragment responsible for hosting a whole process of Editing/Creating a DiaryEntry
 * @property LOG_TAG String String Tag string for showing Debugging Log
 * @property viewModel EntryEditorViewModel Data holder for this Fragment
 */
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

        viewModel = ViewModelProvider(activity!!).get(
                EntryEditorViewModel::class.java)

        toolbar.apply {
            title = if(viewModel.isModification){
                getString(R.string.title_entry_edit)
            }else{
                getString(R.string.title_entry_create)
            }

            setNavigationOnClickListener {
                activity!!.onBackPressed()
            }
        }

        val markwon: Markwon = Markwon.create(context!!)
        val markwonEditor = MarkwonEditor.builder(markwon).build()

        val imageAdapter = ImageCarouselRvAdapter()
        imageCarousel.apply {
            adapter = imageAdapter
            clipToPadding = false
            imageCarouselIndicator.setViewPager(this)
        }
        imageAdapter.registerAdapterDataObserver(imageCarouselIndicator.adapterDataObserver)
        viewModel.allImages.observe(viewLifecycleOwner) {
            Log.d(LOG_TAG, "Image Changed: $it")
            imageAdapter.submitList(it)
            // if(it.isNullOrEmpty()) {
            //     Glide.with(this@EntryEditFragment).load(R.color.colorPrimaryDark).centerCrop()
            //             .into(heroImageView)
            //     imageBtn.setChipIconResource(R.drawable.ic_image_add_black_24dp)
            // } else {
            //     Glide.with(this@EntryEditFragment).load(it.last()).centerCrop().into(heroImageView)
            //     imageBtn.setChipIconResource(R.drawable.ic_image_remove_black_24dp)
            // }
        }

        viewModel.title.observe(viewLifecycleOwner) {
            Log.d(LOG_TAG, "Title Changed: $it")
            titleTextView.apply {
                text = if(it.isNullOrBlank()) {
                    context!!.getString(R.string.untitled)
                } else {
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
            Log.d(LOG_TAG, "Subtitle Changed: $it")
            subtitleTextView.apply {
                text = if(it.isNullOrBlank()) {
                    context!!.getString(R.string.untitled)
                } else {
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

        overlineTextView.setOnClickListener {
            MaterialDialog(it.context).show {

                title(res = R.string.dialog_edit_entry_date_created)
                val c = Calendar.getInstance().apply {
                    time = viewModel.date.value!!
                }

                dateTimePicker(currentDateTime = c, show24HoursView = true) { _, dateTime ->
                    viewModel.date.value = dateTime.time
                }
            }
        }

        viewModel.content.observe(viewLifecycleOwner, object : Observer<String> {
            var count = 0
            override fun onChanged(it: String?) {
                // TODO("Not yet implemented")
                Log.d(LOG_TAG, "Updating Content Text: $it")
                diaryTextEditor.text = SpannableStringBuilder(it)
                count++

                if(count == 2 || !viewModel.isModification) {
                    // Configure the markdown editor
                    diaryTextEditor.apply {
                        addTextChangedListener(MarkwonEditorTextWatcher.withPreRender(markwonEditor,
                                                                                      Executors.newCachedThreadPool(),
                                                                                      this))
                        doOnTextChanged { text, _, _, _ ->
                            viewModel.content.value = text.toString()
                        }
                        this.text?.let { markwonEditor.process(it) }
                    }
                    viewModel.content.removeObserver(this)
                }
            }
        })

        saveBtn.setOnClickListener {
            MaterialDialog(context!!).show {
                cornerRadius(res = R.dimen.dialog_corner_radius)
                title(res = R.string.dialog_save_the_entry)
                positiveButton(android.R.string.ok) {
                    // TODO: save the data here!!!!
                    if(!viewModel.isModification) {
                        viewModel
                                .saveData()
                                .subscribe { it2, throwable ->
                                    Log.d(LOG_TAG, "Upload Data Completed: $it2")
                                    Toasty.success(context, getString(R.string.status_saved)).show()
                                    activity!!.finish()
                                }
                    } else {
                        viewModel
                                .updateData()
                                .subscribe { data, throwable ->
                                    Log.d(LOG_TAG, "Saved Data Completed: $data")
                                    Toasty.success(context, getString(R.string.status_saved)).show()
                                    activity!!.finish()
                                }
                    }
                }
                negativeButton(android.R.string.cancel)
            }
        }

        // TODO: Clean this up to a resource file
        val titleDialogClickListener = { _: View ->
            MaterialDialog(context!!, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                val prefillText = if(viewModel.title.value.isNullOrEmpty()) "" else viewModel.title.value

                Log.d(LOG_TAG, "Title Dialog Prefill: ${viewModel.title.value}")

                // cornerRadius(res = R.dimen.dialog_corner_radius)
                title(R.string.dialog_edit_entry_title)
                input(waitForPositiveButton = true, prefill = prefillText,
                      hintRes = R.string.entry_title,
                      inputType = InputType.TYPE_TEXT_FLAG_AUTO_CORRECT,
                      maxLength = 30) { _, inputText ->
                    viewModel.title.value = inputText.toString()
                }

                // onCancel { input(prefill = "") }

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    this.getInputField().setTextCursorDrawable(R.drawable.edit_text_cursor)
                }

                positiveButton { R.string.submit }
                negativeButton { R.string.cancel }
            }
        }.also { listener ->
            titleTextView.setOnClickListener { listener(it) }
            titleEditBtn.setOnClickListener { listener(it) }
        }

        // TODO: Clean this up to a resource file
        val subtitleDialogClickListener = { _: View ->
            MaterialDialog(context!!, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                val prefillText = if(viewModel.subtitle.value == null) "" else viewModel.subtitle.value

                // cornerRadius(res = R.dimen.dialog_corner_radius)
                title(R.string.dialog_edit_entry_subtitle)
                input(waitForPositiveButton = true, prefill = prefillText,
                      hintRes = R.string.entry_subtitle,
                      inputType = InputType.TYPE_TEXT_FLAG_AUTO_CORRECT,
                      maxLength = 50) { _, inputText ->
                    viewModel.subtitle.value = inputText.toString()
                }

                // onCancel { input(prefill = "") }

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    this.getInputField().setTextCursorDrawable(R.drawable.edit_text_cursor)
                }

                positiveButton { R.string.submit }
                negativeButton { R.string.cancel }
            }
        }.also { listener ->
            subtitleTextView.setOnClickListener { listener(it) }
            subtitleEditBtn.setOnClickListener { listener(it) }
        }

        val colorDialogClickListener = { _: View ->
            MaterialDialog(context!!).show {
                val colors = intArrayOf(R.color.AmberA400,
                                        R.color.BlueA400,
                                        R.color.CyanA400,
                                        R.color.GreenA400,
                                        R.color.Cyan400,
                                        R.color.Pink400,
                                        R.color.Brown400,
                                        R.color.Grey400,
                                        R.color.BlueGrey400,
                                        R.color.RedA400).map {
                    context.getColor(it)
                }.toIntArray() // size = 3

                title(R.string.dialog_edit_entry_color)

                colorChooser(colors, allowCustomArgb = true,
                             initialSelection = viewModel.color.value,
                             selection = { dialog, color ->
                                 viewModel.color.value = color
                             })
                positiveButton(R.string.submit)
            }
        }.also { listener -> colorSelectBtn.setOnClickListener { listener(it) } }

        // overlayImageView.setOnClickListener { invokeImageSelectionIntent() }

        bottomSheetView.apply {
            val btmSheetBehavior = BottomSheetBehavior.from(this)
            locationBtn.setOnClickListener {
                if(requestPermissions(CODE_PERMISSION_FINE_LOCATION)) {
                    btmSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                    initializeLocationAndMap()
                }
            }

            collapseImageBtn.setOnClickListener {
                if(btmSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                    btmSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                } else {
                    btmSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            }

            btmSheetBehavior.addBottomSheetCallback(object :
                                                            BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, slideOffsetPercent: Float) {
                    // TODO("Not yet implemented")
                    ViewCompat.animate(collapseImageBtn)
                            .rotation(-slideOffsetPercent * 180f)
                            .withLayer()
                            .setDuration(0L)
                            // .setInterpolator(OvershootInterpolator(10.0F))
                            .start()

                    ViewCompat.animate(saveBtn)
                            .translationY(
                                    -slideOffsetPercent * (bottomSheet.measuredHeight * 0.96f))
                            .withLayer()
                            .translationX(slideOffsetPercent * (bottomSheet.measuredHeight * 0.2f))
                            .alpha(1 - slideOffsetPercent)
                            .setDuration(0L)
                            .start()

                    ViewCompat.animate(locationBtn)
                            .alpha(1 - slideOffsetPercent)
                            .withLayer()
                            .withEndAction {
                                if(locationBtn.alpha == 0f) {
                                    locationBtn.visibility = View.GONE
                                } else {
                                    locationBtn.visibility = View.VISIBLE
                                }
                            }
                            .setDuration(0L)
                            .start()

                    ViewCompat.animate(frameLayout)
                            .alpha(slideOffsetPercent)
                            .withLayer()
                            .setDuration(0L)
                            .start()
                }

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if(newState == BottomSheetBehavior.STATE_EXPANDED) {
                        diaryTextEditor.clearFocus()
                        bottomSheet.requestFocus()
                        nestedScrollView.isNestedScrollingEnabled = false
                    } else {
                        nestedScrollView.isNestedScrollingEnabled = true
                    }
                }

            })
        }

        tagBtn.setOnClickListener {
            MaterialDialog(context!!, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                // cornerRadius(res = R.dimen.dialog_corner_radius)
                title(R.string.dialog_add_entry_tag)
                input(waitForPositiveButton = true,
                      hintRes = R.string.entry_tag,
                      inputType = InputType.TYPE_TEXT_FLAG_AUTO_CORRECT,
                      maxLength = 20) { _, inputText ->
                    inputText.toString().trim().also {
                        viewModel.addDiaryTag(it)
                    }
                }

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    this.getInputField().setTextCursorDrawable(R.drawable.edit_text_cursor)
                }

                cancelOnTouchOutside(false)
                negativeButton { R.string.cancel }
            }
        }

        val chipRvAdapter = ChipRecyclerAdapter(context!!) { _, text ->
            viewModel.removeDiaryTag(text)
        }.apply {
            tagEditContainer.adapter = this
            tagEditContainer.setHasFixedSize(false)

            val chipsLayoutManager = ChipsLayoutManager
                    .newBuilder(context!!)
                    // .setChildGravity(Gravity.TOP)
                    .setScrollingEnabled(false)
                    .setGravityResolver { Gravity.CENTER }
                    .setOrientation(ChipsLayoutManager.HORIZONTAL)
                    .build()

            tagEditContainer.layoutManager = chipsLayoutManager
            tagEditContainer.addItemDecoration(
                    MarginItemDecoration(context!!, RecyclerView.HORIZONTAL))
        }

        viewModel.tagArray.observe(viewLifecycleOwner, object : Observer<TreeSet<DiaryTag>> {
            override fun onChanged(it: TreeSet<DiaryTag>?) {
                if(it == null) {
                    return
                }
                chipRvAdapter.submitList(
                        it.toList()) { viewModel.color.value = viewModel.color.value }
            }
        })

        viewModel.color.observe(viewLifecycleOwner) { color ->
            if(color == null) {
                return@observe
            }

            chipRvAdapter.setChipBackgroundColor = color
            tagEditContainer.children.iterator().forEach {
                (it as Chip).changeBackgroundColor(color)
            }

            saveBtn.changeBackgroundColor(color)
        }

        goodBadBtnGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if(!isChecked) {
                // Prevents the an unexpected behavior where buttons will be wrongly selected
                // When a button is selected, the button group will
                // 1. clear all selection
                // 2. Assign a selection to a button
                // These 2 process will invoke addOnButtonCheckedListenerMethod -> This is the cause of the problem!
                return@addOnButtonCheckedListener
            }
            viewModel.goodBad.value = when(checkedId) {
                R.id.goodBtn    -> 1
                R.id.badBtn     -> -1
                R.id.neutralBtn -> 0
                else            -> throw IllegalStateException("Wrong button selected!")
            }
        }

        viewModel.goodBad.observe(viewLifecycleOwner, object : Observer<Int> {
            var count = 0
            override fun onChanged(goodBad: Int) {
                goodBadStatusText.text = when {
                    goodBad >= 1 -> {
                        getString(R.string.goodbad_good)
                    }
                    goodBad < 0  -> {
                        getString(R.string.goodbad_bad)
                    }
                    else         -> {
                        getString(R.string.goodbad_neutral)
                    }
                }

                count++
                // For entry editor initialization
                if(count == 2) {
                    Log.d(LOG_TAG, "Updating GoodBad Score to $goodBad")
                    val btn = when {
                        goodBad >= 1 -> {
                            R.id.goodBtn
                        }
                        goodBad < 0  -> {
                            R.id.badBtn
                        }
                        else         -> {
                            R.id.neutralBtn
                        }
                    }
                    goodBadBtnGroup.check(btn)
                }
            }
        })

        imageBtn.setOnClickListener {
            if(requestPermissions(CODE_PERMISSION_READ_STORAGE)) {
                invokeImageSelectionIntent()
            }
        }

        if(activity!!.isGrantedFineLocationPermission()) {
            initializeLocationAndMap()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestPermissions(permissionCode: Int): Boolean {
        if(permissionCode == CODE_PERMISSION_READ_STORAGE) {
            if(!activity!!.isGrantedExternalStoragePermission()) {
                Toasty.error(context!!, getString(R.string.permission_request_storage)).show()
                ActivityCompat.requestPermissions(activity!!,
                                                  arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                                  CODE_PERMISSION_READ_STORAGE)
            } else {
                return true
            }
        } else if(permissionCode == CODE_PERMISSION_FINE_LOCATION) {
            // Permission is not granted, Should we show an explanation?
            if(!activity!!.isGrantedFineLocationPermission()) {
                Toasty.error(context!!, getString(R.string.permission_request_location)).show()
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity!!,
                                                  arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                                  CODE_PERMISSION_FINE_LOCATION)
            } else {
                return true
            }
        } else {
            return false
        }
        return false
    }



    @SuppressLint("MissingPermission")
    private fun initializeLocationAndMap() {

        val locationManager: LocationManager = activity!!.getSystemService(
                Context.LOCATION_SERVICE) as LocationManager

        val locationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location?) {

                progressMap?.visibility = View.GONE
                locationEditBtn?.isEnabled = true
                locationClearBtn?.isEnabled = true

                if(location == null || viewModel.coordinate.value != EMPTY_LOCATION) {
                    return
                }

                if(viewModel.isModification && viewModel.coordinate.value != EMPTY_LOCATION) {
                    return
                }

                try {
                    val initCoordinate = Pair(location.latitude, location.longitude)
                    viewModel.SESSION_DEFAULT_LOCATION = initCoordinate
                    viewModel.coordinate.value = initCoordinate
                } catch(e: IOException) {
                    e.printStackTrace()
                } finally {
                    locationManager.removeUpdates(this)
                }
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                // TODO("Not yet implemented")
            }

            @SuppressLint("MissingPermission")
            override fun onProviderEnabled(provider: String?) {
                Toasty.info(context!!, getString(R.string.gps_enabled)).show()

                if(viewModel.coordinate.value == EMPTY_LOCATION) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300L, 0f,
                                                           this)
                    progressMap?.visibility = View.VISIBLE
                    locationEditBtn?.isEnabled = false
                    locationClearBtn?.isEnabled = false
                }
            }

            override fun onProviderDisabled(provider: String?) {
                Toasty.error(context!!, getString(R.string.gps_disabled)).show()
                progressMap?.visibility = View.GONE
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
     *
     * @param g GoogleMap an instance of Google Map
     */
    override fun onMapReady(g: GoogleMap) {
        g.apply {
            setMinZoomPreference(15f)
            uiSettings.setAllGesturesEnabled(false)
        }

        // Migrate the click to somewhere else
        locationEditBtn.setOnClickListener {
            if(activity!!.isGrantedFineLocationPermission()) {
                val lat = viewModel.coordinate.value!!.first
                val long = viewModel.coordinate.value!!.second

                val action = EntryEditFragmentDirections.actionEntryEditFragmentToLocationActivity(
                        lat.toFloat(), long.toFloat()).arguments
                Log.d(LOG_TAG, "Sent Location Bundle: $lat, $long")

                val intent = Intent(activity!!, LocationActivity::class.java).putExtras(action)
                startActivityForResult(intent, REQUEST_CODE_LOCATION_PICKER)
            }
        }

        viewModel.coordinate.observe(viewLifecycleOwner) {
            if(it == EMPTY_LOCATION){
                locationClearBtn.isEnabled = false
                locationNameTextView.text = getString(R.string.location_not_selected)
                locationCoordinateTextView.text = ""
            }

            locationClearBtn.isEnabled = true

            Log.d(LOG_TAG, "Updating the location to (${it.first}, ${it.second})")
            val position = LatLng(it.first, it.second)
            g.clear()
            g.addMarker(MarkerOptions().position(position))
            g.moveCamera(CameraUpdateFactory.newLatLng(position))

            val areaName = context!!.getAreaNameByCoordinate(it)
            locationNameTextView.text = areaName.first
            locationCoordinateTextView.text = areaName.second
        }

        locationClearBtn.setOnClickListener {
            viewModel.coordinate.value = viewModel.SESSION_DEFAULT_LOCATION
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_entry_edit, container, false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                REQUEST_CODE_GALLERY         -> {
                    //data.getData return the content URI for the selected Image
                    val selectedImage: Uri = data?.data ?: return

                    viewModel.addAnImage(selectedImage)
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode) {
            CODE_PERMISSION_READ_STORAGE  -> {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toasty.success(context!!, getString(R.string.permission_thanks_storage)).show()
                    invokeImageSelectionIntent()
                }
            }
            CODE_PERMISSION_FINE_LOCATION -> {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toasty.success(context!!, getString(R.string.permission_thanks_location)).show()
                    initializeLocationAndMap()
                }
            }
        }
    }
}

/**
 * A RecyclerView adapter for Chips
 * @property onChipClickListener Function2<Chip, String, Unit> A listener specifying the behavior when a chip is clicked
 * @property setChipBackgroundColor Int the initial color of all Chips
 * @constructor default constructor
 */
class ChipRecyclerAdapter(context: Context,
                          private val onChipClickListener: (Chip, String) -> Unit) :
        ListAdapter<DiaryTag, ChipRecyclerAdapter.ViewHolder>(ChipDiffCallback()) {

    var setChipBackgroundColor: Int = context.getColor(R.color.colorPrimary)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val rootView = inflater.inflate(R.layout.chip_tag_edit, parent, false)
        return ViewHolder(rootView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.chip.apply {
            text = getItem(position).title
            changeBackgroundColor(setChipBackgroundColor)
            setOnCloseIconClickListener {
                onChipClickListener(this, text.toString())
            }
        }
    }

    class ViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        val chip: Chip = root.findViewById(R.id.chip)
    }

    internal class ChipDiffCallback : DiffUtil.ItemCallback<DiaryTag>() {

        override fun areItemsTheSame(oldItem: DiaryTag, newItem: DiaryTag): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: DiaryTag, newItem: DiaryTag): Boolean {
            return oldItem.title == newItem.title
        }

    }
}