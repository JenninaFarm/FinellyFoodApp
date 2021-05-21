package fi.tuni.tamk.bottom.ui.scanBarcode

import android.Manifest
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.mlkit.common.MlKitException
import fi.tuni.tamk.bottom.R
import fi.tuni.tamk.test2.BarcodeScannerProcessor
import fi.tuni.tamk.test2.ExchangeScannedData
import fi.tuni.tamk.test2.VisionImageProcessor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * This fragment set camera on and processes the image and scans to barcode
 *
 * @author Jennina Färm
 * @since 1.0
 */
class ScanBarcodeFragment : Fragment(), ExchangeScannedData {
    /** Executes the camera when it is not needed */
    private lateinit var cameraExecutor: ExecutorService
    /** selects the camera and back camera is default */
    private val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    /** use case for the analysis of the image */
    private var analysisUseCase : ImageAnalysis? = null
    /** Vision processor to process the image */
    private var imageProcessor : VisionImageProcessor? = null
    /** view that shows the camera feed */
    private lateinit var viewFinder: PreviewView

    /**
     * Inflates the viewgroup to the fragment
     *
     * @param inflater LayoutInflater to inflate the fragment
     * @param container the viewGroup of the fragment
     * @param savedInstanceState saved instance in state
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?  = inflater.inflate(R.layout.fragment_scan_barcode, container, false)

    /**
     * initializes the viewFinder and starts the camera if permissions are granted else navigates to
     * permissionsFragment
     *
     * @param view the view of the fragment
     * @param savedInstanceState saved instance in state
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewFinder = view.findViewById(R.id.viewFinder)

        // Request camera permissions
        if (PermissionsFragment.hasPermissions(requireContext())) {
            startCamera()
        } else {
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(
                ScanBarcodeFragmentDirections.actionNavigationScanBarcodeToPermissionsFragment()
            )
        }
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    /**
     * If permissions has been granted navigate to camere else inform user
     */
    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults:
            IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (PermissionsFragment.hasPermissions(requireContext())) {
                startCamera()
            } else {
                Toast.makeText(
                    activity,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * starts camera and binds the use cases to it
     */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview use case
            val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(viewFinder.surfaceProvider)
                    }

            setAnalysisUseCase()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, analysisUseCase
                )

            } catch (exc: Exception) {
                //Log.e(TAG, "Use case binding failed", exc)
                error(exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    /**
     * sets up the analysis use case
     */
    private fun setAnalysisUseCase() {
        if (imageProcessor != null) {
            imageProcessor?.stop()
        }

        try {
            imageProcessor = BarcodeScannerProcessor(requireContext(), this)
        } catch (e: Exception) {
            Toast.makeText(
                    activity,
                    "Can not create image processor: " + e.localizedMessage,
                    Toast.LENGTH_LONG
            )
                    .show()
        }

        val builder = ImageAnalysis.Builder()
        analysisUseCase = builder.build()
        analysisUseCase?.setAnalyzer(
                ContextCompat.getMainExecutor(activity),
                ImageAnalysis.Analyzer { imageProxy: ImageProxy ->
                    try {
                        imageProcessor?.processImageProxy(imageProxy)
                    } catch(e: MlKitException) {
                        Toast.makeText(activity, e.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
                }
        )
    }

    /**
     * destroys the fragment when unneeded
     */
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    /**
     * receives the scanned code
     *
     * @param code the barcode that is received
     */
    override fun sendScannedCode(code: String?) {
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            if (code != null && !code.isEmpty() && activity != null) {
                Log.d("Scan Fragment", code)
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(
                    ScanBarcodeFragmentDirections.actionNavigationScanBarcodeToNavigationDashboard(code)
                )
            }
        }
    }

    /**
     * helper object
     */
    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}
