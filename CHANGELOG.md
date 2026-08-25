# Version 4.6.3

- **What's new**
  - Added `removeLicense(onSuccess, onFailed)` — an asynchronous overload that does not block the calling thread. Callbacks are delivered on the main thread.
  - Added `LicenseNotFoundException` — reported by `removeLicense()` (thrown by the synchronous API, delivered to `onFailed` by the asynchronous one) when there is no license to remove because it was never downloaded or has already been removed. An app retrying a removal (e.g. after its own bookkeeping failed) can treat this case as already completed instead of parsing error messages.

- **Bug fixes**
  - Fixed the license being requested again on every re-prepare (`setMediaItem` + `prepare`) of the same player when using `getDrmSessionManager()` / `getMediaSource()`. With short-lived license tokens this caused `Token is Expired` when playback resumed after the token had expired (e.g. returning from background).
  - Fixed stale DRM session state surviving `remove()` / `removeLicense()`. Playing content after deleting it now always starts from a clean DRM session, so the license server receives a well-formed request again instead of failing to parse one built on the removed license.

- **What's changed**
  - The AAR now ships consumer R8/ProGuard rules, so apps with minification enabled no longer need to add keep rules for the SDK manually.
  - `removeLicense()` no longer retries the server release and caps the request at 5 seconds. The local license keys are always invalidated immediately; notifying the server is best-effort, so the worst-case latency drops from 40+ seconds to a few seconds on a poor network.
  - `removeLicense()` is now idempotent: after a successful removal the removed key set ID is also cleared from local storage, so calling `removeLicense()` again reports `LicenseNotFoundException` deterministically instead of failing inside `MediaDrm` with a stale key set ID.

# Version 4.6.2

- **What’s new**
  - Added `getMediaItem(playbackOptions)` — the force-streaming option introduced in 4.6.0 is now also available on the `MediaItem` path, so player setups built on `getMediaItem()` (e.g. Google IMA ads integration) can force streaming playback over already-downloaded content.

# Version 4.6.1

- **Bug fixes**
  - Fixed the cookies/headers provided in `ContentData` not being included in the segment requests during download and playback.
  - Fixed a crash when the HLS manifest request failed while preparing a download.

# Version 4.6.0

- **What’s new**
  - Added `PlaybackOptions` — an option to force streaming playback even when the content is already downloaded (`getMediaSource(playbackOptions)`).
  - Added `DownloadErrorReason` — distinguishes the failure cause of `remove()`/`removeAll()` (`NOT_FOUND` / `REMOVE_FAILED`).

- **Bug fixes**
  - Fixed `stop()` being ignored when called while the license request was in progress.
  - Fixed `remove()`/`removeAll()` not reporting the failure cause.
  - Fixed the download state not returning from `REMOVING` to `NOT` after `remove()` completed.

# Version 4.5.3

- **What’s changed**
  - Updated AndroidX Media3 dependency from 1.8.0 to 1.10.0.
  - Updated WvMediaDrmCallback for Media3 1.9.0+ breaking change: executeKeyRequest and executeProvisionRequest now return MediaDrmCallback.Response instead of ByteArray.
    Compatibility

- **Compatibilitys**
  - Compatible with apps using Media3 1.10.0+.
  - No changes to public SDK APIs; no app-side migration required.


# Version 4.5.2

- **What’s changed**
  - Updated internally to support drdlc(DRMLicenseCipher) v1.6.1.

- **Bug fixes**
  - Fixed an issue where download failed with a missing permission error on Android 12 or lower due to `FOREGROUND_SERVICE_DATA_SYNC` being checked regard


# Version 4.5.1

- **What’s new**
  - The `mimeType` in `ContentData` class parameter has been added
    - the parameter can enforce the MIME type for media content.
  - Removed `FOREGROUND_SERVICE` and `FOREGROUND_SERVICE_DATA_SYNC` permissions from the SDK’s `AndroidManifest.xml`
    - These permissions were previously declared inside the SDK AAR and automatically merged into the host app’s manifest via Android Manifest Merger, causing Google Play review rejections for apps that do not use the download feature.
    - Starting from this version, apps that use the download feature must declare these permissions explicitly in their own `AndroidManifest.xml`.
    - Apps that do **not** use the download feature are not affected and do not need to declare these permissions.
    - See the **Download Feature Setup** section in the QuickStart guide for details.

# Version 4.5.0

- **What’s new**
  - The `localFileUrl` in `ContentData` class parameter has been added
    - the parameters for playing content downloaded in version 2.x.x
