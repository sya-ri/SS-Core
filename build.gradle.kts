plugins {
    kotlin("jvm") version "1.4.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.3.0"
    `maven-publish`
}

group = "me.syari.ss.core"
version = "3.1.1"

val ssMavenRepoUploadURL: String by extra
val ssMavenRepoUploadUser: String by extra
val ssMavenRepoUploadPassword: String by extra

repositories {
    mavenCentral()
    maven {
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }
}

dependencies {
    implementation("com.destroystokyo.paper:paper-api:1.16.2-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

bukkit {
    name = project.name
    version = project.version.toString()
    main = "$group.Main"
    author = "sya_ri"
    depend = listOf("SS-Kotlin")
    apiVersion = "1.16"
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

val jar by tasks.getting(Jar::class) {
    from(configurations.compile.get().map {
        if (it.isDirectory) it else zipTree(it)
    })
}

val sourceJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allJava.srcDirs)
}

publishing {
    repositories {
        maven {
            url = uri(ssMavenRepoUploadURL)
            credentials {
                username = ssMavenRepoUploadUser
                password = ssMavenRepoUploadPassword
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(sourceJar.get())
        }
    }
}
