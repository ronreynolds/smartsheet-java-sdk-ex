plugins {
    java
    id("maven-publish")
}

group = "com.ronreynolds"
version = "0.0.1-SNAPSHOT"

// library versions
val assertJVersion = "3.24.2"
val commonsLangVersion = "3.14.0"
val jacocoVersion = "0.8.10"
val jUnitJupiterVersion = "5.5.1"
val guavaVersion = "33.2.1-jre"
val jacksonCoreVersion = "2.9.10"
val jacksonDatabindVersion = "2.9.10.8"
val lombokVersion = "1.18.32"
val slf4jVersion = "1.7.25"
val smartsheetSdkVersion = "3.2.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenLocal()
    mavenCentral()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

dependencies {
    // code dependencies
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonCoreVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabindVersion")
    implementation("com.google.guava:guava:$guavaVersion")
    implementation("com.smartsheet:smartsheet-sdk-java:$smartsheetSdkVersion")
    implementation("org.apache.commons:commons-lang3:$commonsLangVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")

    // compile-only dependencies
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")

    // test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter:$jUnitJupiterVersion")
    testImplementation("org.assertj:assertj-core:$assertJVersion")
    testCompileOnly("org.projectlombok:lombok:$lombokVersion")
    testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion")
}

tasks.withType<Test> {
    useJUnitPlatform()
}