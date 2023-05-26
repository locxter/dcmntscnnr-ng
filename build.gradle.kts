plugins {
    kotlin("jvm") version "1.8.21"
    application
}

group = "com.github.locxter"
version = "1.0"
description = "This is the next generation rewrite of dcmntscnnr, which is a GUI program for scanning documents."

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.formdev:flatlaf:3.1.1")
    implementation("org.openpnp:opencv:4.7.0-0")
    implementation("com.github.librepdf:openpdf:1.3.30")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("com.github.locxter.dcmntscnnr.ng.MainKt")
}

tasks {
    val standalone = register<Jar>("standalone") {
        dependsOn.addAll(listOf("compileJava", "compileKotlin", "processResources"))
        archiveClassifier.set("standalone")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest { attributes(mapOf("Main-Class" to application.mainClass)) }
        val sourcesMain = sourceSets.main.get()
        val contents = configurations.runtimeClasspath.get()
            .map { if (it.isDirectory) it else zipTree(it) } +
                sourcesMain.output
        from(contents)
    }
    build {
        dependsOn(standalone)
    }
}
