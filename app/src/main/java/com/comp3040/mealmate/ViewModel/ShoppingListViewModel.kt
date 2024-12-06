import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.comp3040.mealmate.Model.ShoppingListItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel to manage the shopping list functionality.
 * It interacts with Firebase Realtime Database to fetch, add, remove, update, and clear items.
 */
class ShoppingListViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * The local state of the shopping list, stored in a mutable state list for Compose recomposition.
     */
    val shoppingList = mutableStateListOf<ShoppingListItem>()

    /**
     * Retrieves the current user's ID from Firebase Authentication.
     *
     * @return The user ID if logged in, otherwise `null`.
     */
    private fun getUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    /**
     * Fetches the shopping list from Firebase Realtime Database and updates the local list.
     */
    fun fetchShoppingList() {
        val userId = getUserId()
        if (userId == null) {
            Log.e("ShoppingListViewModel", "User not logged in.")
            return
        }

        val databaseReference = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("shoppingList")

        databaseReference.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val list = snapshot.children.mapNotNull {
                    val item = it.getValue(ShoppingListItem::class.java)
                    item?.copy(id = it.key ?: "") // Populate the id field
                }
                shoppingList.clear()
                shoppingList.addAll(list)
                Log.d("ShoppingListViewModel", "Shopping List fetched: $list")
            } else {
                Log.d("ShoppingListViewModel", "No Shopping List found.")
            }
        }.addOnFailureListener { exception ->
            Log.e("ShoppingListViewModel", "Error fetching Shopping List: ${exception.message}")
        }
    }

    /**
     * Adds a new item to the shopping list in Firebase and updates the local list.
     *
     * @param item The item to add to the shopping list.
     */
    fun addItemToShoppingList(item: ShoppingListItem) {
        val userId = getUserId()
        if (userId == null) {
            Log.e("ShoppingListViewModel", "User not logged in.")
            return
        }

        val databaseReference = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("shoppingList")
        val itemId = databaseReference.push().key ?: return // Generate a unique id

        val newItem = item.copy(id = itemId) // Add the id to the item

        viewModelScope.launch(Dispatchers.IO) {
            databaseReference.child(itemId).setValue(newItem).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    shoppingList.add(newItem)
                    Log.d("ShoppingListViewModel", "Item added to Shopping List: $newItem")
                } else {
                    Log.e("ShoppingListViewModel", "Failed to add item to Shopping List.")
                }
            }
        }
    }

    /**
     * Removes an item from the shopping list in Firebase and updates the local list.
     *
     * @param item The item to remove.
     */
    fun removeItemFromShoppingList(item: ShoppingListItem) {
        val userId = getUserId()
        if (userId == null) {
            Log.e("ShoppingListViewModel", "User not logged in.")
            return
        }

        val databaseReference = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("shoppingList")

        viewModelScope.launch(Dispatchers.IO) {
            if (item.id.isNotEmpty()) {
                databaseReference.child(item.id).removeValue().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        shoppingList.remove(item)
                        Log.d("ShoppingListViewModel", "Item removed from Shopping List: $item")
                    } else {
                        Log.e("ShoppingListViewModel", "Failed to remove item from Shopping List.")
                    }
                }
            } else {
                Log.e("ShoppingListViewModel", "Item id is empty, cannot remove item.")
            }
        }
    }

    /**
     * Toggles the `isChecked` status of an item in the shopping list and updates Firebase.
     *
     * @param item The item to toggle.
     */
    fun toggleItemChecked(item: ShoppingListItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedItem = item.copy(isChecked = !item.isChecked)
            val index = shoppingList.indexOf(item)

            if (index != -1) {
                shoppingList[index] = updatedItem // Update the list locally

                // Update the database
                val userId = getUserId()
                if (userId != null) {
                    val databaseReference = FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(userId)
                        .child("shoppingList")
                        .child(item.id)

                    databaseReference.setValue(updatedItem).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("ShoppingListViewModel", "Item ${item.itemName} updated successfully.")
                        } else {
                            Log.e("ShoppingListViewModel", "Failed to update item ${item.itemName}.")
                        }
                    }
                } else {
                    Log.e("ShoppingListViewModel", "User not logged in, unable to toggle item.")
                }
            }
        }
    }

    /**
     * Clears the entire shopping list in Firebase and updates the local list.
     */
    fun clearShoppingList() {
        val userId = getUserId()
        if (userId == null) {
            Log.e("ShoppingListViewModel", "User not logged in.")
            return
        }

        val databaseReference = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("shoppingList")

        viewModelScope.launch(Dispatchers.IO) {
            databaseReference.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    shoppingList.clear() // Clear the local list
                    Log.d("ShoppingListViewModel", "Shopping list cleared successfully.")
                } else {
                    Log.e("ShoppingListViewModel", "Failed to clear shopping list.")
                }
            }.addOnFailureListener { exception ->
                Log.e("ShoppingListViewModel", "Error clearing shopping list: ${exception.message}")
            }
        }
    }
}
