
plugins {
    `kotlin-dsl`
}

println("buildSrc config...")

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

//dependencies {
//    implementation("org.jetbrains:annotations:17.0.0")
//}