package com.anvipus.explore.ui.xml.scanqr

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.anvipus.core.R
import com.anvipus.core.utils.*
import com.anvipus.explore.base.BaseFragment
import com.anvipus.explore.databinding.ScanQrCameraFragmentBinding
import com.anvipus.explore.databinding.ScanQrCameraZxingFragmentBinding
import com.anvipus.explore.ui.xml.MainViewModel
import com.codedisruptors.dabestofme.di.Injectable
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class ScanQrCameraZxingFragment : BaseFragment(), DecoratedBarcodeView.TorchListener, BarcodeCallback, Injectable {

    companion object {
        fun newInstance() = ScanQrCameraZxingFragment()

        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0

        const val REQUEST_QR = 555
    }

    override val layoutResource: Int
        get() = com.anvipus.explore.R.layout.scan_qr_camera_zxing_fragment

    override val statusBarColor: Int
        get() = R.color.colorAccent

    override val showToolbar: Boolean
        get() = true

    override val headTitle: Int
        get() = com.anvipus.explore.R.string.title_toolbar_zxing_screen

    private lateinit var binding: ScanQrCameraZxingFragmentBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModelMain: MainViewModel by activityViewModels { viewModelFactory }

    private var capture: CaptureManager? = null

    private var jsonObject: JSONObject? = null

    override fun initView(view: View) {
        super.initView(view)
        binding = ScanQrCameraZxingFragmentBinding.bind(view)
        capture = CaptureManager(requireActivity(), binding.scanCodeView)
        binding.scanCodeView.decodeSingle(this)
        initActivityResultLauncher()
    }

    private fun initActivityResultLauncher() {
        activityResultLauncher = openQRImageFromGallery(object : ActivityResultListener {
            override fun onSuccessGetResult(data: Intent?) {
                data?.data?.run {
                    val bitmap = getBitmap(requireContext(), this)
                    val scanner: BarcodeScanner = BarcodeScanning.getClient()

                    scanner.process(bitmap, 0).addOnSuccessListener { barcodes ->
                        barcodes.getOrNull(0)?.rawValue.orEmpty().let { barcode ->
                            if (barcode.isNotBlank()) {
                                processQRImage(barcode)
                            }
                        }
                    }
                }
            }

            override fun onCancel() {}
            override fun onForgotPin() {}
        })
    }

    override fun setupListener() {
        super.setupListener()
        with(binding){
            layoutBottom.btnGallery.setOnClickListener {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
                val pickIntent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                    type = "image/*"
                }
                val chooserIntent = Intent.createChooser(intent, "Select image").apply {
                    putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))
                }
                activityResultLauncher.launch(chooserIntent)
            }
            btnFlash.apply {
                enableIf(hasFlash())
                setOnClickListener {
                    it.isSelected = !it.isSelected
                    switchFlash()
                }
            }
            scanCodeView.setTorchListener(this@ScanQrCameraZxingFragment)
        }
    }

    private fun switchFlash() {
        with(binding) {
            if (btnFlash.isSelected) {
                scanCodeView.setTorchOn()
                btnFlash.setImageDrawable(resDrawable(R.drawable.ic_flashon))
            } else {
                scanCodeView.setTorchOff()
                btnFlash.setImageDrawable(resDrawable(R.drawable.ic_flashoff))
            }
        }
    }

    private fun hasFlash(): Boolean = requireContext()
        .packageManager
        .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)

    private fun processQRImage(code: String) {
        Timber.d("qrisResult: $code")
        if (isCodeJson(code)) {
            if (jsonObject != null) {
                Timber.e("resultData: $code")
                navController().previousBackStackEntry?.savedStateHandle?.set(Constants.EXTRA_QR, code)
                if (isFragmentInBackStack(com.anvipus.explore.R.id.fragment_scan_qr)) {
                    navController().popBackStack(com.anvipus.explore.R.id.fragment_scan_qr, false)
                } else {
                    navController().popBackStack()
                }
            } else {
                Toast.makeText(requireContext(), getString(com.anvipus.explore.R.string.error_scan_from_gallery), Toast.LENGTH_LONG).show()
            }
        } else {
            navController().previousBackStackEntry?.savedStateHandle?.set(Constants.EXTRA_QR, code)
            if (isFragmentInBackStack(com.anvipus.explore.R.id.fragment_scan_qr)) {
                navController().popBackStack(com.anvipus.explore.R.id.fragment_scan_qr, false)
            } else {
                navController().popBackStack()
            }
        }
    }

    private fun isCodeJson(code: String): Boolean {
        return try {
            jsonObject = JSONObject(code)
            true
        } catch (e: JSONException) {
            false
        }
    }

    override fun onTorchOn() {

    }

    override fun onTorchOff() {

    }

    override fun barcodeResult(p0: BarcodeResult?) {
        processQRImage(p0?.text.toString())
    }

    override fun possibleResultPoints(p0: MutableList<ResultPoint>?) {

    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        capture?.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        capture?.onResume()
    }

    override fun onPause() {
        super.onPause()
        capture?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        capture?.onDestroy()
    }

}