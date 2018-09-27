package ch.tutteli.gradle.publish


import com.jfrog.bintray.gradle.BintrayExtension as JFrogBintrayPluginExtension
import org.gradle.api.Project
import org.gradle.api.internal.plugins.PluginApplicationException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

import static SetUp.*
import static ch.tutteli.gradle.test.Asserts.assertThrowsProjectConfigExceptionWithCause
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assertions.assertTrue

class PublishPluginValidationTest {

    @Test
    void apply_noSourceSets_throwsIllegalStateException() {
        //arrange
        Project project = ProjectBuilder.builder()
            .withName(ARTIFACT_ID)
            .build()
        //act && assert
        def exception = assertThrows(PluginApplicationException) {
            project.plugins.apply(PublishPlugin)
        }
        //assert
        assertEquals(IllegalStateException, exception.cause.class)
        def message = "The project $project.name does not have any sources"
        assertTrue(exception.cause.message.contains(message), "cause.message contains $message:\n$exception.cause.message")
    }

    @Test
    void evaluate_versionUnspecified_throwsIllegalStateException() {
        //arrange
        Project project = setUp()
        project.version = "unspecified"
        //act && assert
        assertThrowsProjectConfigWithCauseIllegalStateNotDefined('project.version', project)
    }

    @Test
    void evaluate_versionEmpty_throwsIllegalStateException() {
        //arrange
        Project project = setUp()
        project.version = ""
        //act && assert
        assertThrowsProjectConfigWithCauseIllegalStateNotDefined('project.version', project)
    }

    @Test
    void evaluate_groupBlank_throwsIllegalStateException() {
        //arrange
        Project project = setUp()
        project.group = "  "
        //act && assert
        assertThrowsProjectConfigWithCauseIllegalStateNotDefined('project.group', project)
    }

    @Test
    void evaluate_descriptionNull_throwsIllegalStateException() {
        //arrange
        Project project = setUp()
        project.description = null
        //act && assert
        assertThrowsProjectConfigWithCauseIllegalStateNotDefined('project.description', project)
    }


    @Test
    void evaluate_artifactAndComponentNull_throwsIllegalStateException() {
        //arrange
        Project project = setUp()
        getPluginExtension(project).component.set(null)
        getPluginExtension(project).artifacts.set(null)
        //act && assert
        assertThrowsProjectConfigWithCauseIllegalStateNotDefined("either ${PublishPlugin.EXTENSION_NAME}.component or ${PublishPlugin.EXTENSION_NAME}.artifacts", project)
    }

    @Test
    void evaluate_artifactsEmptyAndComponentNull_throwsIllegalStateException() {
        //arrange
        Project project = setUp()
        getPluginExtension(project).component.set(null)
        getPluginExtension(project).artifacts.set(new ArrayList<>())
        //act && assert
        assertThrowsProjectConfigWithCauseIllegalStateNotDefined("either ${PublishPlugin.EXTENSION_NAME}.component or ${PublishPlugin.EXTENSION_NAME}.artifacts", project)
    }

    @Test
    void evaluate_artifactsNullButComponentSet_NoException() {
        //arrange
        Project project = setUp()
        getPluginExtension(project).component.set(project.components.getByName('java'))
        getPluginExtension(project).artifacts.set(null)
        //act && assert
        //act && assert no exception
        project.evaluate()
    }

    @Test
    void evaluate_componentNullButArtifactsSet_NoException() {
        //arrange
        Project project = setUp()
        getPluginExtension(project).component.set(null)
        getPluginExtension(project).artifacts.add(project.tasks.getByName('jar'))
        //act && assert no exception
        project.evaluate()
    }

    @Test
    void evaluate_githubUserNotDefined_throwsIllegalStateException() {
        //arrange
        Project project = setUp()
        getPluginExtension(project).githubUser.set(null)
        //act && assert
        assertThrowsProjectConfigWithCauseIllegalStateNotDefined("${PublishPlugin.EXTENSION_NAME}.githubUser", project)
    }

