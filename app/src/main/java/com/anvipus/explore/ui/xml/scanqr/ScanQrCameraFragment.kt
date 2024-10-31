package com.anvipus.explore.ui.xml.scanqr

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDeepLinkBuilder
import com.anvipus.core.R
import com.anvipus.core.utils.*
import com.anvipus.explore.base.BaseFragment
import com.anvipus.explore.databinding.MainFragmentBinding
import com.anvipus.explore.databinding.ScanQrCameraFragmentBinding
import com.anvipus.explore.databinding.ScanQrFragmentBinding
import com.anvipus.explore.ui.xml.MainFragmentDirections
import com.anvipus.explore.ui.xml.MainViewModel
import com.codedisruptors.dabestofme.di.Injectable
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class ScanQrCameraFragment : BaseFragment(), Injectable {

    companion object {
        fun newInstance() = ScanQrCameraFragment()

        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0

        const val REQUEST_QR = 555
    }

    override val layoutResource: Int
        get() = com.anvipus.explore.R.layout.scan_qr_camera_fragment

    override val statusBarColor: Int
        get() = R.color.colorAccent

    override val showToolbar: Boolean
        get() = true

    override val headTitle: Int
        get() = com.anvipus.explore.R.string.title_toolbar_mlkit_screen

    private lateinit var binding: ScanQrCameraFragmentBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModelMain: MainViewModel by activityViewModels { viewModelFactory }

    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraSelector: CameraSelector? = null

    private var previewUseCase: Preview? = null
    private var analysisUseCase: ImageAnalysis? = null

    private var jsonObject: JSONObject? = null
    private var camera: Camera? = null

    @Suppress("DEPRECATION")
    private val screenAspectRatio: Int
        get() {
            // Get screen metrics used to setup camera for full screen resolution
            val metrics = DisplayMetrics().also { binding.scanCodeView.display?.getRealMetrics(it) }
            return aspectRatio(metrics.widthPixels, metrics.heightPixels)
        }

    override fun initView(view: View) {
        super.initView(view)
        binding = ScanQrCameraFragmentBinding.bind(view)
        cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
        startCamera()
    }

    private val activityResultLauncher = openQRImageFromGallery(object : ActivityResultListener {
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
        }
    }

    private fun switchFlash() {
        with(binding) {
            if (btnFlash.isSelected) {
                camera?.cameraControl?.enableTorch(true)
                btnFlash.setImageDrawable(resDrawable(R.drawable.ic_flashon))
            } else {
                camera?.cameraControl?.enableTorch(false)
                btnFlash.setImageDrawable(resDrawable(R.drawable.ic_flashoff))
            }
        }
    }

    private fun hasFlash(): Boolean {
        return requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }

    /*override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return binding.scanCodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }*/

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) return AspectRatio.RATIO_4_3

        return AspectRatio.RATIO_16_9
    }

    private fun startCamera() {
        ProcessCameraProvider.getInstance(requireContext()).let { processProvider ->
            processProvider.addListener(
                {
                    cameraProvider = processProvider.get()
                    bindPreviewUseCase()
                    bindAnalyseUseCase()
                },
                ContextCompat.getMainExecutor(requireContext())
            )
        }
    }

    private fun bindPreviewUseCase() {
        if (cameraProvider == null) return
        if (previewUseCase != null) cameraProvider?.unbind(previewUseCase)

        previewUseCase = Preview.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(binding.scanCodeView.display.rotation)
            .build()

        previewUseCase?.setSurfaceProvider(binding.scanCodeView.surfaceProvider)

        try {
            cameraSelector?.let { camera = cameraProvider?.bindToLifecycle(this, it, previewUseCase) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun bindAnalyseUseCase() {
        val barcodeScanner: BarcodeScanner = BarcodeScanning.getClient()

        if (cameraProvider == null) return
        if (analysisUseCase != null) cameraProvider?.unbind(analysisUseCase)

        analysisUseCase = ImageAnalysis.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(binding.scanCodeView.display.rotation)
            .build()

        // Initialize our background executor
        val cameraExecutor = Executors.newSingleThreadExecutor()

        analysisUseCase?.setAnalyzer(cameraExecutor) { imageProxy ->
            processImageProxy(barcodeScanner, imageProxy)
        }

        try {
            cameraSelector?.let { cameraProvider?.bindToLifecycle(this, it, analysisUseCase) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processImageProxy(scanner: BarcodeScanner, proxy: ImageProxy) {
        val img = proxy.image

        if (img != null) {
            val inputImage = InputImage.fromMediaImage(img, proxy.imageInfo.rotationDegrees)

            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    barcodes.getOrNull(0)?.rawValue.orEmpty().let { barcode ->
                        if (barcode.isNotBlank()) {
                            processQRImage(barcode)

                            scanner.close()
                            cameraProvider?.unbindAll()
                        }
                    }
                }.addOnCompleteListener {
                    proxy.close()
                }
        }
    }

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

}