- **What’s changed**
  - Updated minimum Android version
    - Minimum supported Android version: Updated to `6.0.0`
    - Minimum API level: Updated to `23`
  - Updated JVM
    - jvmTarget: Updated to `17`
  - Updated Libraries
    - media3: Updated to `1.8.0`
  - The Groovy DSL has been migrated to Kotlin DSL
    - `build.gradle` has been changed to `build.gradle.kts`
  - The samples have been changed to a version control system using TOML
    - add `libs.versions.toml` in gradle directory for samples
  - DrmConfigration class modifications
    - `authData` is now used instead of `token` or `customData`
  
# Version 4.4.0

- **Changed The product brand name will be changed to DoveRunner:**
  - DoveRunner(Dr) is the new name for PallyCon.
  - We are changing the names of various products related to the existing PallyCon content security service to DOVERUNNER(Dr).
  - During this time, some documents and code on the Docs site may use PallyCon and DOVERUNNER(Dr) interchangeably.
  - The group name for the sdk has been changed to `com.doverunner.widevine`.
  - The class name of the sdk has been changed from `PallyConWvSDK` to `DrWvSDK`.
  - The class name of the `PallyConDrmConfigration` has been changed to `DrmConfigration`.
  - The class name of the `PallyConEventListener` has been changed to `DrEventListener`.
  - The class name of the `PallyConCallback` has been changed to `DrCallback`.
  - The class name of the `PallyConDownloadManager` has been changed to `DrDownloadManager`.
  - The class name of the `PallyConDownloadService` has been changed to `DrDownloadService`.
  - The class name of the `PallyConDownloadTask` has been changed to `DrDownloadTask`.
  - The class name of the `PallyConDownloadRequest` has been changed to `DrDownloadRequest`.
  - The class name of the `PallyConDownloadListener` has been changed to `DrDownloadListener`.
  - The class name of the `PallyConDownloadStatus` has been changed to `DrDownloadStatus`.
  - The class name of the `PallyConDownloadException` has been changed to `DrDownloadException`.
  - The class name of the `PallyConException` has been changed to `WvException`.
  - The function name `createPallyConWvSDK` has been changed to `createWvSDK`.
  - The function name `setPallyConCallback` has been changed to `setWvCallback`.
  - The function name `addPallyConEventListener` has been changed to `addWvEventListener`.
  - The function name `removePallyConEventListener` has been changed to `removeWvListener`.
  
- **Updated `getMediaSource` function:**
    - Updated to also work for Streaming content if you have a persistent license in advance.

# Version 4.3.2

- **Added `ClearKeyLicenseException` exception:**
  - Added an error that occurs when attempting to download a license for clearkey or non-DRM content.

- **Updated `download()` function:**
  - downloads of Clearkey or NonDRM content will proceed even if you don't have a license to download it.

- **Updated `getMediaSource()` and `getMediaItem` function:**
  - When calling the function while downloading, get the media of the content being downloaded.

# Version 4.3.1

- **Updated Libraries:**
    - media3: Updated to `1.4.1`

# Version 4.3.0

- **Support for license policy 2.0:**
    - The SDK now processes license data according to policy 2.0 specifications.
    - `setPallyConCallback()`, `setDownloadService()` and `getDownloadManager()` have been changed to static functions.
    - Use `PallyConWvSDK.setPallyConCallback()` from now on.
    - The old `setPallyConCallback()` and `setDownloadService()` functions are deprecated.

- **Event listener functions added:**
    - `addPallyConEventListener()` and `removePallyConEventListener()` functions are now available.
    - You can register and remove `PallyConEventListener`.
    - The old `setPallyConEventListener()` function is deprecated.

- **New setCmcdConfigurationFactory() function:**
    - Configure Common Media Client Data (CMCD) for your CDN real-time logs.

- **Updated `PallyConCallback` interface:**
    - The `executeKeyRequest` function parameter has been changed from `url` to `contentData`.

- **Updated Libraries:**
    - core: Updated to `1.13.1`
    - appCompat: Updated to `1.7.0`
    - material: Updated to `1.12.0`
    - coroutines: Updated to `1.8.1`
    - media3: Updated to `1.3.1`
    - gson: Updated to `2.11.0`
    - security: Updated to `1.1.0-alpha06`

# Version 4.2.0

- **New stop() function added:**
  - Content downloads can now be interrupted.
  - Interrupted downloads can be resumed later.

