package com.comp3040.mealmate.Model

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.ui.semantics.text
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

class IngredientRecognitionModel {

    fun recognizeIngredients(context: Context, onResult: (List<String>) -> Unit) {
        val resources = context.resources
        val resourceId = resources.getIdentifier("test", "drawable", context.packageName)
        val bitmap = BitmapFactory.decodeResource(resources, resourceId)
        val image = InputImage.fromBitmap(bitmap, 0)
        // Get an instance of ImageLabeler
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

        // Process the image


        labeler.process(image)
            .addOnSuccessListener { labels ->
                val recognizedIngredients = labels.map { it.text }

                // Identify specific ingredients
                val fruits = recognizedIngredients.filter { it in listOf(
                    "apple", "banana", "orange", "strawberry", "grape", "watermelon", "pineapple", "mango",
                    "kiwi", "peach", "pear", "plum", "cherry", "blueberry", "raspberry", "blackberry",
                    "cranberry", "lemon", "lime", "grapefruit", "avocado", "coconut", "papaya", "pomegranate",
                    "fig", "apricot", "nectarine", "cantaloupe", "honeydew", "guava", "passion fruit",
                    "persimmon", "mandarin", "tangerine", "clementine", "lychee", "longan", "durian",
                    "jackfruit", "mangosteen", "breadfruit", "star fruit", "dragon fruit", "rambutan"
                    // ... add more fruits as needed
                ) }
                val meats = recognizedIngredients.filter { it in listOf(
                    "beef", "chicken", "pork", "lamb", "fish", "salmon", "tuna", "shrimp", "crab",
                    "lobster", "turkey", "duck", "goose", "venison", "bison", "elk", "rabbit", "sausage",
                    "bacon", "ham", "steak", "ground beef", "chicken breast", "pork chops", "lamb shank",
                    "salmon fillet", "tuna steak", "shrimp scampi", "crab legs", "lobster tail"
                    // ... add more meats as needed
                ) }
                val vegetables = recognizedIngredients.filter { it in listOf(
                    "carrot", "broccoli", "onion", "tomato", "potato", "spinach", "lettuce", "cucumber",
                    "pepper", "corn", "mushroom", "cabbage", "cauliflower", "peas", "beans", "garlic",
                    "ginger", "celery", "asparagus", "zucchini", "eggplant", "sweet potato", "pumpkin",
                    "squash", "brussels sprouts", "artichoke", "avocado", "beetroot", "radish", "turnip",
                    "parsnip", "kale", "collard greens", "chard", "okra", "leek", "shallot", "fennel",
                    "yam", "plantain", "edamame", "watercress", "arugula"
                    // ... add more vegetables as needed
                ) }
                // Log or display the specific ingredients
                Log.d("IngredientRecognition", "Fruits: $fruits")
                Log.d("IngredientRecognition", "Meats: $meats")
                Log.d("IngredientRecognition", "Vegetables: $vegetables")

                onResult(recognizedIngredients)
                // Display the results in the log
                Log.d("IngredientRecognition", "Recognized ingredients: $recognizedIngredients")
            }
            .addOnFailureListener { e ->
                Log.e("IngredientRecognition", "Error recognizing ingredients: ${e.message}")
                onResult(emptyList()) // Return empty if error occurs
            }
    }
}