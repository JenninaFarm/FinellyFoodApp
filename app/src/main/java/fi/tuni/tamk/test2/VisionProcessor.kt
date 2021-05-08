package fi.tuni.tamk.test2

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.annotation.GuardedBy
import androidx.annotation.RequiresApi
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskExecutors
import com.google.mlkit.vision.common.InputImage
import java.nio.ByteBuffer
import java.util.*

abstract class VisionProcessor<T> protected constructor(context: Context) : VisionImageProcessor {
    private val activityManager : ActivityManager? = null
    private val fpsTimer = Timer()
    private val executor: ScopedExecutor = ScopedExecutor(TaskExecutors.MAIN_THREAD)

    // Whether this processor is already shut down
    private var isShutdown = false

    // Used to calculate latency, running in the same thread, no sync needed.
    private var numRuns = 0
    private var totalRunMs: Long = 0
    private var maxRunMs: Long = 0
    private var minRunMs = Long.MAX_VALUE

    // Frame count that have been processed so far in an one second interval to calculate FPS.
    private var frameProcessedInOneSecondInterval = 0
    private var framesPerSecond = 0

    // To keep the latest images and its metadata.
    @GuardedBy("this")
    private var latestImage: ByteBuffer? = null

    @GuardedBy("this")
    private var latestImageMetaData: FrameMetadata? = null

    // To keep the images and metadata in process.
    @GuardedBy("this")
    private var processingImage: ByteBuffer? = null

    @GuardedBy("this")
    private var processingMetaData: FrameMetadata? = null

    override fun stop() {
        executor.shutdown()
        isShutdown = true
        numRuns = 0
        totalRunMs = 0
        fpsTimer.cancel()
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    @ExperimentalGetImage
    override fun processImageProxy(image: ImageProxy) {
        if (isShutdown) {
            image.close()
            return
        }

        requestDetectInImage(
            InputImage.fromMediaImage(image.image, image.imageInfo.rotationDegrees),
            null,
            true)
            .addOnCompleteListener { results: Task<T>? -> image.close() }
    }

    // -----------------Common processing logic-------------------------------------------------------
    private fun requestDetectInImage(
        image: InputImage,
        originalCameraImage: Bitmap?,
        shouldShowFps: Boolean): Task<T> {
        val startMs = SystemClock.elapsedRealtime()
        return detectInImage(image)
            .addOnSuccessListener(
                executor
            ) { results ->
                val currentLatencyMs = SystemClock.elapsedRealtime() - startMs
                numRuns++
                frameProcessedInOneSecondInterval++
                totalRunMs += currentLatencyMs
                maxRunMs = Math.max(currentLatencyMs, maxRunMs)
                minRunMs = Math.min(currentLatencyMs, minRunMs)

                // Only log inference info once per second. When frameProcessedInOneSecondInterval is
                // equal to 1, it means this is the first frame processed during the current second.
                if (frameProcessedInOneSecondInterval == 1) {
                    Log.d(TAG, "Max latency is: $maxRunMs")
                    Log.d(TAG, "Min latency is: $minRunMs")
                    Log.d(TAG, "Num of Runs: " + numRuns + ", Avg latency is: " + totalRunMs / numRuns)
                    val mi = ActivityManager.MemoryInfo()
                    activityManager?.getMemoryInfo(mi)
                    val availableMegs = mi.availMem / 0x100000L
                    Log.d(TAG, "Memory available in system: $availableMegs MB")
                }
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

    protected abstract fun detectInImage(image: InputImage?): Task<T>
    protected abstract fun onSuccess(results: T)
    protected abstract fun onFailure(e: Exception)

    companion object {
        private const val TAG = "VisionProcessor"
    }
}