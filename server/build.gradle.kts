import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.io.BufferedReader

plugins {
//    id("org.jetbrains.kotlin.jvm") version "1.4.21"
    application
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("org.jmailen.kotlinter") version "3.3.0"
    id("edu.sc.seis.launch4j") version "2.4.9"
}

val TachideskVersion = "v0.0.2"


repositories {
    mavenCentral()
    jcenter()
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
//    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Source models and interfaces from Tachiyomi 1.x
    // using source class from tachiyomi commit 9493577de27c40ce8b2b6122cc447d025e34c477 to not depend on tachiyomi.sourceapi
//    implementation("tachiyomi.sourceapi:source-api:1.1")

    implementation("com.github.inorichi.injekt:injekt-core:65b0440")

    val okhttpVersion = "4.10.0-RC1"
    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okhttpVersion")
    implementation("com.squareup.okhttp3:okhttp-dnsoverhttps:$okhttpVersion")
    implementation("com.squareup.okio:okio:2.9.0")


    // retrofit
    val retrofitVersion = "2.9.0"
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")
    implementation("com.squareup.retrofit2:adapter-rxjava:$retrofitVersion")


    // reactivex
    implementation("io.reactivex:rxjava:1.3.8")
//    implementation("io.reactivex:rxandroid:1.2.1")
//    implementation("com.jakewharton.rxrelay:rxrelay:1.2.0")
//    implementation("com.github.pwittchen:reactivenetwork:0.13.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.0")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.github.salomonbrys.kotson:kotson:2.5.0")

    implementation("org.jsoup:jsoup:1.13.1")
    implementation("com.github.salomonbrys.kotson:kotson:2.5.0")

    // dex2jar: https://github.com/DexPatcher/dex2jar/releases/tag/v2.1-20190905-lanchon
    implementation("com.github.DexPatcher.dex2jar:dex-tools:v2.1-20190905-lanchon")


    // api
    implementation("io.javalin:javalin:3.12.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.10.3")

    // Exposed ORM
    val exposedVersion = "0.28.1"
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("com.h2database:h2:1.4.199")

    // tray icon
    implementation("com.dorkbox:SystemTray:3.17")


    // AndroidCompat
    implementation(project(":AndroidCompat"))
    implementation(project(":AndroidCompat:Config"))

    // uncomment to test extensions directly
//    implementation(fileTree("lib/"))
}

val name = "ir.armor.tachidesk.Main"
application {
    mainClass.set(name)

    // Required by ShadowJar.
    mainClassName = name
}

sourceSets {
    main {
        resources {
            srcDir("src/main/resources")
        }
    }
}

val TachideskRevision = Runtime
        .getRuntime()
        .exec("git rev-list master --count")
        .let { process ->
            process.waitFor()
            val output = process.inputStream.use {
                it.bufferedReader().use(BufferedReader::readText)
            }
            process.destroy()
            "r" + output.trim()

        }

tasks {
    jar {
        manifest {
            attributes(
                    mapOf(
                            "Main-Class" to "com.example.MainKt", //will make your jar (produced by jar task) runnable
                            "ImplementationTitle" to project.name,
                            "Implementation-Version" to project.version)
            )
        }
    }
    shadowJar {
        manifest.inheritFrom(jar.get().manifest) //will make your shadowJar (produced by jar task) runnable
        archiveBaseName.set("Inspector")
        archiveVersion.set(TachideskVersion)
        archiveClassifier.set(TachideskRevision)
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf(
                "-Xopt-in=kotlin.RequiresOptIn",
                "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-Xopt-in=kotlinx.coroutines.InternalCoroutinesApi"
            )
        }
    }
}

launch4j { //used for windows
    mainClassName = name
    bundledJrePath = "jre"
    bundledJre64Bit = true
    jreMinVersion = "8"
    outputDir = "Tachidesk-$TachideskVersion-$TachideskRevision-win32"
    icon = "${projectDir}/src/main/resources/icon/faviconlogo.ico"
    jar = "${projectDir}/build/Tachidesk-$TachideskVersion-$TachideskRevision.jar"
}

tasks.register<Zip>("windowsPackage") {
    from(fileTree("$buildDir/Tachidesk-$TachideskVersion-$TachideskRevision-win32"))
    destinationDirectory.set(File("$buildDir"))
    archiveFileName.set("Tachidesk-$TachideskVersion-$TachideskRevision-win32.zip")
    dependsOn("windowsPackageWorkaround2")
}

tasks.register<Delete>("windowsPackageWorkaround2") {
    delete(
            "$buildDir/Tachidesk-$TachideskVersion-$TachideskRevision-win32/jre",
            "$buildDir/Tachidesk-$TachideskVersion-$TachideskRevision-win32/lib",
            "$buildDir/Tachidesk-$TachideskVersion-$TachideskRevision-win32/server.exe",
            "$buildDir/Tachidesk-$TachideskVersion-$TachideskRevision-win32/Tachidesk-$TachideskVersion-$TachideskRevision-win32/Tachidesk-$TachideskVersion-$TachideskRevision-win32"
    )
    dependsOn("windowsPackageWorkaround")
}

tasks.register<Copy>("windowsPackageWorkaround") {
    from("$buildDir/Tachidesk-$TachideskVersion-$TachideskRevision-win32")
    into("$buildDir/Tachidesk-$TachideskVersion-$TachideskRevision-win32/Tachidesk-$TachideskVersion-$TachideskRevision-win32")
    dependsOn("deleteUnwantedJreDir")
}

tasks.register<Delete>("deleteUnwantedJreDir") {
    delete(
            "$buildDir/Tachidesk-$TachideskVersion-$TachideskRevision-win32/jdk8u282-b08-jre"
    )
    dependsOn("addJreToDistributable")
}

tasks.register<Copy>("addJreToDistributable") {
    from(zipTree("$buildDir/OpenJDK8U-jre_x86-32_windows_hotspot_8u282b08.zip"))
    into("$buildDir/Tachidesk-$TachideskVersion-$TachideskRevision-win32")
    eachFile {
        path = path.replace(".*-jre".toRegex(),"jre")
    }
    dependsOn("downloadJre")
    dependsOn("createExe")
}

tasks.register<de.undercouch.gradle.tasks.download.Download>("downloadJre") {
    src("https://github.com/AdoptOpenJDK/openjdk8-binaries/releases/download/jdk8u282-b08/OpenJDK8U-jre_x86-32_windows_hotspot_8u282b08.zip")
    dest(buildDir)
    overwrite(false)
    onlyIfModified(true)
}

tasks.withType<ShadowJar> {
    destinationDirectory.set(File("$rootDir/server/build"))
    dependsOn("formatKotlin", "lintKotlin")
}

tasks.named("run") {
    dependsOn("formatKotlin", "lintKotlin")
}



