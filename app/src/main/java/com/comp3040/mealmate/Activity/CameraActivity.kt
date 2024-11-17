package com.comp3040.mealmate.Activity

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter
import com.comp3040.mealmate.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private var shouldShowPhoto by mutableStateOf(false)
    private var capturedPhotoUri by mutableStateOf<Uri?>(null)
    private var imageCapture: ImageCapture? = null

    companion object {
        private const val REQUEST_CODE_CAMERA_PERMISSION = 1001
        private const val TAG = "CameraActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Initializing CameraActivity")

        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()

        if (isCameraPermissionGranted()) {
            Log.d(TAG, "Camera permission granted")
            startCameraScreen()
        } else {
            Log.d(TAG, "Requesting camera permission")
            requestCameraPermission()
        }
    }

    private fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_CODE_CAMERA_PERMISSION
        )
    }

    private fun startCameraScreen() {
        setContent {
            CameraScreen(
                onImageCaptured = { uri ->
                    capturedPhotoUri = uri
                    shouldShowPhoto = true
                    Log.d(TAG, "Photo captured: $uri")
                },
                onRetry = {
                    capturedPhotoUri = null
                    shouldShowPhoto = false
                    Log.d(TAG, "Retry clicked")
                },
                onError = { exception ->
                    Log.e(TAG, "Image capture error: ${exception.message}", exception)
                },
                onBackClick = { finish() } // This will close the activity when the back button is clicked
            )
        }
    }


    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return mediaDir ?: filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        Log.d(TAG, "onDestroy: Shutting down executor")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Camera permission granted")
                startCameraScreen()
            } else {
                Log.e(TAG, "Camera permission denied")
                showPermissionDeniedMessage()
            }
        }
    }

    private fun showPermissionDeniedMessage() {
        setContent {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Camera permission is required to use this feature.")
            }
        }
    }
    @Composable
    fun CameraScreen(
        onImageCaptured: (Uri) -> Unit,
        onRetry: () -> Unit,
        onError: (ImageCaptureException) -> Unit,
        onBackClick: () -> Unit // Pass the back button click handler
    ) {
        var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
        val context = LocalContext.current

        DisposableEffect(Unit) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                cameraProvider = cameraProviderFuture.get()
                Log.d(TAG, "Camera provider initialized")
            }, ContextCompat.getMainExecutor(context))

            onDispose {
                cameraProvider?.unbindAll()
                Log.d(TAG, "Camera provider unbound")
            }
        }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (shouldShowPhoto && capturedPhotoUri != null) {
                PhotoPreview(capturedPhotoUri!!, onRetry)
            } else {
                CameraPreview(
                    cameraProvider = cameraProvider,
                    outputDirectory = outputDirectory,
                    executor = cameraExecutor,
                    onImageCaptured = onImageCaptured,
                    onError = onError
                )
            }

            // Back button at the top
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .align(Alignment.TopStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.back), // Ensure the drawable exists
                    contentDescription = "Back Button",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onBackClick() } // Calls the onBackClick() lambda
                )
            }
        }
    }



    @Composable
    fun CameraPreview(
        cameraProvider: ProcessCameraProvider?,
        outputDirectory: File,
        executor: ExecutorService,
        onImageCaptured: (Uri) -> Unit,
        onError: (ImageCaptureException) -> Unit
    ) {
        val context = LocalContext.current
        val previewView = remember { PreviewView(context) }

        if (cameraProvider != null) {
            val preview = Preview.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            imageCapture = ImageCapture.Builder().build()

            cameraProvider.unbindAll()
            try {
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                preview.setSurfaceProvider(previewView.surfaceProvider)
                Log.d(TAG, "Camera preview started")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to bind camera use cases", e)
            }
        }

        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier.fillMaxSize()
        ) {
            AndroidView(
                { previewView },
                modifier = Modifier.fillMaxSize()
            )

            Button(
                onClick = {
                    takePhoto(outputDirectory, executor, onImageCaptured, onError)
                },
                modifier = Modifier
                    .padding(16.dp)
                    .size(60.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Text(text = "Snap")
            }
        }
    }

    @Composable
    fun PhotoPreview(
        photoUri: Uri,
        onRetry: () -> Unit
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Image(
                painter = rememberAsyncImagePainter(photoUri),
                contentDescription = "Captured Photo",
                modifier = Modifier.fillMaxSize()
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Button(onClick = onRetry) {
                    Text(text = "Retry")
                }
                Button(onClick = { Log.d(TAG, "Photo saved or used.") }) {
                    Text(text = "Use Photo")
                }
            }
        }
    }

    private fun takePhoto(
        outputDirectory: File,
        executor: ExecutorService,
        onImageCaptured: (Uri) -> Unit,
        onError: (ImageCaptureException) -> Unit
    ) {
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture?.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    onImageCaptured(savedUri)
                    Log.d(TAG, "Photo saved: $savedUri")
                }

                override fun onError(exception: ImageCaptureException) {
                    onError(exception)
                }
            }
        )
    }
}
