import org.gradle.api.Project
import java.io.File
import java.nio.file.Files

fun Project.installGitHooks() {
    if (!File("./.git").exists()) return
    val target = File(rootProject.rootDir, ".git/hooks")
    val source = File(rootProject.rootDir, ".git-hooks")
    if (target.canonicalFile == source) return
    target.deleteRecursively()
    Files.createSymbolicLink(target.toPath(), source.toPath())
}
