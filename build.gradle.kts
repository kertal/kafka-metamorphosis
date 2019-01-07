import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.jvm.tasks.Jar

plugins {
    java
    kotlin("jvm") version "1.3.11"
    application
    distribution
    id("com.github.johnrengelman.shadow") version "4.0.3"

}


group = "net.ankertal.kafka"
version = "1.0-SNAPSHOT"

application {
    mainClassName = "net.ankertal.kafka.metamorphosis.MainKt"
}

repositories {
    mavenCentral()
}



dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.0")

    compile("org.slf4j:slf4j-api:1.7.25")
    compile("org.slf4j:slf4j-log4j12:1.7.25")
    // Kafka
    compile("org.apache.kafka:kafka-clients:2.1.0")
    compile("org.apache.kafka:kafka-streams:2.1.0")
    // CLI
    compile("com.github.ajalt:clikt:1.6.0")

    // Testing
    testCompile("junit:junit:4.12")
    testCompile("org.jetbrains.kotlin:kotlin-test-junit:1.3.11")
    testCompile("com.salesforce.kafka.test:kafka-junit4:3.1.0")
    testCompile("org.apache.kafka:kafka_2.11:2.1.0")
    testCompile("com.karumi.kotlinsnapshot:core:2.0.0")
    testCompile(kotlin("test"))
    testCompile(kotlin("test-junit"))
    //building
}


tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}