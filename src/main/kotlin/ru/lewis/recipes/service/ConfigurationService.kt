package ru.lewis.recipes.service

import com.google.inject.Inject
import com.google.inject.Singleton
import org.bukkit.plugin.Plugin
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.extensions.set
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import ru.lewis.recipes.config.ConfigLoaderBuilder
import ru.lewis.recipes.config.Configuration
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

@Singleton
class ConfigurationService @Inject constructor(
    private val plugin: Plugin,
    private val configLoaderBuilder: ConfigLoaderBuilder,
) {
    private val defaultPath get(): Path = plugin.dataFolder.toPath()

    lateinit var configurationSection: Configuration
        private set

    fun run() {
        configurationSection = loadConfig(configLoaderBuilder.builder(), "config")
    }

    private inline fun <reified T : Any> YamlConfigurationLoader.getAndSave(): T {
        val node = this.load()

        var obj = node.get(T::class)

        if (obj == null) {
            plugin.logger.warning("Config ${T::class.simpleName} is empty, creating default...")
            obj = try {
                T::class.java.getDeclaredConstructor().newInstance()
            } catch (e: Exception) {
                throw IllegalStateException("Failed to create default config for ${T::class.simpleName}. Make sure class has a no-arg constructor or default values.", e)
            }
        }

        node.set(T::class, obj)
        this.save(node)
        return obj
    }

    private inline fun <reified T : Any> loadConfig(
        builder: YamlConfigurationLoader.Builder,
        name: String,
        subfolder: String? = null
    ): T {
        val path = if (subfolder != null) {
            defaultPath.resolve("$subfolder${File.separator}$name.yml")
        } else {
            defaultPath.resolve("$name.yml")
        }

        path.parent?.let { Files.createDirectories(it) }

        val loader = builder.path(path).build()

        return try {
            loader.getAndSave()
        } catch (e: Exception) {
            plugin.logger.severe("Failed to load config: $name.yml")
            plugin.logger.severe("Error: ${e.message}")

            val resourcePath = if (subfolder != null) {
                "$subfolder/$name.yml"
            } else {
                "$name.yml"
            }

            plugin.logger.info("Attempting to create default config from resources: $resourcePath")

            try {
                plugin.getResource(resourcePath)?.use { input ->
                    Files.copy(input, path, StandardCopyOption.REPLACE_EXISTING)
                    plugin.logger.info("Created default config: $name.yml")
                    return loader.getAndSave()
                }
            } catch (resourceError: Exception) {
                plugin.logger.warning("No default config found in resources for: $resourcePath")
            }

            throw IllegalStateException("Failed to load config: $name.yml. Please create it manually or check the example configs.", e)
        }
    }
}