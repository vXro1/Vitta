import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// URL del backend para release: primero -Pvitta.baseUrl=..., luego
// vitta.baseUrl en local.properties. Si no hay ninguna queda el placeholder.
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val releaseBaseUrl: String = (project.findProperty("vitta.baseUrl") as String?)
    ?: localProps.getProperty("vitta.baseUrl")
    ?: "https://CAMBIAR-URL/api/"

// Contraseña de la cuenta demo (solo debug). No va en el código: se pone
// vitta.demoPassword en local.properties. Sin ella el botón Demo no aparece.
val demoPassword: String = (project.findProperty("vitta.demoPassword") as String?)
    ?: localProps.getProperty("vitta.demoPassword")
    ?: ""

android {
    namespace = "com.vitta.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.vitta.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0-designsystem"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            // 10.0.2.2 es el localhost del PC visto desde el emulador
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:3000/api/\"")
            manifestPlaceholders["usesCleartext"] = "true"
            buildConfigField("String", "DEMO_PASSWORD", "\"$demoPassword\"")
        }
        release {
            buildConfigField("String", "BASE_URL", "\"$releaseBaseUrl\"")
            // en release solo HTTPS
            manifestPlaceholders["usesCleartext"] = "false"
            // la cuenta demo no existe en release
            buildConfigField("String", "DEMO_PASSWORD", "\"\"")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.compose.material:material-icons-extended:1.7.5")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.2")
    // SVG-first image loading: Vitta's habit icons, badges and mascot
    // illustrations are rich multi-color SVGs (not single-tint glyphs), so
    // they are rendered with Coil instead of being hand-converted to
    // VectorDrawable (see README_RECURSOS.md for why).
    implementation(libs.coil.compose)
    implementation(libs.coil.svg)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
