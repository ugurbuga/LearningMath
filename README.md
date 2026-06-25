# LearningMath

LearningMath is a fun and interactive educational application built with **Compose Multiplatform**, designed to help children master basic mathematical operations through engaging gameplay and guided learning.

## 🚀 Features

-   **Comprehensive Math Operations:** Practice Addition (+), Subtraction (-), Multiplication (×), and Division (÷).
-   **Intelligent Input Modes:**
    -   **Direct Mode:** For quick and confident answering.
    -   **Step-by-Step Mode:** Guided input that mirrors traditional paper-and-pencil methods (e.g., carrying over in addition or borrowing in subtraction).
-   **Interactive Solution Solver:** Stuck on a problem? The app provides a detailed, step-by-step animated walkthrough to explain the logic behind the solution.
-   **Progress Tracking:** A dedicated Statistics Dashboard shows daily achievements and historical performance data for each operation type.
-   **Gamified Experience:** Features colorful Material 3 UI, exciting confetti animations (via ConfettiKit), and encouraging feedback to keep children motivated.
-   **Shared Codebase:** Leverages Kotlin Multiplatform to share core logic and UI components across Android and potential other platforms.

## 🛠 Tech Stack

-   **Language:** [Kotlin](https://kotlinlang.org/)
-   **UI Framework:** [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
-   **Architecture:** Clean Architecture with shared components.
-   **UI Components:** Material 3
-   **Animations:** [ConfettiKit](https://github.com/vinceglb/ConfettiKit)
-   **Data:** Kotlinx Serialization & Kotlinx Datetime
-   **Concurrency:** Kotlin Coroutines

## 📱 Screenshots

| Selection Screen | Game Play | Solution Walkthrough | Stats Dashboard |
| :---: | :---: | :---: | :---: |
| ![Selection](https://via.placeholder.com/200x400?text=Selection+Screen) | ![Gameplay](https://via.placeholder.com/200x400?text=Gameplay) | ![Solution](https://via.placeholder.com/200x400?text=Solution) | ![Stats](https://via.placeholder.com/200x400?text=Stats) |

## 🏁 Getting Started

### Prerequisites

-   **Android Studio Ladybug** (or newer)
-   **JDK 17**

### Setup

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/ugurbuga/MathMonster.git
    ```
2.  **Open in Android Studio:**
    Open the root folder and wait for the Gradle sync to finish.
3.  **Run the App:**
    Select the `app` configuration and run it on your Android emulator or physical device.

## 📂 Project Structure

-   `shared`: The heart of the project. Contains all shared UI (Compose), ViewModels/Logic, and resources (Strings, Colors, etc.).
-   `app`: Android-specific module containing the main activity and Android configuration.

## 📝 License

This project is open-source and available under the [MIT License](LICENSE).

---
Developed with ❤️ by [Uğur Buğa](https://github.com/ugurbuga)
