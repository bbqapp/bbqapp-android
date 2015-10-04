# bbqapp-android
The Android client implementation of bbqapp

## Installation
* JDK
* Android Studio
* Google USB Driver if necessary (Windows only)
* Git

## Configure Anroid Studio
1. Start Android Studio
2. Click 'Configure' and select 'SDK Manager'
3. Select following SDK Platforms:
  * API Level 15
  * API Level 23
4. Select folowing SDK Tools:
  * Google Play Services
  * Android SDK Build Tools
  * Android SDK Tools
  * Android SDK Platform Tools
5. Click 'OK' to start the download

## Import project
1. Start Android Studio
2. Klick on 'Checkout project from Version Control' and select Github
3. Sign in GitHub
4. Enter/Select https://github.com/bbqapp/bbqapp-android.git and specify target directory
5. Click 'Clone'
6. In 'Import Project from gradle' dialog use default gradle wrapper and click 'OK'

## Configure project
Create new value resource file res/values/credentials.xml with following content:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
  <string name="google_maps_api_key">your_key</string>
</resources>
```
