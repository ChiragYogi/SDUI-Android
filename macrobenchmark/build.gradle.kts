plugins {
    alias(libs.plugins.android.test)
}

android {
    namespace = "com.chiraggoswami.sduidemo.macrobenchmark"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        // Macrobenchmark's compilation-mode / process-kill APIs want a modern platform.
        minSdk = 28
        targetSdk = 37

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Instruments a build of :app — never itself becomes an installable app.
    targetProjectPath = ":app"
    experimentalProperties["android.experimental.self-instrumenting"] = true

    buildTypes {
        // Matches :app's "benchmark" build type by name. The test APK itself can stay
        // debuggable; it's the target app under instrumentation that must not be.
        create("benchmark") {
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.junit)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
}

androidComponents {
    beforeVariants(selector().all()) {
        // Only the benchmark build type is ever assembled/run — no debug variant to skip building.
        it.enable = it.buildType == "benchmark"
    }
}
