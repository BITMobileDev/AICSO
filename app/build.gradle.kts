plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.protobuf)


}

android {
    namespace = "com.aicso"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.aicso"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "BASE_URL", value="\"https://aicso-dev-backend-ca.bluegrass-88201ab2.canadacentral.azurecontainerapps.io/\"")
        buildConfigField("boolean", "DEBUG_LEVEL", value="true")

        // sourceSets {
        //    getByName("main") {
        //        proto {
        //            srcDir("src/main/proto")
        //        }
        //    }
        // }

        protobuf {
            protoc {
                artifact = "com.google.protobuf:protoc:${libs.versions.protobufVersion.get()}"
            }

            plugins {
                create("grpc") {
                    artifact = "io.grpc:protoc-gen-grpc-java:${libs.versions.grpcVersion.get()}"
                }
                create("grpckt") {
                    artifact = "io.grpc:protoc-gen-grpc-kotlin:${libs.versions.grpcKotlinVersion.get()}:jdk8@jar"
                }
            }

            generateProtoTasks {
                all().forEach { task ->
                    task.plugins {
                        create("grpc") {
                            option("lite")
                        }
                        create("grpckt") {
                            option("lite")
                        }
                    }
                    task.builtins {
                        create("java") {
                            option("lite")
                        }
                        create("kotlin") {
                            option("lite")
                        }
                    }
                }
            }
        }


    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
//    composeOptions {
//        kotlinCompilerExtensionVersion = "1.5.3"
//    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.text)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    //navigation
    implementation(libs.compose.navigation)

    //serialization
    implementation(libs.serialization)

    //splashScreen
    implementation(libs.splashScreen)

    //layout
    implementation(libs.androidx.compose.foundation.layout)

    //hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    //retrofit
    implementation(libs.squareup.retrofit2.retrofit)
    implementation(libs.squareup.retrofit2.converter)

    //okhttp
    implementation(libs.okhttp3.logging)
    implementation(libs.okhttp3)

    //gson
    implementation(libs.gson)

    //accompanist
    implementation(libs.accompanist)

    //datastore
    implementation(libs.preferenceDatastore)
    implementation(libs.protodatastore)

    implementation(libs.webrtc)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.signalr.core)
    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    implementation(libs.rxkotlin)

    implementation(libs.grpc.okhttp)
    implementation(libs.grpc.protobuf.lite)
    implementation(libs.grpc.stub)
    implementation(libs.grpc.android)
    implementation(libs.grpc.kotlin.stub)

    // Protobuf
    implementation(libs.protobuf.javalite)
    implementation(libs.protobuf.kotlin.lite)
    // implementation(libs.protobuf.java) // Removed to avoid conflict with javalite

    // Optional: Audio processing for voice calls
    implementation(libs.exoplayer.core)

    // Annotation
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")







}