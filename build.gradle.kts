version = "1.0-SNAPSHOT"

plugins {
    id("java")
    id("io.ktor.plugin") version "2.3.7"
    application
}

group = "com.contrabass"

application {
    mainClass.set("me.jsedwards.Main")
}

ktor {
    fatJar {
        archiveFileName.set("${project.name}-${project.version}-fat.jar")
    }
}

// LWJGL stuff
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
        arrayOf("Mac OS X", "Darwin").any { name.startsWith(it) } ->
            "natives-macos${if (arch.startsWith("aarch64")) "-arm64" else ""}"
        arrayOf("Windows").any { name.startsWith(it) } ->
            if (arch.contains("64"))
                "natives-windows${if (arch.startsWith("aarch64")) "-arm64" else ""}"
            else
                "natives-windows-x86"
        else ->
            throw Error("Unrecognized or unsupported platform. Please set \"lwjglNatives\" manually")
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

    // Jsoup - html parsing
    implementation("org.jsoup:jsoup:1.16.1")

    // Jackson - JSON parsing
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.3")
    implementation("com.fasterxml.jackson.core:jackson-core:2.15.3")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.15.3")

    // SnakeYAML - YAML parsing
    implementation("org.yaml:snakeyaml:2.2")

    // Apache commons lang
    implementation("org.apache.commons:commons-lang3:3.13.0")

    // Apache commons IO
    implementation("commons-io:commons-io:2.15.1")

    // Log4j2
    implementation("org.apache.logging.log4j:log4j-api:2.21.1")
    implementation("org.apache.logging.log4j:log4j-core:2.21.1")

    // Slf4j
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.slf4j:slf4j-simple:2.0.9")

    // LWJGL - native file dialogs
    implementation(platform("org.lwjgl:lwjgl-bom:3.3.3"))
    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-nfd")
    implementation("org.lwjgl", "lwjgl-tinyfd")
    runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-nfd", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-tinyfd", classifier = lwjglNatives)

    // NBT and Anvil parsing
    implementation("io.github.jglrxavpok.hephaistos:common:2.6.1")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed")
    }
}