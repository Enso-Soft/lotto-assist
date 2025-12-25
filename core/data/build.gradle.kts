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
    implementation(project(":core:database"))
    implementation(project(":core:di"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)

    // Test
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
}