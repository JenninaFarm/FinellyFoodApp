package fi.tuni.tamk.test2

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskExecutors
import com.google.mlkit.vision.common.InputImage
import java.util.*

/**
 *  Prosessor for the vision, Abstract class
 */
abstract class VisionProcessor<T> protected constructor(context: Context) : VisionImageProcessor {
    /** executed the processor if needed */
    private val executor: ScopedExecutor = ScopedExecutor(TaskExecutors.MAIN_THREAD)
    /** boolean for detect if processor is shut down */
    private var isShutdown = false

    /**
     * Stops the processor when needed
     */
    override fun stop() {
        executor.shutdown()
        isShutdown = true
    }

    /**
     * requests the procession the image proxy
     *
     * @param image The image that is processed
     */
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    @ExperimentalGetImage
    override fun processImageProxy(image: ImageProxy) {
        if (isShutdown) {
            image.close()
            return
        }

        requestDetectInImage(
            InputImage.fromMediaImage(image.image, image.imageInfo.rotationDegrees))
            .addOnCompleteListener { results: Task<T>? -> image.close() }
    }

    /**
     * Processes the image given
     * @param image the image that is wanted to process
     * @return the task that is given after image has been processed
     */
    private fun requestDetectInImage(
        image: InputImage): Task<T> {
        return detectInImage(image)
            .addOnSuccessListener(
                executor
            ) { results ->
                this@VisionProcessor.onSuccess(results)
            }
            .addOnFailureListener(
                executor
            ) { e ->
                val error = "Failed to process. Error: " + e.localizedMessage
                e.printStackTrace()
                this@VisionProcessor.onFailure(e)
            }
    }

    /**
     * Detects the image
     * @param image the image that is processed
     * @return the Task of the processed image
     */
    protected abstract fun detectInImage(image: InputImage?): Task<T>

    /**
     * Success function for the procession
     * @param results element of results
     */
    protected abstract fun onSuccess(results: T)

    /**
     * Failure function of the procession
     * @param e the exception of the failure
     */
    protected abstract fun onFailure(e: Exception)
}