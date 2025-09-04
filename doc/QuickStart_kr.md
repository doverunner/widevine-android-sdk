## **DoveRunner Widevine SDK** for Android 개발 가이드

-------
## **지원 환경**

- 안드로이드 5.0 (Lollipop) 이상
- 본 SDK는 Gradle 8.9.1, Android Studio Chipmunk 에서 테스트 되었으며, 에뮬레이터에서는 동작하지 않습니다.
- 본 SDK는 Media3 1.5.1 버전을 지원합니다. (기타 버전은 문의 바랍니다.)
	- Exoplayer 2.11 이하 버전은 DrWVSDK v1.15.0 version 으로 사용해야 합니다.
	- Exoplayer 2.16 이하 버전은 DrWVSDK v2.x.x version 으로 사용해야 합니다.
	- Exoplayer 2.18.1 이하 버전은 DrWVSDK v4.3.2 version 으로 사용해야 합니다.
	-
## **확인 사항**

- SDK를 이용한 개발을 위해서는 우선 DoveRunner Admin Site에 가입하여 Site ID와 Site Key를 발급받아야 합니다.

## **빠른 시작**

다음 과정으로 DoveRunner Widevine SDK를 개발 프로젝트에 추가할 수 있습니다.

* SDK는 DoveRunner GitHub Packages에서 제공됩니다.

* GitHub ID와 Git을 이용할 때 비밀번호 대신 사용할 access Token을 발급받습니다.
	* https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens

* 샘플의 `settings.gradle` 파일에서 github 패키지 값을 수정합니다.
	* `settings.gradle` 파일의 `dependencyResolutionManagement` 섹션에 다음 코드 스니펫을 추가하여 Widevine Android SDK GitHub 리포지토리를 포함시킵니다.

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

* build.gradle (module)에 다음 사항을 반영합니다.

    ```gradle
	plugins {
        id 'kotlin-parcelize'
    }
 
    android {
        defaultConfig {
            minSdkVersion 21
            targetSdkVersion 35
            multiDexEnabled true
        }
	
        compileOptions {
            sourceCompatibility JavaVersion.VERSION_1_8
            targetCompatibility JavaVersion.VERSION_1_8
        }
    }
	
    dependencies {
        implementation 'androidx.core:core-ktx:1.8.0'
        implementation 'com.google.android.material:material:1.6.1'
        implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4'
        implementation 'androidx.appcompat:appcompat:1.4.2'
        implementation 'androidx.recyclerview:recyclerview:1.2.1'
        implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
        implementation 'androidx.navigation:navigation-fragment-ktx:2.5.3'
        implementation 'androidx.navigation:navigation-ui-ktx:2.5.3'

        implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1'

        // Exo
        implementation "androidx.media3:media3-exoplayer:1.1.1"
        implementation "androidx.media3:media3-ui:1.1.1"
        implementation "androidx.media3:media3-exoplayer-dash:1.1.1"
        implementation "androidx.media3:media3-exoplayer-hls:1.1.1"
        implementation "androidx.media3:media3-exoplayer-rtsp:1.1.1"
        implementation "androidx.media3:media3-exoplayer-smoothstreaming:1.1.1"
        implementation "androidx.media3:media3-datasource-okhttp:1.1.1"
        implementation "androidx.media3:media3-cast:1.1.1"

        // Gson
        implementation 'com.google.code.gson:gson:2.9.1'

        // Secure
        implementation "androidx.security:security-crypto-ktx:1.1.0-alpha03"
    }
    ```

* MainActivity에 WvEventListener 구현합니다. (샘플 소스 참조)

  ```kotlin
  val wvEventListener: WvEventListener = object : WvEventListener {
      override fun onCompleted(contentData: ContentData) {
          // 다운로드가 완료 되었을때 호출: API Guide 문서를 참고해 주십시오.
      }
  
      override fun onProgress(contentData: ContentData, percent: Float, downloadedBytes: Long) {
          // 다운로드가 시작하고 끝날때까지 호출: API Guide 문서를 참고해 주십시오.
      }
  
      override fun onStopped(contentData: ContentData) {
          // 다운로드가 정지 되었을때 호출: API Guide 문서를 참고해 주십시오.
      }
  
      override fun onRestarting(contentData: ContentData) {
          // 다운로드가 중단된 콘텐츠가 다시 시작 되었을때 호출: API Guide 문서를 참고해 주십시오.
      }
  
      override fun onRemoved(contentData: ContentData) {
          // 다운로드된 콘텐츠가 제거되었을때 호출: API Guide 문서를 참고해 주십시오.
      }
  
      override fun onPaused(contentData: ContentData) {
          // 다운로드 중 pause 되었을 때 호출: API Guide 문서를 참고해 주십시오.
      }
  
      override fun onFailed(contentData: ContentData, e: WvException?) {
          // 다운로드 및 라이선스 오류: API Guide 문서를 참고해 주십시오.
      }
  
      override fun onFailed(contentData: ContentData, e: WvLicenseServerException?) {
          // 라이선스 오류: API Guide 문서를 참고해 주십시오.
      }
  }
  ```

