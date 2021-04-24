plugins {
    war
    id("com.github.ben-manes.versions") version "0.38.0"
}

group = "dz.jsoftware95"
version = "4.0-SNAPSHOT"

val sourceEncoding = "UTF-8"
val warFileName = "ROOT.war"
val deploymentDir = layout.projectDirectory.dir("target")
val testDatabaseLogDir = "h2"

val intTest = "intTest"
val intTestSourceSet = sourceSets.register(intTest) {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
}

val intTestImplementation by lazy { intTestSourceSet.get().implementationConfigurationName }

configurations.configureEach {
    if (this.name.startsWith(intTest)) {
        val parentConfigName = SourceSet.TEST_SOURCE_SET_NAME + this.name.substring(intTest.length)
        this.extendsFrom(configurations[parentConfigName])
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    flatDir { dir("lib") }
    mavenCentral()
    maven {
        name = "JBoss Community repository"
        setUrl("http://repository.jboss.org/nexus/content/groups/public")
    }
    maven {
        name = "JBoss Deprecated"
        setUrl("http://repository.jboss.org/nexus/content/repositories/deprecated")
    }
    maven {
        name = "PrimeFaces Maven Repository"
        setUrl("http://repository.primefaces.org")
    }
    mavenCentral()
}

dependencies {
    val hibernateVersion = "5.4.30.Final"

    providedCompile("javax:javaee-api:8.0.1")
    providedCompile("org.glassfish:javax.faces:2.3.8")
    providedCompile("org.hibernate:hibernate-core:$hibernateVersion")
    providedCompile("org.hibernate:hibernate-entitymanager:$hibernateVersion")

    implementation("org.jetbrains:annotations:17.0.0")

    implementation("org.omnifaces:omnifaces:3.11")
    implementation("org.primefaces:primefaces:7.0")
    implementation("org.primefaces.extensions:primefaces-extensions:7.0.2")
    implementation("org.primefaces.extensions:resources-ckeditor:7.0.2")

    implementation("org.apache.logging.log4j:log4j-api:2.12.1")
    implementation("org.apache.logging.log4j:log4j-core:2.12.1")

    //implementation("org.picketbox:picketbox:+")
    implementation("de.mkammerer:argon2-jvm:2.6")

    implementation("com.itextpdf:kernel:7.1.8")
    implementation("com.itextpdf:io:7.1.8")
    implementation("com.itextpdf:layout:7.1.8")
    implementation("com.itextpdf:html2pdf:2.1.5")

    implementation("org.apache.commons:commons-lang3:3.9")
    implementation("org.apache.commons:commons-text:1.8")

    testImplementation("junit:junit:4.13")
    testImplementation("org.jboss.weld:weld-junit4:2.0.1.Final")
    testImplementation("org.jboss.weld.module:weld-ejb:3.1.2.Final")
    testImplementation("com.h2database:h2:1.4.200")
    testImplementation("mysql:mysql-connector-java:8.0.24")
    testImplementation("org.hamcrest:hamcrest-all:1.3")
    testImplementation("org.mockito:mockito-all:2.0.2-beta")
    testImplementation("org.reflections:reflections:0.9.12")

    testRuntimeOnly("org.hibernate:hibernate-core:$hibernateVersion")
    testRuntimeOnly("org.hibernate:hibernate-entitymanager:$hibernateVersion")
//    testImplementation("org.hibernate:hibernate-c3p0:$hibernateVersion")
//    testImplementation("c3p0:c3p0:0.9.1.2")
    testImplementation("org.codehaus.btm:btm:2.1.4")
    testImplementation("org.slf4j:slf4j-api:1.7.30")
    testImplementation("ch.qos.logback:logback-classic:1.2.3")
    testImplementation("nl.jqno.equalsverifier:equalsverifier:3.3")

    add(intTestImplementation, "org.testng:testng:7.1.0")
    add(intTestImplementation, "org.jboss.arquillian.testng:arquillian-testng-container:1.5.0.Final")
    add(intTestImplementation, "org.arquillian.container:arquillian-chameleon-testng-container-starter:1.0.0.CR6")
}

configurations.configureEach {
    resolutionStrategy {
        eachDependency {
            if (requested.group == "log4j" && requested.name == "log4j")
                useVersion("1.2.17")
            else if (requested.group == "com.sun.istack"
                    && requested.name == "istack-commons-runtime"
                    && requested.version?.startsWith("1.") == true)
                useVersion("1.0")
        }
    }
}

// this config is nice to have but cause a huge performance issue during sync
//tasks.withType(JavaCompile::class).configureEach {
//    this.options.encoding = sourceEncoding
//}

fun passClasspathAsSystemProperty(test: Test) {
    val classpathFiles: String = test.classpath.files
            .filter { it.isFile }
            .map { it.absolutePath }
            .reduce { path1, path2 -> path1 + File.pathSeparatorChar + path2 }

    test.systemProperty("intTest.testWar.classpath", classpathFiles)
}

fun addIntTestClasspath(test: Test) {
    test.testClassesDirs = intTestSourceSet.get().output.classesDirs
    test.classpath = intTestSourceSet.get().runtimeClasspath
}

tasks {
    this.war {
        destinationDirectory.set(deploymentDir)
        archiveFileName.set(warFileName)

    }

    val cleanTestLog by registering( Delete::class) {
        delete(testDatabaseLogDir)
    }

    val cleanTest by existing {
        dependsOn(cleanTestLog)
    }

    this.clean {
        description = "delete compiled/deployed/test output the are generated by running other tasks"
        dependsOn(cleanTest)
        delete("out", deploymentDir)
    }

    val parseTestLog by registering( H2LogParser::class) {
        description = "extract SQL statements from H2 database log"
        from(testDatabaseLogDir)
        into(testDatabaseLogDir)
    }

    this.test {
        description = "run unit tests"
        useJUnit()
        this.failFast = true
        finalizedBy(parseTestLog)
    }

    register("intTests", Test::class) {
        description = "run integration tests"
        addIntTestClasspath(this)
        passClasspathAsSystemProperty(this)
        useTestNG()
    }
}
