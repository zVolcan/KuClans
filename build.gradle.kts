plugins {
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
    id("com.gradleup.shadow") version "9.3.0"
}

group = "site.zvolcan"
version = "1.0"

repositories {
    mavenCentral()
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        url = uri("https://jitpack.io")
    }
    maven("https://repo.extendedclip.com/releases/")
}

dependencies {
    paperweight.paperDevBundle("1.21-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.7")
    implementation("com.github.NEZNAMY:YamlAssist:1.0.8")
    implementation("com.zaxxer:HikariCP:7.0.2")
}

tasks {
    runServer {
        minecraftVersion("1.21.10")
    }

    shadowJar {
        archiveFileName = "KuClans.jar"
        relocate("me.neznamy.yamlassist", "site.zvolcan.yamlassist")
        relocate("com.zaxxer.hikari", "site.zvolcan.hikari")
    }

    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(21)
    }
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}