* 다운로드할 콘텐츠 정보를 넣어 DrWvSDK 객체를 생성합니다. (샘플 소스 참조)
  DoveRunner Admin Site에서 확인한 Site ID를 설정합니다.

  ```kotlin
  
  // DRM 관련 정보를 입력한다.
  val config = DrmConfigration(
      "site id",
       "site key", // Set to an empty string if you don't know 
      "content token",
       "custom data",
       mutableMapOf(), // custom header
       "cookie"
  )
  
  val data = ContentData(
      contentId = "content id",
      url = "content URL",
      localPath = "Download location where content will be stored",
      drmConfig = config,
      cookie = null
  )
  
  val wvSDK = DrWvSDK.createWvSDK(
      Context, // Context
      data
  )

  DrWvSDK.addWvEventListener(wvEventListener)
  ```

* 다운로드할 콘텐츠의 트랙 정보를 가져옵니다. (샘플 소스 참조)

  ```kotlin
  // 단말기가 네트워크에 연결되어 있어야 합니다.
  // 트랙정보 획득시 자동으로 라이선스도 다운로드 합니다.
  val trackInfo = wvSDK.getContentTrackInfo()
  ```

* 트랙 정보에서 다운로드할 트랙을 선택합니다. (샘플 소스 참조)

  ```kotlin
  // 샘플에선 TrackSelectDialog 를 이용하여 선택합니다.
  trackInfo.video[0].isDownload = true
  trackInfo.audio[0].isDownload = true
  ```

* 콘텐츠가 이미 다운로드되어 있는지 확인 후 다운로드를 실행합니다. (샘플 소스 참조)

  ```kotlin
  val state = wvSDK.getDownloadState()
  if (state != COMPLETED) {
      wvSDK.download(trackInfo)
  }
  ```

* 다운로드된 콘텐츠를 재생하려면 다음 API를 사용하여 MediaItem 또는 MediaSource 를 획득합니다. (샘플 소스 참조)

  ```kotlin
  // use MediaSource or MediaItem
  val mediaSource = wvSDK.getMediaSource()
  val mediaItem = wvSDK.getMediaItem()
  ```

* PlayerActivity.kt에 다음 개발 가이드를 참고해 플레이어를 구현합니다.
  http://google.github.io/ExoPlayer/guide.html
  > Exoplayer에 관한 자세한 정보는 다음 구글 문서를 참고하시기 바랍니다.
  > https://developer.android.com/guide/topics/media/exoplayer.html

* DRM 라이선스의 license duration 와 playback duration 을 획득합니다.

  ```kotlin
  val drmInfo = wvSDK.getDrmInformation()
  val licenseDuration = drmInfo.licenseDuration
  val playbackDuration = drmInfo.playbackDuration
  
  if (licenseDuration <= 0 || playbackDuration <= 0) {
      // DRM License Expired
  }
  ```

* ExoPlayer 를 다음과 같이 설정합니다. (샘플 소스 참조)

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

### **다운로드 서비스 등록**
고객 상황에 맞춰 download service 를 등록하세요.
- 다운로드에 대한 백그라운드 알림을 지원하려면 아래와 같이 다운로드 서비스를 등록하세요.
  ```kotlin
   // DemoDownloadService 는 advanced 샘플을 확인해 주세요.
   wvSDK.setDownloadService(DemoDownloadService::class.java)
   ```

- AndroidManifest.xml에 다운로드 서비스를 등록합니다.
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

### **라이선스 관리**

콘텐츠를 다운로드와 상관없이 라이선스를 다운로드 및 삭제가 가능합니다.

**라이선스 다운로드**

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

**라이선스 삭제**

```kotlin
wvSDK.removeLicense()
```

### **동영상 레코딩 차단**

화면 녹화 앱을 이용한 콘텐츠 유출을 방지하려면, 어플리케이션에 다음 코드를 추가해 캡쳐 기능을 차단해야 합니다.

```kotlin
val view = binding.exoplayerView.videoSurfaceView as SurfaceView
view.setSecure(true)
```

### **콘텐츠 마이그레이션**

widevine sdk 3.0.0 부터는 다운로드 방식이 기존과 달라지기 때문에 기존 widevine sdk 2.x.x 버전대 사용중인 고객은 다운로드 받아져있던 콘텐츠의 마이그레이션이 반드시 필요합니다.
"needsMigrateDownloadedContent" 함수를 이용하여 해당 콘텐츠가 마이그레이션이 필요한지 여부를 확인할 수 있습니다.
마이그레이션 함수는 내부에서 마이그레이션 콘텐츠가 있을 경우에만 동작하기 때문에 여러번 호출하여도 문제가 되지 않으며, 함수의 파라미터값들은 기존 2.x.x 버전에서 사용된 값으로 동일하게 설정해야 합니다.
ContentData 객체 생성시 사용된 localPath 는 기존의 다운로드 받아진 콘텐츠들의 상위 디렉토리로 설정 되면 안됩니다. 따라서 "MigrationLocalPathException" 예외가 발생될 경우 ContentData 객체 생성시 사용된 localPath 값을 수정해야 정상 동작 합니다.

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

만약 마이그레이션이 성공할 경우 2.x.x 버전대 db 는 아래코드처럼 사용자가 직접 삭제 할 수 있습니다.

```kotlin
val isSuccess = wvSDK.removeOldDownloadedContentDB(
	url = "", // remote content URL
	contentId = "", // ID of content
	siteId = "", // inputs Site ID which is issued on DoveRunner service registration
)
```

### **DoveRunner Widevine SDK API**

API별 상세 설명은 SDK zip 파일 내의 doc/index.html 파일을 참고하여 주시기 바랍니다.

