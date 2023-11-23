version = "1.0-SNAPSHOT"

plugins {
    id("java")
    application
}

group = "com.contrabass"

application {
    mainClass.set("me.jsedwards.Main")
}

// LWJGL stuff
val lwjglVersion = "3.3.3"

val lwjglNatives = Pair(
    System.getProperty("os.name")!!,
    System.getProperty("os.arch")!!
).let { (name, arch) ->
    when {
        arrayOf("Linux", "FreeBSD", "SunOS", "Unit").any { name.startsWith(it) } ->
            if (arrayOf("arm", "aarch64").any { arch.startsWith(it) })
                "natives-linux${if (arch.contains("64") || arch.startsWith("armv8")) "-arm64" else "-arm32"}"
            else
                "natives-linux"
        arrayOf("Mac OS X", "Darwin").any { name.startsWith(it) }                ->
            "natives-macos${if (arch.startsWith("aarch64")) "-arm64" else ""}"
        arrayOf("Windows").any { name.startsWith(it) }                           ->
            "natives-windows-x86"
        else -> throw Error("Unrecognized or unsupported platform. Please set \"lwjglNatives\" manually")
    }
}

repositories {
    mavenCentral()
    maven("https://jitpack.io") // For jSystemThemeDetector
}

dependencies {
    // Testing
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // GUI appearance
    implementation("com.formdev:flatlaf:3.2.1")
    implementation("com.github.Dansoftowner:jSystemThemeDetector:3.8")

    // Gson - JSON parsing
    implementation("com.google.code.gson:gson:2.10.1")

    // Jsoup - web scraping
    implementation("org.jsoup:jsoup:1.16.1")

    // Apache commons lang
    implementation("org.apache.commons:commons-lang3:3.13.0")

    // Log4j2
    implementation("org.apache.logging.log4j:log4j-api:2.21.1")
    implementation("org.apache.logging.log4j:log4j-core:2.21.1")

    // LWJGL - native file dialogs
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))
    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-nfd")
    implementation("org.lwjgl", "lwjgl-tinyfd")
    runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-nfd", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-tinyfd", classifier = lwjglNatives)
}

tasks.test {
    useJUnitPlatform()
}