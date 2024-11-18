package com.comp3040.mealmate.Model


import android.os.Build
import androidx.annotation.RequiresApi
import org.json.JSONObject

@RequiresApi(Build.VERSION_CODES.O)
fun main() {
    // Simulated API JSON response for testing parsing
    val jsonResponse = """
        {
          "choices": [
            {
              "finish_reason": "stop",
              "index": 0,
              "logprobs": null,
              "message": {
                "content": "1. Orange\n2. Tomato\n3. Banana\n4. Broccoli\n5. Eggplant\n6. Zucchini\n7. Red bell pepper\n8. Baguette\n9. Cheese\n10. Meat in packaging\n11. Leeks\n12. Lemon",
                "refusal": null,
                "role": "assistant"
              }
            }
          ],
          "created": 1731919858,
          "id": "chatcmpl-AUrlCzukojW6gpv8RjsDikt1ZRqdb",
          "model": "gpt-4-turbo-2024-04-09",
          "object": "chat.completion",
          "system_fingerprint": null,
          "usage": {
            "completion_tokens": 57,
            "completion_tokens_details": {
              "reasoning_tokens": 0
            },
            "prompt_tokens": 1135,
            "prompt_tokens_details": {
              "cached_tokens": 0
            },
            "total_tokens": 1192
          }
        }
    """.trimIndent()

    println("[TEST] Testing parsing logic...")
    println("[DEBUG] Mock JSON Response: $jsonResponse")

    // Function to parse ingredients
    fun parseIngredientsUsingSplit(responseBody: String): List<String> {
        val ingredients = mutableListOf<String>()
        try {
            // Parse the response JSON to extract the content string
            val json = JSONObject(responseBody)
            val content = json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")

            // Split by numbers followed by a dot and a space
            val items = content.split(Regex("\\d+\\.")).drop(1) // Drop the first split result if it's empty
            for (item in items) {
                val ingredientName = item.trim() // Trim whitespace
                println("[DEBUG] Parsed ingredient: $ingredientName")
                ingredients.add(ingredientName)
            }
        } catch (e: Exception) {
            println("[ERROR] Unexpected Error: ${e.message}")
        }
        return ingredients
    }


    // Call the parsing function
    val parsedIngredients = parseIngredientsUsingSplit(jsonResponse)

    // Display the results
    if (parsedIngredients.isNotEmpty()) {
        println("[TEST] Parsed Ingredients:")
        parsedIngredients.forEach { ingredient ->
            println(" - $ingredient")
        }
    } else {
        println("[TEST] No ingredients recognized or an error occurred.")
    }
}
