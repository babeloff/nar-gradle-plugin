package de.fanero.gradle.plugin.nar

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.util.jar.Manifest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * @author Robert Kühne
 */
class NarPluginFuncTest extends Specification {

    private static final TEST_BASE_NAME = 'nar-test'
    private static final TEST_VERSION = '1.0'

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    File buildFile
    File settingsFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
plugins {
    id 'de.fanero.gradle.plugin.nar'
}
nar {
    baseName '${TEST_BASE_NAME}'
}
group = 'de.fanero.test'
version = '${TEST_VERSION}'
"""
        settingsFile = testProjectDir.newFile('settings.gradle')
        settingsFile << """
rootProject.name = "nar-test"
"""
    }

    def "test simple nar"() {

        when:
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('nar')
                .withPluginClasspath()
                .build()

        Manifest manifest = extractManifest()

        then:
        manifest != null
        manifest.getMainAttributes().getValue('Nar-Id') == 'nar-test'
        manifest.getMainAttributes().getValue('Nar-Dependency-Id') == null
    }

    def "test parent nar entry"() {

        buildFile << """
repositories {
    mavenCentral()
}
dependencies {
    nar 'org.apache.nifi:nifi-standard-services-api-nar:0.2.1'
}
"""
        when:
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('nar')
                .withPluginClasspath()
                .build()

        Manifest manifest = extractManifest()

        then:
        manifest != null
        manifest.getMainAttributes().getValue('Nar-Id') == 'nar-test'
        manifest.getMainAttributes().getValue('Nar-Dependency-Id') == 'nifi-standard-services-api-nar'
    }

    def "test multiple parent nar entries"() {

        buildFile << """
repositories {
    mavenCentral()
}
dependencies {
    nar 'org.apache.nifi:nifi-standard-services-api-nar:0.2.1'
    nar 'org.apache.nifi:nifi-enrich-nar:1.5.0'
}
"""
        expect:
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('nar')
                .withPluginClasspath()
                .buildAndFail()
    }

    def "test bundled jar dependencies"() {

        buildFile << """
repositories {
    mavenCentral()
}
dependencies {
    compile group: 'commons-io', name: 'commons-io', version: '2.2'
}
"""
        when:
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('nar')
                .withPluginClasspath()
                .build()

        then:
        countBundledJars() == 3
    }

    int countBundledJars() {
        int counter = 0
        eachZipEntry {ZipInputStream zip, ZipEntry entry ->
            if (entry.name.startsWith('META-INF/bundled-dependencies')) {
                counter++
            }
            true
        }
        counter
    }

    Manifest extractManifest() {
        Manifest manifest = null
        eachZipEntry { ZipInputStream zip, ZipEntry entry ->
            if (entry.name == 'META-INF/MANIFEST.MF') {
                manifest = new Manifest(zip)
                return false
            } else {
                return true
            }
        }

        manifest
    }

    private void eachZipEntry(Closure closure) {
        narFile().withInputStream {
            ZipInputStream zip = new ZipInputStream(it)
            ZipEntry entry = zip.nextEntry
            while (entry != null) {
                def result = closure(zip, entry)
                if (!result) {
                    break
                }
                entry = zip.nextEntry
            }
        }
    }

    private File narFile() {
        new File(testProjectDir.root, "build/libs/${TEST_BASE_NAME}-${TEST_VERSION}.nar")
    }
}