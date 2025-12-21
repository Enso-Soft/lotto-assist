plugins {
    id("lotto.jvm.library")
}

dependencies {
    implementation("javax.inject:javax.inject:1")
    implementation(libs.kotlinx.coroutines.core)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}