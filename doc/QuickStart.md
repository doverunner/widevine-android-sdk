## Implementation Guide - **DoveRunner Widevine SDK** for Android

-------
## **Requirements**

- Android 6.0 (API level 23) or later
- This SDK has been tested on Gradle 8.12.2, Android Studio Chipmunk and will not work on the simulator.
- This SDK supports Media3 1.8.0 (contact us about other versions.)
    - Exoplayer 2.11 and earlier versions must be used with DrWVSDK v1.15.0.
    - Exoplayer 2.16 and earlier versions must be used with DrWVSDK v2.x.x.
    - ExoPlayer 2.18.1 and later versions must be used with DrWVSDK v4.3.2.

## **Note**

- To develop application using the SDK, you should sign up for DoveRunner Admin Site to get Site ID and Site Key.

## **Quick Start**

You can add the DoveRunner Widevine SDK to your development project by following these steps:

* The SDK is available on DoveRunner GitHub Packages.

* When using GitHub ID and Git, you will receive a access token to use instead of a password.
  * https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens

* Modify the github package values in the `settings.gradle` files of the samples.
  * Add the following code snippet to the `dependencyResolutionManagement` section of your `settings.gradle` file to include the Widevine Android SDK GitHub repository:

     ```gradle
         dependencyResolutionManagement {
             repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
             repositories {
                 mavenLocal()
                 google()
                 mavenCentral()
                 maven {
                     url "https://plugins.gradle.org/m2/"
                 }
                 // GitHub Packages for Widevine SDK
                 maven {
                     name = "GitHubPackages"
                     url = uri("https://maven.pkg.github.com/doverunner/widevine-android-sdk")
                     credentials {
                         username = "Git hub ID"
                         password = "password"
                     }
                 }
             }
         }
     ```
   
* Apply the below configuration in build.gradle (app).
   ```gradle
   plugins {
   id 'kotlin-parcelize'
   }

   android {
   defaultConfig {
   minSdkVersion 23
   targetSdkVersion 36
   multiDexEnabled true
   }

       compileOptions {
           sourceCompatibility JavaVersion.VERSION_1_8
           targetCompatibility JavaVersion.VERSION_1_8
       }
   }

   dependencies {
        implementation 'androidx.core:core-ktx:1.17.0'
        implementation 'com.google.android.material:material:1.13.0'
        implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2'
        implementation 'androidx.appcompat:appcompat:1.7.1'
        implementation 'androidx.recyclerview:recyclerview:1.4.0'
        implementation 'androidx.constraintlayout:constraintlayout:2.2.1'
        implementation 'androidx.navigation:navigation-fragment-ktx:2.9.3'
        implementation 'androidx.navigation:navigation-ui-ktx:2.9.3'

        implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1'

        // Exo
        implementation "androidx.media3:media3-exoplayer:1.8.0"
        implementation "androidx.media3:media3-ui:1.8.0"
        implementation "androidx.media3:media3-exoplayer-dash:1.8.0"
        implementation "androidx.media3:media3-exoplayer-hls:1.8.0"
        implementation "androidx.media3:media3-exoplayer-rtsp:1.8.0"
        implementation "androidx.media3:media3-exoplayer-smoothstreaming:1.8.0"
        implementation "androidx.media3:media3-datasource-okhttp:1.8.0"
        implementation "androidx.media3:media3-cast:1.8.0"

        // Gson
        implementation 'com.google.code.gson:gson:2.13.1'

        // Secure
        implementation "androidx.security:security-crypto-ktx:1.1.0"
   }
   ```
   
