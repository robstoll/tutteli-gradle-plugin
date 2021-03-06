package ch.tutteli.gradle.publish


import com.jfrog.bintray.gradle.BintrayExtension
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

import static ch.tutteli.gradle.publish.Validation.throwIllegalPropertyNorSystemEnvSet

class ValidateBeforeUploadTask extends DefaultTask {
    @TaskAction
    def validate() {
        def extension = project.extensions.getByName(PublishPlugin.EXTENSION_NAME)

        def bintrayExtension = project.extensions.getByType(BintrayExtension)
        if (!bintrayExtension.user?.trim()) throw throwIllegalPropertyNorSystemEnvSet(extension.propNameBintrayUser, extension.envNameBintrayUser)
        if (!bintrayExtension.key?.trim()) throw throwIllegalPropertyNorSystemEnvSet(extension.propNameBintrayApiKey, extension.envNameBintrayApiKey)
    }
}
