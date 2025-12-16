plugins {
    id("lotto.android.application")
    id("lotto.android.hilt")
}

android {
    namespace = "com.enso.lotto_assist"

    defaultConfig {
        applicationId = "com.enso.lotto_assist"
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(project(":feature:home"))
    implementation(project(":core:data"))
    implementation(project(":core:domain"))
    implementation(project(":core:network"))
    implementation(project(":core:util"))
    implementation(project(":core:di"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}