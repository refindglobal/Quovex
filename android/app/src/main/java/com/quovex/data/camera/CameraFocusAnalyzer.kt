package com.quovex.data.camera

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.quovex.domain.model.FocusFrameResult
import java.util.concurrent.atomic.AtomicBoolean

/**
 * On-device camera frame analyzer for real-time focus & drowsiness detection.
 * Throttles inference to ~1 frame per second to maintain negligible battery and CPU impact.
 * Ensures zero camera data leaves the device RAM.
 */
class CameraFocusAnalyzer(
    private val onFrameAnalyzed: (FocusFrameResult) -> Unit
) : ImageAnalysis.Analyzer {

    private val isProcessing = AtomicBoolean(false)
    private var lastAnalysisTimestamp = 0L

    private val detector: FaceDetector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL) // Enables eye open probabilities
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            .setMinFaceSize(0.18f)
            .build()
        FaceDetection.getClient(options)
    }

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val currentTime = System.currentTimeMillis()

        // Rate-limit analysis to ~1 FPS (1000ms intervals) to save device battery
        if (currentTime - lastAnalysisTimestamp < 900L || !isProcessing.compareAndSet(false, true)) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            isProcessing.set(false)
            return
        }

        val rotation = imageProxy.imageInfo.rotationDegrees
        val inputImage = InputImage.fromMediaImage(mediaImage, rotation)

        detector.process(inputImage)
            .addOnSuccessListener { faces ->
                lastAnalysisTimestamp = System.currentTimeMillis()

                if (faces.isEmpty()) {
                    onFrameAnalyzed(
                        FocusFrameResult(
                            isFaceDetected = false,
                            isDrowsy = false,
                            isLookingAway = false
                        )
                    )
                } else {
                    // Pick the primary face in the foreground
                    val primaryFace = faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() } ?: faces[0]

                    val leftEyeOpen = primaryFace.leftEyeOpenProbability ?: 1.0f
                    val rightEyeOpen = primaryFace.rightEyeOpenProbability ?: 1.0f

                    // Drowsiness: both eyes closed (< 0.25 probability)
                    val isDrowsy = leftEyeOpen < 0.25f && rightEyeOpen < 0.25f

                    // Looking away: head yaw (Y) or pitch (X) turned away > 24 degrees
                    val headEulerY = primaryFace.headEulerAngleY
                    val headEulerX = primaryFace.headEulerAngleX
                    val isLookingAway = kotlin.math.abs(headEulerY) > 24f || kotlin.math.abs(headEulerX) > 24f

                    onFrameAnalyzed(
                        FocusFrameResult(
                            isFaceDetected = true,
                            isDrowsy = isDrowsy,
                            isLookingAway = isLookingAway
                        )
                    )
                }
            }
            .addOnFailureListener {
                // In case of transient ML Kit error, close silently without failing session
            }
            .addOnCompleteListener {
                imageProxy.close()
                isProcessing.set(false)
            }
    }
}
