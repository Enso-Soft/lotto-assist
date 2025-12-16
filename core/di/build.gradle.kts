plugins {
    id("lotto.android.library")
    id("lotto.android.hilt")
}

android {
    namespace = "com.enso.di"
}

dependencies {
    implementation(project(":core:network"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}