import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.61"
    application
    id("org.openjfx.javafxplugin") version "0.0.8"
    //id("org.beryx.jlink") version "2.17.1"
    //id("com.github.johnrengelman.shadow") version "5.2.0"
    id("org.beryx.runtime") version "1.8.0"
}

group = "ru.mipt.physics"
version = "1.1.2"

val compileKotlin: KotlinCompile by tasks
val compileJava: JavaCompile by tasks
compileJava.destinationDir = compileKotlin.destinationDir

compileKotlin.kotlinOptions {
    jvmTarget = "11"
}

application {
    mainClassName = "ru.mipt.physics.survey.ReportAppKt"
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.3.61")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.61")
    implementation("no.tornado:tornadofx:1.7.20") {
        exclude("org.jetbrains.kotlin")
    }
    implementation("org.freemarker:freemarker:2.3.29")
    implementation("com.google.api-client:google-api-client:1.30.5")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.23.0")
    implementation("com.google.apis:google-api-services-sheets:v4-rev604-1.25.0")

    testImplementation("junit:junit:4.+")
}

javafx {
    modules = listOf("javafx.web")
}

//jlink{
//    addOptions("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages")
//    launcher {
//        name = "phys-survey"
//    }
//    addExtraDependencies("javafx")
//    //imageZip.set(project.file("${project.buildDir}/image-zip/phys-survey.zip"))
//}

runtime {
    imageZip.set(project.file("${project.buildDir}/image-zip/phys-survey.zip"))
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    modules.set(listOf("java.desktop", "jdk.unsupported", "java.scripting", "java.logging", "java.xml"))
}