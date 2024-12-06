package com.comp3040.mealmate.Activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable

import androidx.compose.foundation.lazy.items
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberAsyncImagePainter
import com.comp3040.mealmate.Model.IngredientRecognitionModel
import com.comp3040.mealmate.Model.IngredientRepository
import com.comp3040.mealmate.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
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
    private lateinit var database: DatabaseReference

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    // MutableStates for UI updates
    private val galleryPhotoUri = mutableStateOf<Uri?>(null) // State for selected gallery photo
    private val recognizedIngredients = mutableStateOf<List<String>>(emptyList())
    private val recipeSuggestions = mutableStateOf<List<Map<String, String>>>(emptyList())
    private val nutritionalInfo = mutableStateOf<Map<String, Map<String, Any>>>(emptyMap())
    private val isRecognizing = mutableStateOf(false)

    companion object {
        private const val REQUEST_CODE_CAMERA_PERMISSION = 1001
        private const val TAG = "CameraActivity"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = FirebaseDatabase.getInstance().reference

        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val imageUri = result.data?.data
                imageUri?.let {
                    galleryPhotoUri.value = it // Update the state with the selected photo
                    convertImageToBase64(it)  // Convert image to Base64 for recognition
                }
            }
        }

        if (isCameraPermissionGranted()) {
            startCameraScreen()
        } else {
            requestCameraPermission()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun performIngredientRecognition() {
        if (base64EncodedPhoto == null) {
            Toast.makeText(this, "No photo available for recognition.", Toast.LENGTH_SHORT).show()
            return
        }

        isRecognizing.value = true
        lifecycleScope.launch {
            try {
                val ingredientRecognitionModel = IngredientRecognitionModel(IngredientRepository())

                // Recognize ingredients
                val recognitionResult = ingredientRecognitionModel.recognizeIngredients(base64EncodedPhoto!!)
                val ingredients = recognitionResult["Ingredients"] as? List<String> ?: emptyList()
                val recipes = recognitionResult["RecipeSuggestions"] as? List<Map<String, String>> ?: emptyList()

                // Save photo and recognition results
                val userId = getCurrentUserId() // Retrieve current user ID
                val photoId = ingredientRecognitionModel.savePhoto(base64EncodedPhoto!!, userId)
                if (photoId != null) {
                    ingredientRecognitionModel.saveRecognitionResults(photoId, ingredients, userId)
                }

                // Update UI with results
                recognizedIngredients.value = ingredients
                recipeSuggestions.value = recipes
                Toast.makeText(this@CameraActivity, "Recognition completed successfully.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "Error recognizing ingredients: ${e.message}", e)
                Toast.makeText(this@CameraActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isRecognizing.value = false
            }
        }
    }



    /**
     * Retrieves the current user's ID.
     * This assumes you are using Firebase Authentication.
     */
    private fun getCurrentUserId(): String {
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        return currentUser?.uid ?: "UnknownUser" // Default to "UnknownUser" if not logged in
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startCameraScreen() {
        setContent {
            CameraScreen(
                shouldShowPhoto = galleryPhotoUri.value != null || base64EncodedPhoto != null, // Include gallery photo condition
                capturedPhotoUri = galleryPhotoUri.value, // Pass the selected photo URI
                ingredients = recognizedIngredients.value,
                recipes = recipeSuggestions.value,
                nutrition = nutritionalInfo.value,
                isRecognizing = isRecognizing.value,
                onImageCaptured = { uri ->
                    galleryPhotoUri.value = uri
                    convertImageToBase64(uri)
                },
                onRetry = {
                    base64EncodedPhoto = null
                    galleryPhotoUri.value = null
                    recognizedIngredients.value = emptyList()
                    recipeSuggestions.value = emptyList()
                    nutritionalInfo.value = emptyMap()
                },
                onUsePhoto = {
                    performIngredientRecognition()
                },
                onError = { exception -> Log.e(TAG, "Image capture error: ${exception.message}") },
                onBackClick = { finish() },
                cameraExecutor = cameraExecutor,
                outputDirectory = outputDirectory,
                onGalleryClick = { launchImagePicker() }
            )
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraScreen()
            } else {
                Toast.makeText(
                    this,
                    "Camera permission is required to use this feature.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return mediaDir ?: filesDir
    }






    private fun launchImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*" // Restrict to image files
        }
        imagePickerLauncher.launch(intent) // Launch the image picker
    }

    private fun convertImageToBase64(imageUri: Uri) {
        lifecycleScope.launch {
            try {
                val inputStream = contentResolver.openInputStream(imageUri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes != null) {
                    base64EncodedPhoto = Base64.encodeToString(bytes, Base64.DEFAULT)
                    Log.d(TAG, "Base64 Encoded Image: $base64EncodedPhoto")
                    Toast.makeText(this@CameraActivity, "Image converted to Base64.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@CameraActivity, "Failed to read image data.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error converting image to Base64: ${e.message}")
                Toast.makeText(this@CameraActivity, "Error converting image.", Toast.LENGTH_SHORT).show()
            }
        }
    }
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
    onUsePhoto: () -> Unit, // Pass this callback
    onError: (ImageCaptureException) -> Unit,
    onBackClick: () -> Unit,
    cameraExecutor: ExecutorService,
    outputDirectory: File,
    onGalleryClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (shouldShowPhoto && capturedPhotoUri != null) {
            PhotoPreview(
                photoUri = capturedPhotoUri,
                ingredients = ingredients,
                recipes = recipes,
                nutrition = nutrition,
                isRecognizing = isRecognizing,
                onRetry = onRetry,
                onUsePhoto = onUsePhoto // Pass the callback here
            )
        } else {
            CameraPreview(
                onImageCaptured = onImageCaptured,
                onError = onError,
                cameraExecutor = cameraExecutor,
                outputDirectory = outputDirectory,
                onGalleryClick = onGalleryClick
            )
        }

        // Back Button
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
    // State to control the visibility of the instruction overlay
    var showInstruction by remember(photoUri) { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display the captured or selected image
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(photoUri),
                contentDescription = "Captured Photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .align(Alignment.Center)
            )

            // Instruction overlay
            if (showInstruction) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6F))
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Press 'Use Photo' to recognize the ingredients.",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        // Data box with scrolling LazyColumn
        Box(
            modifier = Modifier
                .weight(2f)
                .fillMaxWidth()
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
        ) {
            if (isRecognizing) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    // Ingredients
                    item {
                        Text("Ingredients:", style = MaterialTheme.typography.headlineSmall)
                    }
                    items(ingredients.ifEmpty { listOf("No ingredients found") }) { ingredient ->
                        Text(
                            text = ingredient,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Recipes
                    item {
                        Text("Recipes:", style = MaterialTheme.typography.headlineSmall)
                    }
                    items(recipes.ifEmpty { listOf(mapOf("name" to "No recipes found", "description" to "")) }) { recipe ->
                        val recipeName = recipe["name"] ?: "Unnamed Recipe"
                        val recipeDesc = recipe["description"] ?: "No description available"
                        Text(
                            "$recipeName: $recipeDesc",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // Retry and Use Photo Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = onRetry, modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text("Retry")
            }
            Button(
                onClick = {
                    showInstruction = false // Hide the instruction overlay
                    onUsePhoto() // Trigger the recognition process
                },
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            ) {
                Text("Use Photo")
            }
        }
    }
}


@Composable
fun CameraPreview(
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit,
    cameraExecutor: ExecutorService,
    outputDirectory: File,
    onGalleryClick: () -> Unit // Add this parameter
) {
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }

    val localImageCapture = remember { mutableStateOf<ImageCapture?>(null) }

    if (localImageCapture.value == null) {
        val preview = Preview.Builder().build()
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        val newImageCapture = ImageCapture.Builder().build()
        localImageCapture.value = newImageCapture

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                (context as AppCompatActivity),
                cameraSelector,
                preview,
                newImageCapture
            )
            preview.setSurfaceProvider(previewView.surfaceProvider)
        }, ContextCompat.getMainExecutor(context))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview
        AndroidView({ previewView }, modifier = Modifier.fillMaxSize())

        // Text overlay for instructions
        Text(
            text = "Instructions:\nTake a clear photo of your ingredient.\nMake sure the whole item is in the frame.\nSupports recognizing multiple ingredients.",
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.6F)) // Add a semi-transparent background
                .align(Alignment.TopCenter)
        )

        // Bottom controls for "Snap" and "Gallery" buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Gallery Button
            Button(
                onClick = { onGalleryClick() }, // Trigger the gallery callback
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)
            ) {
                Text("Load From Gallery")
            }

            // Snap Button
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

                    localImageCapture.value?.takePicture(
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
                    .padding(8.dp)
                    .weight(1f)
            ) {
                Text("Snap")
            }
        }
    }
}




