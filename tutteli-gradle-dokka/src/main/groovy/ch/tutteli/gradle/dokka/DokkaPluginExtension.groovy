package ch.tutteli.gradle.dokka

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.jetbrains.dokka.gradle.DokkaTask

class DokkaPluginExtension {
    protected static final String DEFAULT_REPO_URL = "repoUrlWillBeReplacedDuringProjectEvaluation"
    private DokkaTask dokkaTask

    Property<String> repoUrl
    Property<String> githubUser
    Property<Boolean> ghPages

    DokkaPluginExtension(Project project) {
        dokkaTask = project.tasks.getByName('dokka') as DokkaTask
        repoUrl = project.objects.property(String)
        githubUser = project.objects.property(String)
        ghPages = project.objects.property(Boolean)
        ghPages.set(false)

        dokka {
            outputFormat = 'html'
            outputDirectory = "$project.buildDir/kdoc"
            linkMapping {
                dir = project.rootProject.projectDir.absolutePath
                url = DEFAULT_REPO_URL
                suffix = '#L'
            }
        }
    }

    void dokka(Action<DokkaTask> configure) {
        configure.execute(dokkaTask)
    }
}

