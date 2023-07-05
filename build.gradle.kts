plugins {
    id("java-library")
    id("maven-publish")
    id("checkstyle")
    id("com.github.johnrengelman.shadow") version("7.0.0")
}

group = "com.github.burningtnt"
version = "0.1.0"
description = "A bot on KOOK to do actions on GitHub"

java {
    withSourcesJar()
}

repositories {
    mavenCentral()

    maven(url = "https://libraries.minecraft.net")
    maven(url = "https://jitpack.io")
    maven(url = "https://maven.fabricmc.net/")
}

val javadocJar = tasks.create<Jar>("javadocJar") {
    group = "build"
    archiveClassifier.set("javadoc")
}

checkstyle {
    sourceSets = mutableSetOf()
}

tasks.getByName("build") {
    dependsOn(tasks.getByName("checkstyleMain") {
        group = "build"
    })
    dependsOn(tasks.getByName("checkstyleTest") {
        group = "build"
    })
}

val plugin = tasks.create<Copy>("plugin") {
    dependsOn(tasks.getByName("shadowJar"))

    this.destinationDir = File("runtime/plugins")
    this.from("build/libs/${project.name}-${project.version}-all.jar")
}

dependencies {
    compileOnly("org.jetbrains:annotations:24.0.1")
    implementation("com.mojang:brigadier:1.0.18")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.github.SNWCreations:JKook:0.49.0")
    implementation("org.apache.httpcomponents:httpclient:4.5.14")
    compileOnly("com.github.SNWCreations:KookBC:0.27.1")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])
        }
    }
}