/**
 * Precompiled [xhu.compose.multiplatform.gradle.kts][Xhu_compose_multiplatform_gradle] script plugin.
 *
 * @see Xhu_compose_multiplatform_gradle
 */
public
class Xhu_compose_multiplatformPlugin : org.gradle.api.Plugin<org.gradle.api.Project> {
    override fun apply(target: org.gradle.api.Project) {
        try {
            Class
                .forName("Xhu_compose_multiplatform_gradle")
                .getDeclaredConstructor(org.gradle.api.Project::class.java, org.gradle.api.Project::class.java)
                .newInstance(target, target)
        } catch (e: java.lang.reflect.InvocationTargetException) {
            throw e.targetException
        }
    }
}
