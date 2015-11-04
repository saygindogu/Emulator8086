plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.0.12"
}

buildscript {
    repositories {
        maven {
            setUrl("https://plugins.gradle.org/m2/")
        }
    }
    dependencies {
        classpath("org.openjfx:javafx-plugin:0.0.12")
    }
}
apply(plugin = "org.openjfx.javafxplugin")
apply(plugin = "java")

application {
    mainClassName = "com.saygindogu.emulator.Emulator"
}

javafx {
    modules("javafx.controls", "javafx.fxml")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
