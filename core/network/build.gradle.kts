plugins {
    id("lotto.android.library")
    id("lotto.android.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.enso.network"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Retrofit 사용을 위한 의존성
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.converter.scalars)
    implementation(libs.logging.interceptor)
    implementation(libs.okhttp3.okhttp)
    implementation(libs.retrofit.kotlin.serialization)
}