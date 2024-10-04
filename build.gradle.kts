import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import com.google.protobuf.gradle.*

plugins {
    kotlin("jvm") version ("2.0.20")
    kotlin("plugin.serialization") version "2.0.20"
    id("com.google.protobuf") version "0.9.4"
    application
}

apply(plugin = "com.google.protobuf")


repositories {
    maven("https://jitpack.io")
    maven("https://packages.confluent.io/maven/")
    mavenCentral()
}

configurations {
    this.all {
        exclude(group = "ch.qos.logback")
    }
}

val junitVersion = "5.11.1"
val ktorVersion = "2.3.12"
val log4jVersion = "2.24.0"
val assertJVersion = "3.26.3"
val prometheusVersion = "0.16.0"
val micrometerVersion = "1.13.4"
val protobufVersion = "4.28.2"



dependencies {
    api("com.google.protobuf:protobuf-java:$protobufVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.0")
    implementation ("com.google.cloud:google-cloud-bigquery:2.42.3"){
        exclude(group="com.fasterxml.jackson.core", module = "jackson-core")
    }
    implementation("com.natpryce:konfig:1.6.10.0")
    implementation("com.vlkan.log4j2:log4j2-logstash-layout-fatjar:1.0.5")
    implementation("commons-codec:commons-codec:1.17.1")
    implementation("io.confluent:kafka-protobuf-serializer:7.7.1")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-default-headers:$ktorVersion")
    implementation("io.ktor:ktor-server-metrics-micrometer:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.micrometer:micrometer-registry-prometheus:$micrometerVersion")
    implementation("io.prometheus:simpleclient_common:$prometheusVersion")
    implementation("io.prometheus:simpleclient:$prometheusVersion")
    implementation("org.apache.kafka:kafka-clients:3.8.0")
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.0.20")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.slf4j:slf4j-simple:2.0.16")
    protobuf(files("src/main/protobuf/"))
    testImplementation("io.ktor:ktor-server-test-host:1.6.7")
    testImplementation("org.assertj:assertj-core:$assertJVersion")
    testImplementation("org.assertj:assertj-core:$assertJVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

java {
    val mainJavaSourceSet: SourceDirectorySet = sourceSets.getByName("main").java
    val protoSrcDir = layout.buildDirectory.dir("generated/source/proto/main").get().asFile
    mainJavaSourceSet.srcDirs("$protoSrcDir/java", "$protoSrcDir/grpc", "$protoSrcDir/grpckotlin")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
}

sourceSets{
    create("proto"){
        proto {
            srcDir("src/main/protobuf/")
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        showExceptions = true
        showStackTraces = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        events("passed", "skipped", "failed")
    }
}

tasks.named<Jar>("jar") {
    archiveBaseName.set("app")

    manifest {
        attributes["Main-Class"] = "io.nais.devrapid.AppKt"
        attributes["Class-Path"] = configurations.runtimeClasspath.get().joinToString(separator = " ") {
            it.name
        }
    }

    doLast {
        configurations.runtimeClasspath.get().forEach {
            val file = File("${layout.buildDirectory.get().asFile}/libs/${it.name}")
            if (!file.exists())
                it.copyTo(file)
        }
    }
}


application {
    mainClass.set("io.nais.devrapid.App")
}