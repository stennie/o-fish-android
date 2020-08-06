package org.wildaid.ofish.ui.basicinformation

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.fragment.app.viewModels
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.wildaid.ofish.EventObserver
import org.wildaid.ofish.R
import org.wildaid.ofish.data.mpa.addTestMpa
import org.wildaid.ofish.databinding.FragmentBasicInformationBinding
import org.wildaid.ofish.ui.base.BaseReportFragment
import org.wildaid.ofish.util.getViewModelFactory
import org.wildaid.ofish.util.hideKeyboard
import java.util.*

class BasicInformationFragment : BaseReportFragment(R.layout.fragment_basic_information),
    OnMapReadyCallback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var viewDataBinding: FragmentBasicInformationBinding

    private lateinit var map: GoogleMap
    private lateinit var marker: Marker

    private val fragmentViewModel: BasicInformationViewModel by viewModels { getViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentViewModel.initReport(currentReport)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        subscribeToNavigationResult()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewDataBinding = FragmentBasicInformationBinding.bind(view).apply {
            this.viewmodel = fragmentViewModel
            this.lifecycleOwner = this@BasicInformationFragment.viewLifecycleOwner
        }

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.basic_info_map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        fragmentViewModel.buttonId.observe(
            viewLifecycleOwner, EventObserver(::onButtonClicked)
        )
    }

    @SuppressLint("MissingPermission")
    private fun initMap(location: Location) {
        val coord = LatLng(location.latitude, location.longitude)
        if (!::marker.isInitialized) {
            marker = map.addMarker(
                MarkerOptions()
                    .position(coord)
            )
        }
        map.isMyLocationEnabled = true
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(coord, 10f))
    }

    private fun subscribeToNavigationResult() {
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap) {
        this.map = map

        this.map.setOnCameraMoveListener { updateMarker() }

        fusedLocationClient.lastLocation.addOnSuccessListener {
            it?.let {
                fragmentViewModel.setLocation(it.latitude, it.longitude)
                initMap(it)
            }
        }

        addTestMpa(this.map, resources)
    }

    private fun updateMarker() {
        val target = map.cameraPosition.target
        fragmentViewModel.setLocation(target.latitude, target.longitude)
        marker.position = target
    }

    private fun onButtonClicked(buttonId: Int) {
        hideKeyboard()
        when (buttonId) {
            R.id.btn_next -> {
                onNextListener.onNextClicked()
            }
            R.id.basic_info_date -> {
                peekDate()
            }
            R.id.basic_info_time -> {
                peekTime()
            }
        }
    }

    private fun peekDate() {
        val calendar: Calendar = Calendar.getInstance(TimeZone.getDefault())
        val dialog = DatePickerDialog(
            requireContext(), ::onDatePicked,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        dialog.show()
    }

    private fun onDatePicked(datePicker: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        fragmentViewModel.updateDate(year, month, dayOfMonth)
    }

    private fun peekTime() {
        val calendar: Calendar = Calendar.getInstance(TimeZone.getDefault())
        val dialog = TimePickerDialog(
            requireContext(),
            ::onTimePicked,
            calendar.get(Calendar.HOUR),
            calendar.get(Calendar.MINUTE),
            false
        )
        dialog.show()
    }

    private fun onTimePicked(timePicker: TimePicker, hourOfDay: Int, minute: Int) {
        fragmentViewModel.updateTime(hourOfDay, minute)
    }
}