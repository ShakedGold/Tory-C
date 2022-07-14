import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    id("org.jetbrains.compose") version "1.0.1"
}

group = "me.shake"
version = "1.0"

repositories {
    google()
    mavenCentral()
    maven{url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev")}
    maven{url = uri("https://repository.aspose.com/repo/")}
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("net.bramp.ffmpeg:ffmpeg:0.7.0")
    implementation("fr.opensagres.xdocreport:fr.opensagres.xdocreport.document:2.0.3")
    implementation("fr.opensagres.xdocreport:fr.opensagres.xdocreport.converter.docx.xwpf:2.0.3")
    implementation("fr.opensagres.xdocreport:fr.opensagres.xdocreport.document.docx:2.0.3")
    implementation("fr.opensagres.xdocreport:fr.opensagres.xdocreport.core:2.0.3")
    implementation("fr.opensagres.xdocreport:xdocreport:2.0.3")
    implementation("fr.opensagres.xdocreport:fr.opensagres.xdocreport.converter:2.0.3")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "16"
}

compose.desktop {
    application {
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageVersion = "1.0.0"
            packageName = "ToryC"
            description = "Video / Image Conversion App"
            copyright = "© 2022 Shaked Gold. All rights reserved."
            jvmArgs(
                "-Dapple.awt.application.appearance=system"
            )
            modules("java.sql")

            macOS {
                iconFile.set(project.file("icon.icns"))
            }
            windows {
                iconFile.set(project.file("icon.ico"))
            }
            linux {
                iconFile.set(project.file("icon.png"))
            }
        }
        mainClass = "MainKt"
    }
}