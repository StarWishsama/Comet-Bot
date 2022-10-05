import org.gradle.api.Project
import org.gradle.internal.os.OperatingSystem
import java.io.File
import java.nio.file.Files

fun Project.installGitHooks() {
    val target = File(project.rootProject.rootDir, ".git/hooks")
    val source = File(project.rootProject.rootDir, ".git-hooks")
    if (target.canonicalFile == source) return
    target.deleteRecursively()
    if (OperatingSystem.current().isWindows) {
        source.copyRecursively(target)
    } else {
        Files.createSymbolicLink(target.toPath(), source.toPath())
    }
}
