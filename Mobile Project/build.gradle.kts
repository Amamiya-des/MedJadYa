// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

// บังคับเวอร์ชัน Guava เพื่อแก้ VerifyException ใน AGP
subprojects {
    configurations.all {
        resolutionStrategy {
            force("com.google.guava:guava:33.3.0-jre")
        }
    }
}
