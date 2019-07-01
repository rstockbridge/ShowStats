# ShowStats

This is an Android app to explore the statistics of shows attended.

## Debug/Release build types

This application has two build types, **debug** and **release**. Below, we describe the setup required to support these build types.

**Note:** we use Crashlytics for crash reporting, Timber for logging, and the OSS Licenses Gradle Plugin. You can force a crash in the debug build type.

### Project

- The directories `src/debug` and `src/release ` hold the respective `google-services.json` files from Firebase.
- The app name has been removed from string resource file. Instead, the app `build.gradle` file specifies separate debug and release names, as well as adding an `applicationIdSuffix` to the debug build type, which makes a unique application ID so we can install the debug and release build types side-by-side.
      
### Firebase

- We have two Firebase projects, debug and release. Each project uses authentication via Google Sign In and the Cloud Firestore. Both projects have the same Firestore security rules, which only give access to authenticated users.
- To add each build type to the respective Firebase project, we had to provide the application ID and SHA(s). For debug, the only SHA is from the local debug key. For release, we had to provide SHAs from the local release key and the Google Play (re-signed) release key.
   
### API Keys

- To be able to use Google Sign In and Maps SDK for Android, we created one API key for each build type in the Google Cloud Platform Console. The manifest specifies a string placeholder for the key, and the appropriate key for each build type is specified in the app `build.gradle` file. 
- We restricted access to each key by selecting Android apps only and by adding the appropriate IDs and SHAs (same as above). We also restricted the APIs to Maps SDK for Android and Identity Toolkit API.	

### Proguard

- We added `minifyEnabled true` to both build types in the app `build.gradle` file. This is necessary for obfuscation in the release build, but not absolutely necessary in the debug build if the app is small.
- We created the additional file `proguard-rules-debug.pro` to prevent obfuscation in the debug build.
	
## Build Instructions	
	
### Debug	

Run `./gradlew installDebug`.

### Release

#### Keystore/Signing Key

- We used the keytool to generate a keystore that contains a single key, where that key was configured appropriately for signing Android applications (we copied the algorithm and complexity from the Android developers website). The alias, password, and keystore are backed up securely in 1Password.
- Create a `keystores` folder in your home directory and add `showstats_upload.jks`.
- Edit the root `gradle.properties` file to specify the alias and password. This prevents these values from being accessed.
- Update the app `build.gradle` file: add

    ```
    signingConfigs {
        release {
            String releaseKeyStoreFileName = System.properties['user.home'] + "/keystores/showstats_upload.jks"

            if (file(releaseKeyStoreFileName).exists()) {
                storeFile file(releaseKeyStoreFileName)
                storePassword showstatsUploadPassword
                keyAlias showstatsUploadAlias
                keyPassword showstatsUploadPassword
            }
        }
    }
    ```
 and `signingConfig signingConfigs.release` to the `buildTypes { release { }` block.
 
#### Generating Local Build

Run `./gradlew installRelease`.

#### Generating Relase Build
Run `./gradlew bundleRelease` to generate an Android app bundle (`.aab`) in `apps/build/outputs/bundle/release`.

#### Releasing to the Play Store
- Upload the `.aab` file to the Play Storee.
- Google Play Console gives us the SHA for the re-signed release version of the app. Add the SHA to the Firebase release project and the API key in the Google Cloud Platform Console.