    @Test
    void evaluate_licenseWithoutShortName_throwsIllegalStateException() {
        //arrange
        Project project = setUp()
        //act && assert
        assertThrowsIllegalState("${PublishPlugin.EXTENSION_NAME}.license.shortName") {
            getPluginExtension(project).license {
                longName = "test"
                url = "http"
            }
        }
    }

    @Test
    void evaluate_licenseLongNameEmpty_throwsIllegalStateException() {
        //arrange
        Project project = setUp()
        //act && assert
        assertThrowsIllegalState("${PublishPlugin.EXTENSION_NAME}.license.longName") {
            getPluginExtension(project).license {
                shortName = "test"
                longName = ""
                url = "http"
            }
        }
    }

    @Test
    void evaluate_licenseWithoutUrl_throwsIllegalStateException() {
        //arrange
        Project project = setUp()
        //act && assert
        assertThrowsIllegalState("${PublishPlugin.EXTENSION_NAME}.license.url") {
            getPluginExtension(project).license {
                shortName = "test"
                longName = "Test License"
            }
        }
    }

    @Test
    void evaluate_licenseDistributionSetToNull_throwsIllegalStateException() {
        //arrange
        Project project = setUp()
        //act && assert
        assertThrowsIllegalState("${PublishPlugin.EXTENSION_NAME}.license.distribution") {
            getPluginExtension(project).license {
                shortName = "test"
                longName = "Test License"
                url = "http"
                distribution = null
            }
        }
    }

    @Test
    void evaluate_envNameBintrayUserUnset_throwsIllegalStateException() {
        //arrange
        Project project = setUp()
        getPluginExtension(project).envNameBintrayUser.set(null)
        //act && assert
        assertThrowsProjectConfigWithCauseIllegalStateNotDefined("${PublishPlugin.EXTENSION_NAME}.envNameBintrayUser", project)
    }

    @Test
    void evaluate_envNameBintrayApiKeyUnset_throwsIllegalStateException() {
        //arrange
        Project project = setUp()
        getPluginExtension(project).envNameBintrayApiKey.set(null)
        //act && assert
        assertThrowsProjectConfigWithCauseIllegalStateNotDefined("${PublishPlugin.EXTENSION_NAME}.envNameBintrayApiKey", project)
    }

    @Test
    void evaluate_envNameBintrayGpgPassphrase_throwsIllegalStateException() {
        //arrange
        Project project = setUp()
        getPluginExtension(project).envNameBintrayGpgPassphrase.set(null)
        //act && assert
        assertThrowsProjectConfigWithCauseIllegalStateNotDefined("${PublishPlugin.EXTENSION_NAME}.envNameBintrayGpgPassphrase", project)
    }

    @Test
    void evaluate_repoNullAndAlsoNotSetOnJFrogBintray_throwsIllegalStateException() {
        //arrange
        Project project = setUp()
        getPluginExtension(project).bintrayRepo.set(null)
        //act && assert
        assertThrowsProjectConfigWithCauseIllegalStateNotDefined("${PublishPlugin.EXTENSION_NAME}.bintrayRepo", project)
    }

    @Test
    void evaluate_repoBlankButSetOnJFrogBintray_noError() {
        //arrange
        Project project = setUp()
        getPluginExtension(project).bintrayRepo.set("  ")
        project.extensions.getByType(JFrogBintrayPluginExtension).pkg.repo = 'test'
        //act && assert no exception
    }

    @Test
    void evaluate_pkgNullAndAlsoNotSetOnJFrogBintray_noErrorCorrespondsToProjectName() {
        //arrange
        Project project = setUp()
        getPluginExtension(project).bintrayPkg.set(null)
        //act
        project.evaluate()
        //assert
        assertEquals(project.name, getBintrayExtension(project).pkg.name)
    }

