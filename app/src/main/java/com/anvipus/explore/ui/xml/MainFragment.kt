package com.anvipus.explore.ui.xml

import android.Manifest
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.anvipus.core.R
import com.anvipus.explore.base.BaseFragment
import com.anvipus.explore.databinding.MainFragmentBinding
import com.anvipus.explore.ui.xml.scanqr.ScanQrFragmentDirections
import com.bumptech.glide.Glide
import com.codedisruptors.dabestofme.di.Injectable
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import javax.inject.Inject

class MainFragment : BaseFragment(), Injectable {

    companion object {
        fun newInstance() = MainFragment()
    }

    override val layoutResource: Int
        get() = com.anvipus.explore.R.layout.main_fragment

    override val statusBarColor: Int
        get() = R.color.colorAccent

    override val showToolbar: Boolean get() = true

    override val headTitle: Int
        get() = com.anvipus.explore.R.string.title_toolbar_main_screen

    private lateinit var binding: MainFragmentBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModelMain: MainViewModel by activityViewModels { viewModelFactory }

    override fun initView(view: View) {
        super.initView(view)
        binding = MainFragmentBinding.bind(view)
        ownIcon(null)
    }

    override fun setupListener() {
        super.setupListener()
        with(binding){
            btnScanQr.setOnClickListener {
                navigate(MainFragmentDirections.actionToScanQr())
            }
        }
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

    private fun checkIfCameraPermissionIsGranted() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Permission granted: start the preview
            navigate(MainFragmentDirections.actionToScanQrCompose())
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