plugins {
    java
    id("net.minecrell.licenser") version "0.4.1"
    id("net.minecrell.plugin-yml.bungee") version "0.3.0"
}

group = "eu.mikroskeem.mikroskeemsrealm"
version = "0.0.1-SNAPSHOT"

val mikrocordApiVersion = "1.14-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    compileOnly("eu.mikroskeem.mikrocord:mikrocord-api:$mikrocordApiVersion")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

license {
    header = rootProject.file("etc/HEADER")
    filter.include("**/*.java")
}

bungee {
    name = "ProxyEnvConfigAdapter"
    description = "Overrides certain configuration values using environment variables"
    author = "${listOf("mikroskeem")}"
    main = "eu.mikroskeem.mikroskeemsrealm.proxyenvconfigadapter.ProxyEnvConfigAdapter"
}