    @Test
    void evaluate_pkgEmptyButSetOnJFrogBintray_noError() {
        //arrange
        Project project = setUp()
        getPluginExtension(project).bintrayPkg.set("")
        getBintrayExtension(project).pkg.name = 'test'
        //act && assert no exception
    }

    @Test
    void evaluate_envUserNullAndNotSetOnJFrogBintray_throwsIllegalStateException() {
        //arrange
        Project project = setUp()
        getBintrayExtension(project).user = null
        //act & assert
        assertThrowsProjectConfigWithCauseIllegalStateNotDefined("System.env variable with name BINTRAY_USER", project)
    }

    @Test
    void evaluate_envUserNameChangedButNullAndNotSetOnJFrogBintray_throwsIllegalStateException() {
        //arrange
        Project project = setUp()
        def extension = getPluginExtension(project)
        extension.envNameBintrayUser.set('TEST')
        getBintrayExtension(project).user = null
        //act & assert
        assertThrowsProjectConfigWithCauseIllegalStateNotDefined("System.env variable with name TEST", project)
    }

    @Test
    void evaluate_envUserNotSetButSetOnJFrogBintray_noError() {
        //arrange
        Project project = setUp()
        getBintrayExtension(project).user = 'test'
        //act && assert no exception
    }


    @Test
    void evaluate_envUserSetButNotOnJFrogBintray_noError() {
        //arrange
        Project project = setUp()
        try {
            System.setProperty('BINTRAY_USER', 'test')
            getBintrayExtension(project).user = null
            //act && assert no exception
        } finally {
            System.setProperty('BINTRAY_USER', "")
        }
    }

    @Test
    void evaluate_envUserNameChangedAndSetButNotOnJFrogBintray_noError() {
        //arrange
        Project project = setUp()
        def envName = 'TEST_USER'
        try {
            getPluginExtension(project).envNameBintrayUser.set(envName)
            System.setProperty(envName, 'test')
            getBintrayExtension(project).user = null
            //act && assert no exception
        } finally {
            System.setProperty(envName, "")
        }
    }


    @Test
    void evaluate_envApiKeyNullAndNotSetOnJFrogBintray_throwsIllegalStateException() {
        //arrange
        Project project = setUp()
        getBintrayExtension(project).key = null
        //act & assert
        assertThrowsProjectConfigWithCauseIllegalStateNotDefined("System.env variable with name BINTRAY_API_KEY", project)
    }

    @Test
    void evaluate_envApiKeyNameChangedButNullAndNotSetOnJFrogBintray_throwsIllegalStateException() {
        //arrange
        Project project = setUp()
        def extension = getPluginExtension(project)
        extension.envNameBintrayApiKey.set('TEST')
        getBintrayExtension(project).key = null
        //act & assert
        assertThrowsProjectConfigWithCauseIllegalStateNotDefined("System.env variable with name TEST", project)
    }

    @Test
    void evaluate_envApiKeyNotSetButSetOnJFrogBintray_noError() {
        //arrange
        Project project = setUp()
        getBintrayExtension(project).key = 'test'
        //act && assert no exception
    }

    @Test
    void evaluate_envApiKeySetButNotOnJFrogBintray_noError() {
        //arrange
        Project project = setUp()
        try {
            System.setProperty('BINTRAY_API_KEY', 'test')
            getBintrayExtension(project).key = null
            //act && assert no exception
        } finally {
            System.setProperty('BINTRAY_API_KEY', "")
        }
    }

    @Test
    void evaluate_envApiKeyNameChangedAndSetButNotOnJFrogBintray_noError() {
        //arrange
        Project project = setUp()
        def envName = 'TEST_USER'
        try {
            getPluginExtension(project).envNameBintrayApiKey.set(envName)
            System.setProperty(envName, 'test')
            getBintrayExtension(project).key = null
            //act && assert no exception
        } finally {
            System.setProperty(envName, "")
        }
    }

