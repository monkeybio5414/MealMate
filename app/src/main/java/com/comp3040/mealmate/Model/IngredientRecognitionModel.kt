package com.comp3040.mealmate.Model

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * A model class responsible for ingredient recognition using an external API,
 * saving photos and recognition results to Firebase, and parsing API responses.
 *
 * @param repository An instance of `IngredientRepository` for managing data operations.
 */
@RequiresApi(Build.VERSION_CODES.O)
class IngredientRecognitionModel(private val repository: IngredientRepository) {

    companion object {
        private const val API_URL = "https://api.pumpkinaigc.online/v1/chat/completions" // External API endpoint
        private const val API_KEY = "Bearer sk-53apYwt9DO3KkSZjAeFd0eEcA8Cd4901B7Bf9e9f696aD81a" // API key for authentication
        private const val TAG = "IngredientRecognition" // Log tag for debugging
    }

    // Firebase database reference for saving and retrieving data
    private val database: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference
    }

    // OkHttp client instance with logging and timeout configurations
    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY // Logs request and response bodies
            })
            .connectTimeout(3, TimeUnit.MINUTES)
            .writeTimeout(3, TimeUnit.MINUTES)
            .readTimeout(3, TimeUnit.MINUTES)
            .build()
    }

    /**
     * Saves a photo to Firebase with its Base64 representation.
     * @param base64Image Base64 encoded string of the image.
     * @param userId The ID of the user uploading the photo.
     * @return The unique ID of the saved photo or `null` if the save operation failed.
     */
    suspend fun savePhoto(base64Image: String, userId: String): String? {
        val photoId = database.child("Photos").push().key ?: return null
        val uploadDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(System.currentTimeMillis())

        val photoData = mapOf(
            "ImageData" to base64Image, // Encoded image data
            "UploadDate" to uploadDate, // Current upload date
            "UserId" to userId // Link photo to user
        )

        return withContext(Dispatchers.IO) {
            try {
                database.child("Photos").child(photoId).setValue(photoData).await()
                Log.d(TAG, "Photo saved successfully with ID: $photoId")
                photoId
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save photo: ${e.message}", e)
                null
            }
        }
    }

    /**
     * Saves the ingredient recognition results to Firebase.
     * @param photoId The ID of the photo related to the recognition.
     * @param recognizedIngredients A list of recognized ingredients.
     * @param userId The ID of the user associated with the recognition.
     */
    suspend fun saveRecognitionResults(photoId: String, recognizedIngredients: List<String>, userId: String) {
        val recognitionId = database.child("RecognitionResults").push().key ?: return
        val recognitionDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(System.currentTimeMillis())

        val recognitionData = mapOf(
            "RecognitionDate" to recognitionDate, // Date of recognition
            "RecognizedIngredients" to recognizedIngredients.mapIndexed { index, ingredient ->
                (index + 1).toString() to ingredient // Map each ingredient to its position
            }.toMap(),
            "UserId" to userId, // User who initiated the recognition
            "PhotoId" to photoId // Associated photo ID
        )

        withContext(Dispatchers.IO) {
            try {
                database.child("RecognitionResults").child(recognitionId).setValue(recognitionData).await()
                Log.d(TAG, "Recognition results saved successfully with ID: $recognitionId")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save recognition results: ${e.message}", e)
            }
        }
    }

    /**
     * Sends a Base64 image to the API for ingredient recognition and parses the response.
     * @param base64Image Base64 encoded string of the image.
     * @return A map containing the recognition results or an error message.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun recognizeIngredients(base64Image: String): Map<String, Any> {
        Log.d(TAG, "Starting ingredient recognition.")
        Log.d(TAG, "Base64 image length: ${base64Image.length}")

        // Sanitize Base64 string by removing newlines
        val sanitizedBase64Image = base64Image.replace("\n", "")
        Log.d(TAG, "Sanitized Base64 image length: ${sanitizedBase64Image.length}")

        // Construct JSON payload programmatically
        val messagesArray = listOf(
            mapOf(
                "role" to "user",
                "content" to listOf(
                    mapOf(
                        "type" to "text",
                        "text" to """
                    List all the ingredients you can identify in this image. Format your response as a structured JSON object with the following fields:
                    1. **Ingredients**: A list of identified ingredients as strings. If no ingredients are detected, set this to an empty array (e.g., []).
                    2. **RecipeSuggestions**: An array of objects, where each object contains:
                        - name: The name of the recipe.
                        - description: A brief description of the recipe.
                        - link: A URL to the recipe (or null if unavailable).
                    3. **NutritionalInformation**: A key-value map where each key is an ingredient name and the value is an object with:
                        - calories: Integer value for calories (or 0 if unavailable).
                        - protein: Double value for protein in grams (or 0.0 if unavailable).
                        - fats: Double value for fats in grams (or 0.0 if unavailable).
                        - vitamins: A list of vitamins as strings (or an empty array if unavailable).
                    Respond with 'No data available' if nothing can be detected in the image.
                    """.trimIndent()
                    ),
                    mapOf(
                        "type" to "image_url",
                        "image_url" to mapOf(
                            "url" to "data:image/jpeg;base64,$sanitizedBase64Image"
                        )
                    )
                )
            )
        )

        val payload = mapOf(
            "model" to "gpt-4o-mini",
            "messages" to messagesArray
        )

        val payloadJson = JSONObject(payload).toString()
        Log.d(TAG, "Payload prepared:\n$payloadJson")

        val requestBody = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            payloadJson
        )

        val request = Request.Builder()
            .url(API_URL)
            .addHeader("Authorization", API_KEY)
            .post(requestBody)
            .build()

        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Sending request to the API.")
                httpClient.newCall(request).execute().use { response ->
                    Log.d(TAG, "API Response Code: ${response.code}")
                    if (!response.isSuccessful) {
                        val errorBody = response.body?.string()
                        Log.e(TAG, "Error Body: $errorBody")
                        return@withContext mapOf("error" to "API call failed with code ${response.code}")
                    }

                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        Log.d(TAG, "API Response Body:\n$responseBody")
                        parseResponse(responseBody)
                    } else {
                        Log.e(TAG, "API returned an empty response.")
                        mapOf("error" to "Empty response from API")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during API request: ${e.message}", e)
                mapOf("error" to e.message.toString())
            }
        }
    }

    /**
     * Parses the API response to extract ingredients, recipes, and nutritional information.
     * @param responseBody The JSON response from the API.
     * @return A map containing parsed results or an error message.
     */
    private fun parseResponse(responseBody: String): Map<String, Any> {
        Log.d(TAG, "Parsing API response for ingredients, recipes, and nutritional information.")
        val result = mutableMapOf<String, Any>()

        try {
            val responseJson = JSONObject(responseBody)
            val choicesArray = responseJson.optJSONArray("choices")
            if (choicesArray == null || choicesArray.length() == 0) {
                result["error"] = "No choices found in response"
                return result
            }

            val messageContent = choicesArray
                .getJSONObject(0)
                .optJSONObject("message")
                ?.optString("content", "")

            if (messageContent.isNullOrEmpty()) {
                result["error"] = "No content found in message"
                return result
            }

            // Extract embedded JSON string
            val embeddedJson = messageContent
                .replace("```json", "")
                .replace("```", "")
                .trim()

            // Parse JSON for ingredients, recipes, and nutritional information
            val embeddedJsonObject = JSONObject(embeddedJson)

            // Extract "Ingredients"
            val ingredientsArray = embeddedJsonObject.optJSONArray("Ingredients")
            val ingredients = mutableListOf<String>()
            if (ingredientsArray != null) {
                for (i in 0 until ingredientsArray.length()) {
                    ingredients.add(ingredientsArray.getString(i))
                }
            }
            result["Ingredients"] = ingredients.ifEmpty { listOf("No ingredients detected") }

            // Extract "RecipeSuggestions"
            val recipeSuggestionsArray = embeddedJsonObject.optJSONArray("RecipeSuggestions")
            val recipes = mutableListOf<Map<String, String>>()
            if (recipeSuggestionsArray != null) {
                for (i in 0 until recipeSuggestionsArray.length()) {
                    val recipeObject = recipeSuggestionsArray.getJSONObject(i)
                    recipes.add(
                        mapOf(
                            "name" to recipeObject.optString("name", "No name available"),
                            "description" to recipeObject.optString("description", "No description available"),
                            "link" to recipeObject.optString("link", "No link available")
                        )
                    )
                }
            }
            result["RecipeSuggestions"] = if (recipes.isNotEmpty()) recipes else listOf(mapOf("message" to "No recipes available"))

            // Extract "NutritionalInformation"
            val nutritionalInfoObject = embeddedJsonObject.optJSONObject("NutritionalInformation")
            val nutritionalInfo = mutableMapOf<String, Map<String, Any>>()
            if (nutritionalInfoObject != null) {
                val keys = nutritionalInfoObject.keys()
                while (keys.hasNext()) {
                    val ingredient = keys.next()
                    val nutritionDetails = nutritionalInfoObject.getJSONObject(ingredient)
                    nutritionalInfo[ingredient] = mapOf(
                        "calories" to nutritionDetails.optInt("calories", 0),
                        "protein" to nutritionDetails.optDouble("protein", 0.0),
                        "fats" to nutritionDetails.optDouble("fats", 0.0),
                        "vitamins" to (nutritionDetails.optJSONArray("vitamins")?.let { array ->
                            List(array.length()) { index -> array.optString(index, "Unknown") }
                        } ?: emptyList<String>())
                    )
                }
            }
            result["NutritionalInformation"] = if (nutritionalInfo.isNotEmpty()) nutritionalInfo else mapOf("message" to "No nutritional information available")

        } catch (e: Exception) {
            Log.e(TAG, "Error parsing response: ${e.message}", e)
            result["error"] = "Error parsing response: ${e.message}"
        }

        Log.d(TAG, "Parsed response: $result")
        return result
    }
}
