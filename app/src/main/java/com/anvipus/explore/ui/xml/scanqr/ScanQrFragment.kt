package com.anvipus.explore.ui.xml.scanqr

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.anvipus.core.R
import com.anvipus.core.utils.Constants
import com.anvipus.explore.base.BaseFragment
import com.anvipus.explore.databinding.MainFragmentBinding
import com.anvipus.explore.databinding.ScanQrFragmentBinding
import com.anvipus.explore.ui.xml.MainFragment
import com.anvipus.explore.ui.xml.MainFragmentDirections
import com.anvipus.explore.ui.xml.MainViewModel
import com.codedisruptors.dabestofme.di.Injectable
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import javax.inject.Inject

class ScanQrFragment : BaseFragment(), Injectable {

    companion object {
        fun newInstance() = ScanQrFragment()
        private const val PERMISSION_REQUEST = 101
    }

    override val layoutResource: Int
        get() = com.anvipus.explore.R.layout.scan_qr_fragment

    override val statusBarColor: Int
        get() = R.color.colorAccent

    override val showToolbar: Boolean
        get() = true

    override val headTitle: Int
        get() = com.anvipus.explore.R.string.title_toolbar_main_screen

    private lateinit var binding: ScanQrFragmentBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModelMain: MainViewModel by activityViewModels { viewModelFactory }

    override fun initView(view: View) {
        super.initView(view)
        binding = ScanQrFragmentBinding.bind(view)
    }

    override fun setupListener() {
        super.setupListener()
        with(binding){
            btnOpenScanQrCamera.setOnClickListener {
                val cameraPermission = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED

                if (cameraPermission) {
                    checkCameraPermission()
                } else {
                    navigate(ScanQrFragmentDirections.actionToScanQrCamera())
                }
            }
            btnOpenScanQrCameraZxing.setOnClickListener {
                val cameraPermission = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED

                if (cameraPermission) {
                    checkCameraPermission2()
                } else {
                    navigate(ScanQrFragmentDirections.actionToScanQrCameraZxing())
                }
            }
            btnOpenScanQrCameraCompose.setOnClickListener {
                val cameraPermission = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED

                if (cameraPermission) {
                    checkCameraPermission3()
                } else {
                    navigate(ScanQrFragmentDirections.actionToScanQrCompose())
                }
            }
        }
    }

    override fun initObserver() {
        super.initObserver()
        navController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>(Constants.EXTRA_QR)
            ?.observe(viewLifecycleOwner) {
                if(it.isNullOrEmpty().not()){
                    with(binding){
                        tvContent.text = it.toString()
                    }
                }
            }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        checkIfCameraPermissionIsGranted()
    }

    private fun checkCameraPermission() {
        try {
            val requiredPermissions = arrayOf(Manifest.permission.CAMERA)
            ActivityCompat.requestPermissions(requireActivity(), requiredPermissions, 0)
            val tes1 = requiredPermissions
            val tes2 = requiredPermissions
        } catch (e: IllegalArgumentException) {
            checkIfCameraPermissionIsGranted()
        }
    }

    private fun checkCameraPermission2() {
        try {
            val requiredPermissions = arrayOf(Manifest.permission.CAMERA)
            ActivityCompat.requestPermissions(requireActivity(), requiredPermissions, 0)
            val tes1 = requiredPermissions
            val tes2 = requiredPermissions
        } catch (e: IllegalArgumentException) {
            checkIfCameraPermissionIsGranted2()
        }
    }

    private fun checkCameraPermission3() {
        try {
            val requiredPermissions = arrayOf(Manifest.permission.CAMERA)
            ActivityCompat.requestPermissions(requireActivity(), requiredPermissions, 0)
            val tes1 = requiredPermissions
            val tes2 = requiredPermissions
        } catch (e: IllegalArgumentException) {
            checkIfCameraPermissionIsGranted3()
        }
    }

    private fun checkIfCameraPermissionIsGranted() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Permission granted: start the preview
            navigate(ScanQrFragmentDirections.actionToScanQrCamera())
        } else {
            // Permission denied
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Permission required")
                .setMessage("This application needs to access the camera to process barcodes")
                .setPositiveButton("Ok") { _, _ ->
                    // Keep asking for permission until granted
                    checkCameraPermission()
                }
                .setCancelable(false)
                .create()
                .apply {
                    setCanceledOnTouchOutside(false)
                    show()
                }
        }
    }

    private fun checkIfCameraPermissionIsGranted2() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Permission granted: start the preview
            navigate(ScanQrFragmentDirections.actionToScanQrCamera())
        } else {
            // Permission denied
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Permission required")
                .setMessage("This application needs to access the camera to process barcodes")
                .setPositiveButton("Ok") { _, _ ->
                    // Keep asking for permission until granted
                    checkCameraPermission()
                }
                .setCancelable(false)
                .create()
                .apply {
                    setCanceledOnTouchOutside(false)
                    show()
                }
        }
    }

    private fun checkIfCameraPermissionIsGranted3() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Permission granted: start the preview
            navigate(ScanQrFragmentDirections.actionToScanQrCompose())
        } else {
            // Permission denied
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Permission required")
                .setMessage("This application needs to access the camera to process barcodes")
                .setPositiveButton("Ok") { _, _ ->
                    // Keep asking for permission until granted
                    checkCameraPermission()
                }
                .setCancelable(false)
                .create()
                .apply {
                    setCanceledOnTouchOutside(false)
                    show()
                }
        }
    }

}