package ru.lewis.recipes.config

import com.google.inject.Inject
import com.google.inject.Singleton
import org.bukkit.plugin.Plugin
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.extensions.set
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import ru.lewis.recipes.craft.CustomRecipe

@Singleton
class RecipeStorage @Inject constructor(
    private val plugin: Plugin,
    private val configLoaderBuilder: ConfigLoaderBuilder,
) {
    private val recipes = LinkedHashMap<String, CustomRecipe>()

    private val loader: YamlConfigurationLoader by lazy {
        configLoaderBuilder.builder()
            .path(plugin.dataFolder.toPath().resolve("recipes.yml"))
            .build()
    }

    fun load() {
        recipes.clear()

        val node = try {
            loader.load()
        } catch (e: Exception) {
            plugin.logger.severe("Failed to load recipes.yml: ${e.message}")
            return
        }

        val recipeList = node.node("recipes").get(RecipeList::class.java)
            ?: return

        recipeList.recipes.forEach { recipe ->
            recipes[recipe.id] = recipe
        }
    }

    fun save() {
        val node = loader.createNode()
        node.node("recipes").set(
            RecipeList::class.java,
            RecipeList(recipes.values.toList())
        )
        loader.save(node)
    }

    fun getAll(): List<CustomRecipe> = recipes.values.toList()

    fun getById(id: String): CustomRecipe? = recipes[id]

    fun add(recipe: CustomRecipe) {
        recipes[recipe.id] = recipe
        save()
    }

    fun remove(id: String): Boolean {
        val removed = recipes.remove(id) != null
        if (removed) save()
        return removed
    }
}

@ConfigSerializable
data class RecipeList(
    val recipes: List<CustomRecipe> = listOf()
)