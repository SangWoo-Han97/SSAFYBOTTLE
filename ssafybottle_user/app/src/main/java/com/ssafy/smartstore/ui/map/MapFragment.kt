package com.ssafy.smartstore.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import com.ssafy.smartstore.R
import com.ssafy.smartstore.databinding.FragmentMapBinding
import com.ssafy.smartstore.utils.*
import java.io.IOException
import java.util.*


class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private var map: GoogleMap? = null
    private var storeMarker: Marker? = null
    private var currentMarker: Marker? = null
    private lateinit var currentPosition: LatLng

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private var needRequest = false // ?????? ????????? ??????

    private var myLocation = Location("MyLocation")
    private var storeLocation = Location("StoreLocation").apply {
        latitude = DEFAULT_LATITUDE; longitude = DEFAULT_LONGITUDE
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ?????? ?????? ?????? ??? location ?????????
        init()
        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        binding.imgMapBack.setOnClickListener { requireActivity().onBackPressed() }
    }

    override fun onMapReady(p0: GoogleMap) {
        map = p0

        // ?????? ?????? ??????
        setDefaultLocation()

        // ??? ?????? ??????
        setMyLocation()

        // ?????? ?????? ????????? ??????
        map!!.setOnInfoWindowClickListener(this)
    }

    override fun onInfoWindowClick(marker: Marker) {
        if (marker == storeMarker) {
            findNavController().navigate(
                R.id.action_mapFragment_to_storeFragment,
                bundleOf(DISTANCE to storeLocation.distanceTo(myLocation))
            )
        }
    }

    // ?????????
    private fun init() {
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = UPDATE_INTERVAL.toLong()
            smallestDisplacement = 10.0f
            fastestInterval = FASTEST_UPDATE_INTERVAL.toLong()
        }

        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.fragmentcontainer_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setDefaultLocation() {
        val latLng = LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE)

        storeMarker?.remove()
        storeMarker =
            drawMarker(LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE), "????????????", "010-1234-5678")
        moveCamera(latLng)
    }


    private fun setMyLocation() {
        if (checkPermissions(
                listOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        ) {
            startLocationUpdates()
        } else {
            // ????????? ????????? ?????? ??????
            requestPermission {
                // ?????? ?????? ????????? ??? ?????? ????????????
                startLocationUpdates()
            }
        }
    }

    private fun requestPermission(logic: () -> Unit) {
        TedPermission.create()
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    logic()
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    Toast.makeText(requireContext(), "?????? ????????? ?????????????????????.", Toast.LENGTH_SHORT).show()
                }
            })
            .setDeniedMessage("[??????] ?????? ?????? ?????? ????????? ??????????????? ????????? ???????????????.")
            .setPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .check()
    }

    // ?????? ?????????
    private fun drawMarker(latLng: LatLng, markerTitle: String?, markerSnippet: String?): Marker? {
        val markerOptions = MarkerOptions().apply {
            position(latLng)
            title(markerTitle)
            snippet(markerSnippet)
            draggable(true)
            icon(
                BitmapDescriptorFactory.fromBitmap(
                    Bitmap.createScaledBitmap(
                        (ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_marker,
                            requireActivity().theme
                        ) as BitmapDrawable).bitmap,
                        100, 150, false
                    )
                )
            )
        }
        return map!!.addMarker(markerOptions)
    }

    // ?????? ????????? ????????????
    private fun moveCamera(latLng: LatLng) {
        map!!.animateCamera(
            CameraUpdateFactory.newLatLngZoom(latLng, 15f)
        )
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)

            locationResult.locations.let {
                // ?????? ????????? ?????????
                if (it.size > 0) {
                    val location = it[it.size - 1]
                    currentPosition = LatLng(location.latitude, location.longitude)
                    var markerTitle = "??? ??????"
                    var markerSnippet = getCurrentAddress(currentPosition)

                    myLocation.apply {
                        latitude = location.latitude; longitude = location.longitude
                    }

                    currentMarker = drawMarker(currentPosition, markerTitle, markerSnippet)
                    moveCamera(currentPosition)
                }
            }
        }
    }

    // ??? ?????? ????????????
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        // ??????????????? ????????? ?????? check
        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting()
        } else {
            if (checkPermissions(
                    listOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            ) {
                mFusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.myLooper()!!
                )

                map?.let {
                    it.isMyLocationEnabled = true   // ??? ?????? ??????
                    it.uiSettings.isZoomControlsEnabled = true  // +, -??????
                }
            }
        }
    }

    // ??????, ????????? ????????? ??????
    fun getCurrentAddress(latLng: LatLng): String {
        //????????????: GPS??? ????????? ??????
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses: List<Address>?
        try {
            addresses = geocoder.getFromLocation(
                latLng.latitude,
                latLng.longitude,
                1
            )
        } catch (ioException: IOException) {
            //???????????? ??????
            Toast.makeText(requireContext(), "???????????? ????????? ????????????", Toast.LENGTH_LONG).show()
            return "???????????? ????????????"
        } catch (illegalArgumentException: IllegalArgumentException) {
            Toast.makeText(requireContext(), "????????? GPS ??????", Toast.LENGTH_LONG).show()
            return "????????? GPS ??????"
        }

        return if (addresses == null || addresses.isEmpty()) {
            Toast.makeText(requireContext(), "?????? ?????? ??????", Toast.LENGTH_LONG).show()
            "?????? ?????? ??????"
        } else {
            val address = addresses[0]
            address.getAddressLine(0).toString()
        }
    }

    // ?????? ????????? ????????? ?????? ???????????????
    private fun showDialogForLocationServiceSetting() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("?????? ????????? ????????????")
            setMessage("?????? ???????????? ???????????? ?????? ???????????? ???????????????.\n")
            setCancelable(true)
            setPositiveButton("??????") { _, _ ->
                val callGPSSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                gpsSettingIntentLauncher.launch(callGPSSettingIntent)
            }
            setNegativeButton("??????") { dialog, _ -> dialog.cancel() }
            create()
            show()
        }
    }

    // ?????? ????????? ?????????
    private val gpsSettingIntentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // ?????? ????????? ????????? ?????? ??????
            if (checkLocationServicesStatus()) {
                needRequest = true
                startLocationUpdates()
            } else {
                Toast.makeText(
                    requireContext(),
                    "?????? ???????????? ?????? ??????, ?????? ????????? ????????? ??? ????????????.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}