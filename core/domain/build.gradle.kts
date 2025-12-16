plugins {
    id("lotto.jvm.library")
}

dependencies {
    implementation("javax.inject:javax.inject:1")
    implementation(libs.kotlinx.coroutines.core)
}