* Implement WvEventListener in MainActivity. (Please refer to sample project)
```kotlin
	val wvEventListener: WvEventListener = object : WvEventListener {
	    override fun onCompleted(contentData: ContentData) {
	        // Called when download is complete: Please refer to the API Guide.
	    }
	
	    override fun onProgress(contentData: ContentData, percent: Float, downloadedBytes: Long) {
	        // Call from start to end of download: Please refer to the API Guide.
	    }
	
	    override fun onStopped(contentData: ContentData) {
	        // Called when download is stopped: Refer to the API Guide.
	    }
	
	    override fun onRestarting(contentData: ContentData) {
	        // Called when download is restarting: Refer to the API Guide.
	    }
	
	    override fun onRemoved(contentData: ContentData) {
	        // Called when downloaded content is removed: Refer to the API Guide.
	    }
	
	    override fun onPaused(contentData: ContentData) {
	        // Called when download is pause: Refer to the API Guide.
	    }
	
	    override fun onFailed(contentData: ContentData, e: WvException?) {
	        // Called when an error occurs while downloading content or an error occurs in the license: Refer to the API Guide.
	    }
	
	    override fun onFailed(contentData: ContentData, e: WvLicenseServerException?) {
	        // Called when error sent from server when acquiring license: Refer to the API Guide.
	    }
	}
```
   
* Create a DrWvSDK object with content information to download. Set the Site ID verified in the DoveRunner Admin Site. (Please refer to sample project)

	```kotlin
	// Enter DRM related information.
	val config = DrmConfigration(
	    "site id",
 		"site key", // Set to an empty string if you don't know 
	    "content token",
 		"custom data",
 		mutableMapOf(), // custom header
 		"cookie",
 		"licenseCipherPath", // Set to true if you want to communicate with the server using the DoveRunner License Cipher feature.
  		"drmLicenseUrl", // Set to license server URL if you have one
        "uuid" // Set to an empty string if you don't know
	)
	
    // localFileUrl: content URLs obtained from SDK 2.x.x or stored in external storage
    // If the contentName used during download in version 2.x.x is TestRunner_User, you must set the URL as follows:
    // var file = File(context.getExternalFilesDir(null), "TestRunner_User/stream.mpd")
    // val localUrl = "file://${file.absolutePath}"
    // localFileUrl = localUrl
	val data = ContentData(
	    contentId = "content id",
	    url = "content URL",
	    localFileUrl = "content URLs obtained from SDK 2.x.x or stored in external storage",
	    drmConfig = config,
	    cookie = null,
        httpHeaders = null
	)
	
	val wvSDK = DrWvSDK.createWvSDK(
	    Context, // Context
	    data
	)
	
	DrWvSDK.addWvEventListener(wvEventListener)
	```

* Get the track information of the content to be downloaded. (Please refer to sample project)

	```kotlin
	// The device must be connected to a network.
	// When track information is acquired, the license is also automatically downloaded.
	val trackInfo = wvSDK.getContentTrackInfo()
	```

* Select the track you want to download from the track information. (Please refer to sample project)

	```kotlin
	// In our sample, we use TrackSelectDialog to select.
	trackInfo.video[0].isDownload = true
	trackInfo.audio[0].isDownload = true
	```

* Execute the download after checking if the content has already been downloaded. (Please refer to sample project)

	```kotlin
	val state = wvSDK.getDownloadState()
	if (state != COMPLETED) {
	    wvSDK.download(trackInfo)
	}
	```

* To play downloaded content, obtain a MediaItem or MediaSource using the following API. (Please refer to sample project)

	```kotlin
	// use MediaSource or MediaItem
	val mediaSource = wvSDK.getMediaSource()
	val mediaItem = wvSDK.getMediaItem()
	```

* Implement player in PlayerActivity.java using the below development guide.
    http://google.github.io/ExoPlayer/guide.html
> Please refer to the below guide from Google for more information about Exoplayer.
> https://developer.android.com/guide/topics/media/exoplayer.html

* Obtain license duration and playback duration of DRM license.

	```kotlin
	val drmInfo = wvSDK.getDrmInformation()
	val licenseDuration = drmInfo.licenseDuration
	val playbackDuration = drmInfo.playbackDuration
	
	if (licenseDuration <= 0 || playbackDuration <= 0) {
	    // DRM License Expired
	}
	```

* Set up ExoPlayer as follows. (Please refer to sample project) 

	```kotlin
	ExoPlayer.Builder(this).build()
	    .also { player ->
	        exoPlayer = player
	        binding.exoplayerView.player = player
	        exoPlayer?.setMediaSource(mediaSource) //use mediaSource.
	        exoPlayer?.addListener(object : Player.Listener {
	            override fun onPlayerError(error: PlaybackException) {
	                super.onPlayerError(error)
	            }
	
	            override fun onIsPlayingChanged(isPlaying: Boolean) {
	                super.onIsPlayingChanged(isPlaying)
	            }
	        })
	    }
	```

