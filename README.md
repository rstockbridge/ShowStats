# ShowStats

This is an Android app to explore the statistics of shows attended.

## Generating Release Bundles

1. Create a `keystores` folder in your home directory.
2. Obtain `showstats_upload.jks` from LastPass and place it in `keystores`.
3. Edit the root `gradle.properties` file to include the following lines, with the alias and password obtained from LastPass:  
    ```
    showstatsUploadAlias=put-alias-here 
    showstatsUploadPassword=put-password-here
    ```
4. Run `./gradlew bundleRelease` to generate the release bundle.