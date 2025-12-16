import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.enso.lotto_assist.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.android.tools.common)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "lotto.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "lotto.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidHilt") {
            id = "lotto.android.hilt"
            implementationClass = "AndroidHiltConventionPlugin"
        }
        register("jvmLibrary") {
            id = "lotto.jvm.library"
            implementationClass = "JvmLibraryConventionPlugin"
        }
    }
}
