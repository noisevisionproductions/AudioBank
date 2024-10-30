## Project Contributions

- **Code:** [Tomasz - noise](https://github.com/noisevisionproductions/)
- **Design and UI:** [Arkadiusz - aalqz] – Idea creator and UI designer

# Project Title

This is a cross-platform mobile application developed using Kotlin Multiplatform, designed for music producers who use mobile apps in their creative workflows.
The app allows users to upload their own sound creations, explore other users' samples, and download chosen sounds directly to the app, ready for use in their music production setup.
Key features include a sample library, where users can upload, tag, and edit metadata for sound files, and a forum for community engagement, where users can create posts, comment, and discuss various music production topics.

## Features

- **Sample Library**: Upload sound files with metadata (BPM, tone, tags), play them within the app, and organize based on user-defined tags.
- **Forum**: Users can create posts, comment, and reply to other comments in a community-based forum setup.
- **User Authentication**: Firebase Authentication integration to enable secure login and registration.
- **Music Player**: Built-in player to preview uploaded sound files with a customizable playlist.
- **Custom Metadata Management**: Metadata extracted from sound files is stored and managed using Firebase Firestore, allowing easy updates and user-defined metadata.

## Tech Stack

Before Firebase I also tried to implement services like MongoDB and Azure (Cosmos DB, AD B2C, Function App, Storage). While Azure was close to fulfilled my needs, not everything was suitable for Android, especially for KMP.

- **Kotlin Multiplatform (KMP)**: Core framework for cross-platform development (Android, iOS).
- **Firebase**:
  - **Firebase Auth** for user authentication, allowing secure login and registration for the app's users.
  - **Firebase Firestore** for storing user and forum data, including posts, comments, and sound file metadata.
  - **Firebase Storage** for managing uploaded audio files, enabling users to store and access their sounds across devices.
  - **Firebase Functions** to automate metadata extraction: when a user uploads a sound file to Firebase Storage, a Firebase Cloud Function is triggered. This function extracts metadata such as the file name, format, and duration, then saves this data in Firestore. This automated process ensures that all uploaded sounds are cataloged with accurate metadata for easy search and retrieval.
- **Coroutines & StateFlow**: Used for asynchronous operations and state management within ViewModels.
- **Jetpack Compose (Android)**: UI framework for building the app’s interface on Android.
- **Architecture**:
  - **MVVM** pattern used to separate logic and UI.
  - Custom services and repository classes to handle data operations.

## Challenges and Solutions

Throughout the development of this cross-platform music production app, several challenges emerged that required custom solutions to create a seamless user experience.

- **Cross-Platform File Management**  
  **Challenge**: Managing file uploads and metadata across Android and iOS while keeping data consistent and accessible on both platforms.  
  **Solution**: Implemented platform-specific expect/actual classes to handle file picking and storage, allowing seamless integration with Firebase Storage and maintaining a unified experience for both platforms.

- **Data Synchronization and Performance in Firebase**  
  **Challenge**: Ensuring efficient data retrieval and synchronization in Firebase, especially for audio file metadata. The initial implementation provided all metadata but was slow.  
  **Solution**: Developed two versions of the `listFilesWithMetadata` method to balance speed and detail, with one version optimized for rapid loading and another for more comprehensive data display.

- **Complex User Interactions in a Forum System**  
  **Challenge**: Enabling dynamic forum interactions, including nested comments (like in Reddit), like counts, and real-time updates, posed complexities in structuring and updating data across Firestore.  
  **Solution**: Structured Firestore documents with unique IDs and opted for a flat data model with parent/child relationships for comments. This approach simplified retrieval and maintained fast load times even with deep reply chains.

- **User Experience in File Uploads with Metadata**  
  **Challenge**: Allowing users to tag, edit, and preview sound files with custom metadata (e.g., BPM, tone, tags) while keeping the upload process efficient and intuitive.  
  **Solution**: Designed a custom `UploadSoundViewModel` to handle metadata alongside file uploads, making it easy to add, edit, or view metadata within the app. Each file is also prefixed with the username, ensuring easy tracking in shared libraries.

- **Error Handling and Cross-Platform Consistency**  
  **Challenge**: Displaying consistent error messages across Android and iOS platforms in common code, especially since Snackbar and certain UI components aren't available in `commonMain`.  
  **Solution**: Implemented a custom error box to display error messages at the top of the screen in `commonMain`, providing users with instant feedback without platform-specific UI dependencies.

## This is a Kotlin Multiplatform project targeting Android, iOS.

* `/composeApp` is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - `commonMain` is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    `iosMain` would be the right folder for such calls.

* `/iosApp` contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform, 
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.


Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…