package fi.tuni.tamk.test2

import android.os.Build.VERSION_CODES
import androidx.annotation.RequiresApi
import androidx.camera.core.ImageProxy
import com.google.mlkit.common.MlKitException

/** An interface to process the images with different vision detectors and
 * custom image models.
 */
interface VisionImageProcessor {

    /** Processes ImageProxy image data, e.g. used for CameraX live preview case.  */
    @RequiresApi(VERSION_CODES.KITKAT)
    @Throws(MlKitException::class)
    fun processImageProxy(image: ImageProxy)

    /** Stops the underlying machine learning model and release resources.  */
    fun stop()
}