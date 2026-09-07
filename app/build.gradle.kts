import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.secrets)
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.example.kvcgpl"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
    create("debugConfig") {
      val localProperties = Properties()
      val localPropertiesFile = rootProject.file("local.properties")
      if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { stream ->
          localProperties.load(stream)
        }
      }

      val debugStorePassword = System.getenv("DEBUG_STORE_PASSWORD")
        ?: localProperties.getProperty("DEBUG_STORE_PASSWORD")
      val debugKeyPassword = System.getenv("DEBUG_KEY_PASSWORD")
        ?: localProperties.getProperty("DEBUG_KEY_PASSWORD")
      val debugKeyAlias = System.getenv("DEBUG_KEY_ALIAS")
        ?: localProperties.getProperty("DEBUG_KEY_ALIAS")
        ?: "androiddebugkey"

      storeFile = file("${rootDir}/debug.keystore")
      storePassword = debugStorePassword
      keyAlias = debugKeyAlias
      keyPassword = debugKeyPassword
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      val debugConfig = signingConfigs.getByName("debugConfig")
      if (debugConfig.storePassword != null && debugConfig.keyPassword != null) {
        signingConfig = debugConfig
      }
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
  packaging {
    resources {
      excludes += "/reference/**"
      excludes += "/blueprint/**"
      excludes += "/receipts/**"
      excludes += "/**/*.md"
    }
  }
  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
  ignoreList.add("FIREBASE_APPCHECK_DEBUG_TOKEN")
}

// Unused dependencies are commented out to optimize memory & APK size
dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.recyclerview)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
}
