plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.task1"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.task1"
        minSdk = 21
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// ─────────────────────────────────────────────
//  CUSTOM CHECKSTYLE TASK FOR ANDROID
// ─────────────────────────────────────────────
tasks.register<Checkstyle>("checkstyleMain") {
    // 1. Define the source files to check
    source = fileTree("src/main/java") {
        include("**/*.java")
    }

    // 2. Set the classpath (can be empty for basic checks)
    classpath = files()

    // 3. Point to your Checkstyle configuration file
    configFile = rootProject.file("config/checkstyle/checkstyle.xml")

    // 4. Provide the Checkstyle library (this fixes 'checkstyleClasspath')
    // The plugin should auto-resolve this from the toolVersion, but we make it explicit.
    checkstyleClasspath = configurations.getByName("checkstyle")

    // 5. Set the config directory (this fixes 'configDirectory')
    configDirectory.set(layout.projectDirectory.dir("config/checkstyle"))

    // 6. Fail the build if violations are found
    isIgnoreFailures = false
    maxWarnings = 0
    maxErrors = 0

    // 7. Configure report outputs (this fixes 'outputLocation' errors)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

dependencies {
    // Required for the Checkstyle tool library
    checkstyle("com.puppycrawl.tools:checkstyle:10.12.4")
    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)

    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.3")
    implementation("androidx.lifecycle:lifecycle-livedata:2.8.3")
}