### **Sign up for download services**
Register a download service based on your customer's situation.
- To support background notifications for downloads, register your download service as shown below.
  ```kotlin
   // DemoDownloadService 는 advanced 샘플을 확인해 주세요.
   wvSDK.setDownloadService(DemoDownloadService::class.java)
   ```

- Register the download service in the AndroidManifest.xml.
  ```xml
  <service
      android:name="com.doverunner.advencedsample.DemoDownloadService"
      android:exported="false">
      <intent-filter>
          <action android:name="com.google.android.exoplayer.downloadService.action.RESTART" />

          <category android:name="android.intent.category.DEFAULT" />
      </intent-filter>
  </service>
  ```

### **Manage licenses**

You can download and delete licenses regardless of whether you download content.

**Download license**

```kotlin
val uri = Uri.parse("content url")
// val dataSource = FileDataSource.Factory() // local file
val okHttpClient = OkHttpClient.Builder().build()
val dataSource = OkHttpDataSource.Factory(okHttpClient) // remote content
val dashManifest =
    DashUtil.loadManifest(dataSource.createDataSource(), uri)
val format = DashUtil.loadFormatWithDrmInitData(
    dataSource.createDataSource(),
    dashManifest.getPeriod(0)

// The format parameter does not need to be entered unless it is a local file.
// If the format value is NULL, it is automatically defined inside the SDK via the REMOTE CONTENT URL.
wvSDK.downloadLicense(format = format, { 
    Toast.makeText(this@MainActivity, "success download license", Toast.LENGTH_SHORT).show()
}, { e ->
    Toast.makeText(this@MainActivity, "${e.message()}", Toast.LENGTH_SHORT).show()
    print(e.msg)
})
```

**Remove license**

```kotlin
wvSDK.removeLicense()
```

### **Block video recording**

To prevent content leaks with screen recording apps, you should block the capture function by adding the following code to your application:

```kotlin
val view = binding.exoplayerView.videoSurfaceView as SurfaceView
view.setSecure(true)
```

### **Migration for past users**

Since the download method is different from widevine sdk 3.0.0, customers who are using the existing widevine sdk 2.x.x version must migrate the downloaded content.
You can use the "needsMigrateDownloadedContent" function to determine if the content needs to be migrated.
Since the migration function operates only when there is migration content inside, it does not matter if it is called multiple times, and the parameter values of the function should be set identically to the values used in the existing 2.x.x version.
The localPath used when creating the ContentData object should not be set as the parent directory of the existing downloaded contents. Therefore, if a "MigrationLocalPathException" exception occurs, the localPath value used when creating the ContentData object must be modified for normal operation.

```kotlin
try {
    if (wvSDK.needsMigrateDownloadedContent(
            url = contents[index].content.url!!,
            contentId = contents[index].cid,
            siteId = contents[index].content.drmConfig!!.siteId!!)
    ) {
        val isSuccess = wvSDK.migrateDownloadedContent(
            url = "", // remote content URL
            contentId = "", // ID of content
            siteId = "", // inputs Site ID which is issued on DoveRunner service registration
            contentName = "", // content's name which will be used for the name of downloaded content's folder
            downloadedFolderName = null // content download folder name
        )
    }
} catch (e: WvException.MigrationException) {
    print(e)
} catch (e: WvException.MigrationLocalPathException) {
    // you have to change localPath
    // ex) val localPath = File(fi, "downloads_v2").toString()
    print(e)
}
```

If the migration is successful, you can delete the 2.x.x version db by yourself like the code below.

```kotlin
val isSuccess = wvSDK.removeOldDownloadedContentDB(
    url = "", // remote content URL
    contentId = "", // ID of content
    siteId = "", // inputs Site ID which is issued on DoveRunner service registration
)
```

### **DoveRunner Widevine SDK API**

Please refer to the doc/en/api_reference.html file of the SDK zip file for detailed explanation of each API.