- **Changes to PallyConEventListener event listeners:**
  - The `contentUrl` parameter has been replaced with `ContentData(contentId, url, ..., drmConfig)`.

- **ContentData class modifications:**
  - The `localPath` parameter has been removed.
  - Added `setDownloadDirectory()` static function to set the download directory.
  - Separate directories for each content are no longer supported (this matches the behavior of the old code).

# Version 4.1.0

>- PallyConSDK has been updated to version 4.1.0.
>  - Fixed redownload not happening if remove after pause

# Version 4.0.1

>- PallyConSDK has been updated to version 4.0.1.
   >  - Added a "getDrmSessionManager" function.
   >  - Added "getMediaSource" function that utilizes the drmSessionManager parameter.
   >    -  You can specify a drmSessionManager to create the mediaSource object.
   >  - Fixed an issue that caused the download() function to crash when running in the main thread from now on.
   >    - A function runs as a background thread under the hood.
   >  - Fixed crash when selecting more than one video track

# Version 4.0.0

>- PallyConSDK has been updated to version 4.0.0
   >  - Changed the ExoPlayer Package -> Media3 Package

# Version 3.4.6

>- PallyConSDK has been updated to version 3.4.6.
   >  - Fixed an issue where the license key rotation feature for live content was not working correctly.

# Version 3.4.5

>- PallyConSDK has been updated to version 3.4.5.
   >  - Added a PallyConLicenseCipherException.

# Version 3.4.3

>- PallyConSDK has been updated to version 3.4.3.
   >  - Fixed an internal error.

# Version 3.4.2

>- PallyConSDK has been updated to version 3.4.2.
   >  - Starting with 3.4.2, DB migration between 2.X.X versions is not supported.
>  - Removed "removeOldDownloadedContentDB" function

# Version 3.4.1

>- PallyConSDK has been updated to version 3.4.1.
   >  - Fixed the LicenseCipher feature part.

# Version 3.4.0

> - PallyConSDK has been updated to version 3.4.0. 
>   - Added contentId member variable to "ContentData" class.
>     - For content management, the contentId used in the 2.x.x version range has been added back..
>       If you don't enter a contentId, the content will be managed by url. 
>   - We've changed how we handle the migration of historical content.
>     - Added "needsMigrateDownloadedContent" function to check whether the content needs to be migrated or not. If that function returns true, you can run the "migrateDownloadedContent" function.
>     - Added "removeOldDownloadedContentDB" function can remove the DB of content that was downloaded before the SDK 2.x.x version. 
>   - Added a format parameter to the "downloadLicense" function. 
>     - If it's null, the format value is taken directly from the content url(remote content) internally to download the license.
>   - Added a "setDownloadService" and "getDownloadManager" functions.
>     - Can now turn off the default applied alarm and use the DownloadService set by the customer.
>     - Can add their own using the setDownloadService function.
>     - Additionally, the DownloadManager object needed to create the DownloadService can now be retrieved using the getDownloadManager function.
>   - Added licenseCipherPath member variable to "PallyConDrmConfigration" class
>     - This is a member variable added for customers using PallyCon LicenseCipher and has a default value of null.
>     - For more information, please contact us at PallyCon.

# Version 3.3.0

> - PallyConSDK has been updated to version 3.3.0.
>   - Added "setPallyConCallback" function and "PallyConCallback" interface
>     - You can handle the communication part with the server when getting the license as a callback.
>     - In the callback, you can further proceed with things like encrypting the data. 
>   - Modified HLS download.

# Version 3.2.0

> - PallyConSDK has been updated to version 3.2.0. 
>   - Added download and play for HLS(m3u8) widevine contents 
>   - Added download for Non-DRM contents

# Version 3.1.0

> - PallyConSDK has been updated to version 3.1.0. 
>   - Added migration function "migrate Downloaded Content" for users existing 2.x versions. 
>   - Changed "createPallyConWvSDK" function. instead of entering a PallyConEventListener object as a "createPallyConWvSDK" parameter, set it using the "setPallyConEventListener" function. 
>   - Existing 2.x.x customers must call in advance. 

# Version 3.0.1

> - PallyConSDK has been updated to version 3.0.1. 
>   - Bug fix, Crash occurs when the getContentTrackInfo() function is called multiple times while offline

# Version 3.0.0

> - PallyConSDK has been updated to version 3.0.0. 
>   - PallyConSDK 3.0.0 is based on Kotlin and can be used in java. 
>   - All of the APIs used in the previous 2.x.x version have been changed. 
>   - From now on, background multi-download (max 6) is supported.
