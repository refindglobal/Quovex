import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.google.services)
  alias(libs.plugins.ksp)
  alias(libs.plugins.hilt.android)
}

android {
  namespace = "com.quovex"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.quovex"
    minSdk = 26
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    // Load secrets.properties for API keys and OAuth client IDs
    val secretsFile = rootProject.file("../secrets.properties")
    val secrets = Properties().apply {
      if (secretsFile.exists()) load(secretsFile.inputStream())
    }
    buildConfigField(
      "String",
      "GOOGLE_WEB_CLIENT_ID",
      "\"${secrets.getProperty("GOOGLE_WEB_CLIENT_ID", "")}\""
    )
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  sourceSets {
    getByName("test") {
      java.srcDirs("src/test/java")
    }
  }
}

kotlin {
  jvmToolchain(17)
}

ksp {
  arg("room.generateKotlin", "true")
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.activity.compose)

  // Arch Components
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  // Compose
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material.icons.extended)
  debugImplementation(libs.androidx.compose.ui.tooling)
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  // Local tests
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.mockk)

  // Instrumented tests
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.espresso.core)

  // Navigation & Animation
  implementation(libs.androidx.navigation.compose)
  implementation(libs.lottie.compose)

  // Firebase
  val firebaseBom = platform(libs.firebase.bom)
  implementation(firebaseBom)
  implementation(libs.firebase.analytics)
  implementation(libs.firebase.auth)
  implementation(libs.firebase.firestore)
  implementation(libs.firebase.messaging)
  implementation(libs.firebase.storage)

  // Google Sign-In via Credential Manager
  implementation(libs.google.credentials)
  implementation(libs.google.credentials.play)
  implementation(libs.googleid)

  // Image loading & ML Kit OCR (retained for ImageDoubt feature)
  implementation(libs.coil.compose)
  implementation(libs.mlkit.text.recognition)

  // In-app PDF reader — AndroidPdfViewer renders; PDFBox extracts native text layer & geometry
  implementation(libs.android.pdf.viewer)
  implementation(libs.pdfbox.android)

  // ML Kit Face Detection & CameraX for AI Focus & Drowsiness Tracking
  implementation(libs.mlkit.face.detection)
  implementation(libs.androidx.camera.core)
  implementation(libs.androidx.camera.camera2)
  implementation(libs.androidx.camera.lifecycle)
  implementation(libs.androidx.camera.view)

  // ML Kit Document Scanner — BETA (16.0.0-beta1)
  // Provides: auto edge detection, perspective correction, multi-page, crop, rotate
  // No OCR dependency — scanner is image-based only
  implementation(libs.mlkit.document.scanner)

  // Room DB
  implementation(libs.room.runtime)
  implementation(libs.room.ktx)
  ksp(libs.room.compiler)

  // Retrofit & Networking
  implementation(libs.retrofit.core)
  implementation(libs.retrofit.converter.gson)
  implementation(libs.okhttp.core)
  implementation(libs.okhttp.logging)
  implementation(libs.gson)

  // Hilt Dependency Injection
  implementation(libs.hilt.android)
  ksp(libs.hilt.compiler)
  implementation(libs.androidx.hilt.navigation.compose)

  // WorkManager (Morning Briefing & Background Jobs)
  implementation(libs.work.runtime.ktx)

  // Google Play Billing v6
  implementation(libs.play.billing)

  // Google Mobile Ads (AdMob)
  implementation(libs.play.services.ads)
}
