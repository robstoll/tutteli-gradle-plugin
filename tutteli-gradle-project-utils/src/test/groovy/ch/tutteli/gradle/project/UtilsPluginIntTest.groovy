package ch.tutteli.gradle.project

import ch.tutteli.gradle.test.SettingsExtension
import ch.tutteli.gradle.test.SettingsExtensionObject
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import static ch.tutteli.gradle.test.Asserts.assertProjectInOutput
import static ch.tutteli.gradle.test.Asserts.assertStatusOk
import static org.junit.jupiter.api.Assertions.assertTrue

@ExtendWith(SettingsExtension)
class UtilsPluginIntTest {

    @Test
    void smokeTest(SettingsExtensionObject settingsSetup) throws IOException {
        //arrange
        new File(settingsSetup.tmp, 'test-project-one').mkdir()
        settingsSetup.settings << """
        rootProject.name='test-project'
        include 'test-project-one'
        """
        File buildGradle = new File(settingsSetup.tmp, 'build.gradle')
        buildGradle << """
        buildscript {
            dependencies {
                classpath files($settingsSetup.pluginClasspath)
            }
        }
        apply plugin: 'ch.tutteli.project.utils'
        
        println("here we are: \${prefixedProject('one').name}")
        """
        //act
        def result = GradleRunner.create()
            .withProjectDir(settingsSetup.tmp)
            .withArguments("projects")
            .build()
        //assert
        assertProjectInOutput(result, ':test-project-one')
        assertTrue(result.output.contains("here we are: test-project-one"), "println in output")
        assertStatusOk(result, ":projects")
    }
}