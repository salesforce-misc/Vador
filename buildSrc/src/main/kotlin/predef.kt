import org.gradle.api.artifacts.ExternalModuleDependencyBundle
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.plugin.use.PluginDependency

val Provider<PluginDependency>.pluginId: String
  get() = get().pluginId

infix fun <T> Property<T>.by(value: T) {
  set(value)
}

internal val VersionCatalog.kotestBundle: Provider<ExternalModuleDependencyBundle>
  get() = getBundle("kotest")

private fun VersionCatalog.getLibrary(library: String) = findLibrary(library).get()

private fun VersionCatalog.getBundle(bundle: String) = findBundle(bundle).get()

private fun VersionCatalog.getPlugin(plugin: String) = findPlugin(plugin).get()
