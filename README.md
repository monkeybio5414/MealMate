
# MealMate - Personalized Meal Planning App

MealMate is an innovative meal planning application designed to streamline meal preparation, reduce food waste, and promote healthy eating habits. By leveraging cutting-edge image recognition technology, MealMate identifies ingredients from user-uploaded photos and generates tailored recipes and meal plans that cater to individual dietary preferences and restrictions.

## Table of Contents

1. [Features](#features)
2. [Installation](#installation)
3. [Usage](#usage)
4. [Important Note](#important-note)
5. [Architecture](#architecture)
6. [Technologies Used](#technologies-used)
7. [Testing](#testing)
8. [Contributing](#contributing)
9. [License](#license)

## Features

- **Ingredient Recognition:** Identify and catalog ingredients from photos using advanced image recognition.
- **Personalized Meal Plans:** Generate recipes and meal plans tailored to dietary preferences, restrictions, and health goals.
- **Community Forum:** Share recipes, tips, and meal ideas with a collaborative user community.
- **Dietary Customization:** Support for vegan, gluten-free, and allergy-specific meal suggestions.
- **Real-Time Updates:** Interactive UI with real-time data synchronization using Firebase.
- **Offline Mode:** Basic functionality available even without internet access.

## Installation

### Prerequisites

- Android Studio installed on your computer.
- Android device or emulator for testing.

### Steps

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/MealMate.git
   ```
2. Open the project in Android Studio.
3. Sync the project with Gradle files.
4. Build and run the app on an emulator or connected Android device.

## Usage

1. Launch the app on your device.
2. Sign up or log in to access personalized features.
3. Use the camera feature to upload ingredient photos.
4. Explore meal plans and recipes generated based on your preferences.
5. Join the community forum to share and discover new recipes and tips.

## Important Note

⚠️ **Do not use Eduroam school Wi-Fi while using MealMate.**  
Eduroam networks may restrict certain APIs or block connections required for the app's functionality. For the best experience, use a private or alternative Wi-Fi network.

## Architecture

MealMate follows the **Model-View-ViewModel (MVVM)** architectural pattern to ensure scalability, maintainability, and testability.

- **Model Layer:** Manages data retrieval, storage, and business logic.
- **ViewModel Layer:** Acts as a bridge between the Model and View layers, handling UI-related data and logic.
- **View Layer:** Displays data and manages user interactions.

![MVVM Architecture](path-to-your-architecture-diagram)

## Technologies Used

- **Programming Language:** Kotlin
- **Architecture:** MVVM
- **Libraries:** 
  - CameraX (Image recognition and camera integration)
  - Firebase (Authentication and data synchronization)
  - Jetpack Compose (UI components and navigation)
  - Kotlin Coroutines (Asynchronous processing)
- **APIs:** ChatGPT Vision API for ingredient recognition

## Testing

MealMate employs a robust testing framework:

- **Instrumented Tests:** Validate UI interactions in emulator environments.
- **Mock Tests:** Simulate external dependencies to verify component interactions.
- **Unit Tests:** Test individual components for functionality and reliability.

Run the tests using Android Studio’s testing tools.

## Contributing

We welcome contributions! To contribute:

1. Fork the repository.
2. Create a feature branch:
   ```bash
   git checkout -b feature-name
   ```
3. Commit your changes:
   ```bash
   git commit -m "Add feature description"
   ```
4. Push the branch:
   ```bash
   git push origin feature-name
   ```
5. Open a pull request on GitHub.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
