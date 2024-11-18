package com.comp3040.mealmate.Activity

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberAsyncImagePainter
import com.comp3040.mealmate.Model.IngredientRecognitionModel
import com.comp3040.mealmate.Model.IngredientRepository
import com.comp3040.mealmate.R
import kotlinx.coroutines.launch
import okio.IOException
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private var base64EncodedPhoto: String? = null
    private lateinit var ingredientRecognitionModel: IngredientRecognitionModel

    companion object {
        private const val REQUEST_CODE_CAMERA_PERMISSION = 1001
        private const val TAG = "CameraActivity"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
        ingredientRecognitionModel = IngredientRecognitionModel(IngredientRepository())

        if (isCameraPermissionGranted()) {
            startCameraScreen()
        } else {
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

    private fun encodeImageToBase64(file: File): String? {
        return try {
            val bytes = FileInputStream(file).use { it.readBytes() }
            Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (e: IOException) {
            Log.e(TAG, "Failed to encode image to Base64", e)
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startCameraScreen() {
        setContent {
            var shouldShowPhoto by remember { mutableStateOf(false) }
            var capturedPhotoUri by remember { mutableStateOf<Uri?>(null) }
            var ingredientsList by remember { mutableStateOf<List<String>>(emptyList()) }
            var recipeSuggestions by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
            var nutritionalInfo by remember { mutableStateOf<Map<String, Map<String, Any>>>(emptyMap()) }
            var isRecognizing by remember { mutableStateOf(false) }

            CameraScreen(
                shouldShowPhoto = shouldShowPhoto,
                capturedPhotoUri = capturedPhotoUri,
                ingredients = ingredientsList,
                recipes = recipeSuggestions,
                nutrition = nutritionalInfo,
                isRecognizing = isRecognizing,
                onImageCaptured = { uri ->
                    capturedPhotoUri = uri
                    shouldShowPhoto = true
                    Log.d(TAG, "Photo captured: $uri")
                },
                onRetry = {
                    capturedPhotoUri = null
                    shouldShowPhoto = false
                    base64EncodedPhoto = null
                    ingredientsList = emptyList()
                    recipeSuggestions = emptyList()
                    nutritionalInfo = emptyMap()
                    isRecognizing = false
                },
                onUsePhoto = {
                    val capturedFile = File(capturedPhotoUri?.path ?: "")
                    base64EncodedPhoto = encodeImageToBase64(capturedFile)
                    base64EncodedPhoto?.let { base64 ->
                        lifecycleScope.launch {
                            isRecognizing = true
                            try {
                                val recognitionResult = ingredientRecognitionModel.recognizeIngredients(base64)
                                ingredientsList = recognitionResult["Ingredients"] as? List<String> ?: emptyList()
                                recipeSuggestions = recognitionResult["RecipeSuggestions"] as? List<Map<String, String>> ?: emptyList()
                                nutritionalInfo = recognitionResult["NutritionalInformation"] as? Map<String, Map<String, Any>> ?: emptyMap()
                            } catch (e: Exception) {
                                Log.e(TAG, "Recognition failed: ${e.message}", e)
                            } finally {
                                isRecognizing = false
                            }
                        }
                    } ?: run { Log.e(TAG, "Base64 encoding failed.") }
                },
                onError = { exception ->
                    Log.e(TAG, "Image capture error: ${exception.message}", exception)
                },
                onBackClick = { finish() }
            )
        }
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return mediaDir ?: filesDir
    }



    @Composable
    fun CameraScreen(
        shouldShowPhoto: Boolean,
        capturedPhotoUri: Uri?,
        ingredients: List<String>,
        recipes: List<Map<String, String>>,
        nutrition: Map<String, Map<String, Any>>,
        isRecognizing: Boolean,
        onImageCaptured: (Uri) -> Unit,
        onRetry: () -> Unit,
        onUsePhoto: () -> Unit,
        onError: (ImageCaptureException) -> Unit,
        onBackClick: () -> Unit
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (shouldShowPhoto && capturedPhotoUri != null) {
                PhotoPreview(
                    photoUri = capturedPhotoUri,
                    ingredients = ingredients,  // Corrected parameter
                    recipes = recipes,          // Corrected parameter
                    nutrition = nutrition,      // Corrected parameter
                    isRecognizing = isRecognizing,
                    onRetry = onRetry,
                    onUsePhoto = onUsePhoto
                )
            } else {
                CameraPreview(
                    onImageCaptured = onImageCaptured,
                    onError = onError
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.back),
                    contentDescription = "Back Button",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onBackClick() }
                )
            }
        }
    }


    @Composable
    fun PhotoPreview(
        photoUri: Uri,
        ingredients: List<String>,
        recipes: List<Map<String, String>>,
        nutrition: Map<String, Map<String, Any>>,
        isRecognizing: Boolean,
        onRetry: () -> Unit,
        onUsePhoto: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Image(
                    painter = rememberAsyncImagePainter(photoUri),
                    contentDescription = "Captured Photo",
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (isRecognizing) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    item {
                        Text("Ingredients:", color = Color.Black, modifier = Modifier.padding(8.dp))
                    }
                    items(ingredients) { ingredient ->
                        Text(text = ingredient, color = Color.Black, modifier = Modifier.padding(8.dp))
                    }

                    item {
                        Text("Recipe Suggestions:", color = Color.Black, modifier = Modifier.padding(8.dp))
                    }
                    items(recipes) { recipe ->
                        Text(
                            text = "${recipe["name"]}: ${recipe["description"]}\nLink: ${recipe["link"]}",
                            color = Color.Black,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    item {
                        Text("Nutritional Information:", color = Color.Black, modifier = Modifier.padding(8.dp))
                    }
                    items(nutrition.keys.toList()) { ingredient ->
                        val details = nutrition[ingredient] ?: mapOf()
                        Text(
                            text = "$ingredient - Calories: ${details["calories"]}, Protein: ${details["protein"]}g, Fats: ${details["fats"]}g, Vitamins: ${(details["vitamins"] as? List<*>)?.joinToString() ?: "None"}",
                            color = Color.Black,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onRetry,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                ) {
                    Text(text = "Retry")
                }
                Button(
                    onClick = onUsePhoto,
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                ) {
                    Text(text = "Use Photo")
                }
            }
        }
    }



    @Composable
    fun CameraPreview(
        onImageCaptured: (Uri) -> Unit,
        onError: (ImageCaptureException) -> Unit
    ) {
        val context = LocalContext.current
        val previewView = remember { PreviewView(context) }

        if (imageCapture == null) {
            val preview = Preview.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            imageCapture = ImageCapture.Builder().build()

            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                preview.setSurfaceProvider(previewView.surfaceProvider)
            }, ContextCompat.getMainExecutor(context))
        }

        Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize()) {
            AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
            Button(
                onClick = {
                    val photoFile = File(
                        outputDirectory,
                        SimpleDateFormat(
                            "yyyy-MM-dd-HH-mm-ss-SSS",
                            Locale.US
                        ).format(System.currentTimeMillis()) + ".jpg"
                    )
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                    imageCapture?.takePicture(
                        outputOptions,
                        cameraExecutor,
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                val savedUri = Uri.fromFile(photoFile)
                                onImageCaptured(savedUri)
                            }

                            override fun onError(exception: ImageCaptureException) {
                                onError(exception)
                            }
                        }
                    )
                },
                modifier = Modifier
                    .padding(16.dp)
                    .size(60.dp)
            ) {
                Text(text = "Snap")
            }
        }
    }
}
