package fi.tuni.tamk.test2

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.hardware.display.DisplayManager
import android.media.Image
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.camera.core.*
import androidx.camera.view.PreviewView

import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.Navigation
import kotlinx.coroutines.Dispatchers
import java.lang.Exception
import java.lang.IllegalStateException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/** Helper type alias used for analysis use case callbacks */
//typealias LumaListener = (luma: Double) -> Unit

/**
 * A simple [Fragment] subclass.
 * Use the [CameraFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CameraFragment : Fragment() {

    private lateinit var container: ConstraintLayout
    private lateinit var cameraView : PreviewView
    private lateinit var broadcastManager: LocalBroadcastManager

    private var displayId : Int = -1
    private var lensFacing : Int = CameraSelector.LENS_FACING_BACK
    private var somePreview : Preview? = null
    private var imageAlyzer : ImageAnalysis? = null
    private var camera : Camera? = null
    private var cameraProvider : ProcessCameraProvider? = null

    private val displayManager by lazy {
        requireContext().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    }

    /** Will block the camere operations*/
    private lateinit var cameraExecutor : ExecutorService

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit
        override fun onDisplayChanged(displayId: Int) =view?.let {
            //TODO("Onko tarpeellinen iffi???")
            if(displayId == this@CameraFragment.displayId) {
                Log.d("CameraFragment", "Rotation changed: ${it.display.rotation}")
                imageAlyzer?.targetRotation = it.display.rotation
            }
        } ?: Unit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //TODO("tänne jotain ehkä")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_camera, container, false)

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        container = view as ConstraintLayout
        cameraView = container.findViewById(R.id.camera_preview) as PreviewView

        // init backgorund executor
        cameraExecutor = Executors.newSingleThreadExecutor()
        // change rotation if orientation of device changes
        displayManager.registerDisplayListener(displayListener, null)

        cameraView.post {
            displayId = cameraView.display.displayId
            updateCameraUi()
            setUpCamera()
        }
    }

/*
    override fun onResume() {
        super.onResume()
        // making sure permissions are present
        if(!PermissionsFragment.hasPermissions(requireContext())) {
            Navigation.findNavController(requireActivity(), R.id.fragment_container).navigate(
                CameraFragmentDirections.actionCameraToPermissions()
            )
        }
    }
*/
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        displayManager.unregisterDisplayListener(displayListener)
    }

 /*   override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateCameraUi()
        //enable or
        updateCameraSwitchButton()
    }*/

    private fun setUpCamera() {
        val newCameraProvider = ProcessCameraProvider.getInstance(requireContext())
        newCameraProvider.addListener( {
            cameraProvider = newCameraProvider.get()
            lensFacing = when {
                hasBackCamera() -> CameraSelector.LENS_FACING_BACK
                hasFrontCamera() -> CameraSelector.LENS_FACING_FRONT
                else -> throw IllegalStateException("Cameras are unavailable")
            }

            updateCameraSwitchButton()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun bindCameraUseCases() {
        // Get screen metrics used to setup camera for full screen resolution
        //val metrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
        //Log.d(TAG, "Screen metrics: ${metrics.widthPixels} x ${metrics.heightPixels}")
        //val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)

        val rotation = cameraView.display.rotation
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed")
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        somePreview = Preview.Builder()
            //.setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()

        // need to undind befor rebinding
        cameraProvider.unbindAll()
        try {
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, somePreview, imageAlyzer
            )
            somePreview?.setSurfaceProvider(cameraView.surfaceProvider)
        } catch (e : Exception) {
            Log.e("CameraFragment", "Use case binding failed:", e)
        }
    }

    private fun updateCameraUi() {
        //remove previous if there is one
        container.findViewById<ConstraintLayout>(R.id.camera_live_ui)?.let {
            container.removeView(it)
        }

        val controls = View.inflate(requireContext(), R.layout.camera_live_ui, container)

        // add listener to switch camera button
        controls.findViewById<ImageButton>(R.id.camera_switch_button).let {
            // disable until camera is set up
            it.isEnabled = false

            it.setOnClickListener {
                lensFacing = if(CameraSelector.LENS_FACING_FRONT == lensFacing) {
                    CameraSelector.LENS_FACING_BACK
                } else {
                    CameraSelector.LENS_FACING_FRONT
                }
                // rebind to update selected camera
                bindCameraUseCases()
            }
        }
    }

    /**
     * To enable or disable the camera switch button if cameras are available
     */
    private fun updateCameraSwitchButton() {
        val switchCameraButton = container.findViewById<ImageButton>(R.id.camera_switch_button)
        try {
            switchCameraButton.isEnabled = hasBackCamera() && hasFrontCamera()
        } catch(cameraException: CameraInfoUnavailableException) {
            switchCameraButton.isEnabled = false
        }
    }

    /** Returns true if back camera is avalable */
    private fun hasBackCamera() : Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
    }
    /** Returns true if front camera is available */
    private fun hasFrontCamera() : Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CameraFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CameraFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}