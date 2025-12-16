plugins {
    id("lotto.android.library")
    id("lotto.android.hilt")
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.enso.database"
}

dependencies {
    implementation(project(":core:domain"))

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}