plugins {
    id("lotto.android.library")
    id("lotto.android.hilt")
}

android {
    namespace = "com.enso.data"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:network"))
    implementation(project(":core:di"))

    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit)
}