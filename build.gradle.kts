import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    id("org.openjfx.javafxplugin") version "0.0.10"
}

group = "com.abyss.debug"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

javafx {
    modules("javafx.controls", "javafx.fxml", "javafx.graphics")
}

dependencies {
    implementation("com.abyss.api:AbyssAPI:1.0-SNAPSHOT")
    implementation("no.tornado:tornadofx:1.7.20")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("io.insert-koin:koin-core:3.2.0")
    implementation("org.mongodb:mongo-java-driver:3.12.11")
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
    testImplementation(kotlin("test"))
}

tasks.create("copyJar", Copy::class) {
    from(tasks.withType<Jar>())
    into("C:\\Users\\david\\OneDrive\\Documents\\Abyss")
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if(it.isDirectory) it else zipTree(it) })
    finalizedBy(tasks.named("copyJar"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}