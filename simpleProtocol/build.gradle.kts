import com.google.protobuf.gradle.proto
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("com.lifemmo.pl.base.kotlin-library-conventions")
}

group =  Versions.groupId
version = Versions.version

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = Versions.groupId
            artifactId = "simpleProtocol"
            version = Versions.version
            from(components["java"])
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${Versions.protocVersion}"
    }
}

sourceSets {
    main {
        proto {
            srcDir ("./proto")
        }
        java {
            srcDirs(
                "build/generated/source/proto/main/java"
            )
        }
    }
}

