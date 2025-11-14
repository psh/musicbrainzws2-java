import org.gradle.api.JavaVersion
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.gradle.api.publish.maven.MavenPublication

plugins {
    `java-library`
    `maven-publish`
    signing
    jacoco
}

group = "info.schnatterer.musicbrainzws2-java"
version = "3.2.1-SNAPSHOT"

java {
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.httpcomponents.client5:httpclient5:5.1.2")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("jdom:jdom:1.1")
    implementation("fm.last:coverartarchive-api:2.1.1")
    implementation("org.slf4j:slf4j-jdk14:1.7.32")

    compileOnly("com.cloudogu.versionName:processor:2.1.0")

    testImplementation("junit:junit:4.13.2")
}

val versionName = project.version.toString()

tasks.test {
    useJUnit()
    finalizedBy(tasks.named("jacocoTestReport"))
}

jacoco {
    toolVersion = "0.8.7"
}

tasks.named<JacocoReport>("jacocoTestReport") {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        val opts = options as StandardJavadocDocletOptions
        opts.addStringOption("source", "8")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("musicbrainzws2-java")
                description.set("java binding for MusicBrainz XML Web Service/Version 2")
                url.set("https://github.com/schnatterer/musicbrainzws2-java/")
                inceptionYear.set("2011")

                licenses {
                    license {
                        name.set("GNU General Public License 3.0")
                        url.set("http://www.gnu.org/copyleft/gpl.html")
                    }
                }

                developers {
                    developer {
                        name.set("Johannes Schnatterer")
                        email.set("schnatterer@users.noreply.github.com")
                        timezone.set("Europe/Berlin")
                    }
                }

                organization {
                    name.set("github/schnatterer")
                    url.set("https://github.com/schnatterer/")
                }

                scm {
                    connection.set("scm:git:ssh://github.com/schnatterer/musicbrainzws2-java.git")
                    developerConnection.set("scm:git:ssh://git@github.com/schnatterer/musicbrainzws2-java.git")
                    url.set("https://github.com/schnatterer/musicbrainzws2-java.git")
                }
            }
        }
    }

    repositories {
        maven {
            name = "OSSRH"
            val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl

            credentials {
                username = findProperty("ossrhUsername") as String? ?: ""
                password = findProperty("ossrhPassword") as String? ?: ""
            }
        }
    }
}

signing {
    setRequired {
        gradle.taskGraph.hasTask("publish") && !version.toString().endsWith("SNAPSHOT")
    }
    sign(publishing.publications["mavenJava"])
}

// Custom properties for Sonar (replace old `ext {}` block)
extra["sonarOrganization"] = "schnatterer-github"
extra["sonarHostUrl"] = "https://sonarcloud.io"