    @Test
    void evaluate_envGpgPassphraseNotSetButNoSign_noError() {
        //arrange
        Project project = setUp()
        getPluginExtension(project).signWithGpg.set(false)
        getBintrayExtension(project).pkg.version.gpg.sign = false
        //act && assert no exception
    }

    @Test
    void evaluate_envGpgPassphraseNotSetButNoSignDefinedOnJFrog_noError() {
        //arrange
        Project project = setUp()
        getBintrayExtension(project).pkg.version.gpg.sign = false
        //act && assert no exception
    }

    @Test
    void evaluate_envGpgPassphraseNotSetAndNotSetOnJFrogBintray_noError() {
        //arrange
        Project project = setUp()
        def jfrogExtension = getBintrayExtension(project)
        jfrogExtension.pkg.version.gpg.sign = true
        jfrogExtension.pkg.version.gpg.passphrase = null
        //act & assert
        assertThrowsProjectConfigWithCauseIllegalStateNotDefined("System.env variable with name BINTRAY_GPG_PASSPHRASE", project)
    }

    @Test
    void evaluate_envGpgPassphraseNameChangedButNullAndNotSetOnJFrogBintray_throwsIllegalStateException() {
        //arrange
        Project project = setUp()
        def extension = getPluginExtension(project)
        extension.envNameBintrayGpgPassphrase.set('TEST')
        def jfrogExtension = getBintrayExtension(project)
        jfrogExtension.pkg.version.gpg.sign = true
        jfrogExtension.pkg.version.gpg.passphrase = null
        //act & assert
        assertThrowsProjectConfigWithCauseIllegalStateNotDefined("System.env variable with name TEST", project)
    }

    @Test
    void evaluate_envGpgPassphraseNotSetButSetOnJFrogBintray_noError() {
        //arrange
        Project project = setUp()
        def jfroExtension = getBintrayExtension(project)
        jfroExtension.pkg.version.gpg.sign = true
        jfroExtension.pkg.version.gpg.passphrase = "test"
        //act && assert no exception
    }

    @Test
    void evaluate_envGpgPassphraseSetButNotOnJFrogBintray_noError() {
        //arrange
        Project project = setUp()
        try {
            System.setProperty('BINTRAY_GPG_PASSPHRASE', 'test')
            def jfrogExtension = getBintrayExtension(project)
            jfrogExtension.pkg.version.gpg.sign = true
            jfrogExtension.pkg.version.gpg.passphrase = null
            //act && assert no exception
        } finally {
            System.setProperty('BINTRAY_GPG_PASSPHRASE', "")
        }
    }

    @Test
    void evaluate_envGpgPassphraseNameChangedAndSetButNotOnJFrogBintray_noError() {
        //arrange
        Project project = setUp()
        def envName = 'TEST_USER'
        try {
            getPluginExtension(project).envNameBintrayGpgPassphrase.set(envName)
            System.setProperty(envName, 'test')
            def jfrogExtension = getBintrayExtension(project)
            jfrogExtension.pkg.version.gpg.sign = true
            jfrogExtension.pkg.version.gpg.passphrase = null
            //act && assert no exception
        } finally {
            System.setProperty(envName, "")
        }
    }

    private static void assertThrowsIllegalState(String what, Executable executable) {
        def exception = assertThrows(IllegalStateException) {
            executable.execute()
        }
        assertEquals(getExceptionMessage(what), exception.message)
    }

    private static void assertThrowsProjectConfigWithCauseIllegalStateNotDefined(String what, Project project) {
        assertThrowsProjectConfigExceptionWithCause(IllegalStateException, getExceptionMessage(what)) {
            project.evaluate()
        }
    }

    private static String getExceptionMessage(String what) {
        return "You need to define $what for publishing (empty or blank is considered to be undefined)"
    }
}
