package ru.lewis.recipes.craft

import com.google.inject.Inject
import com.google.inject.Singleton
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import ru.lewis.recipes.config.RecipeStorage
import ru.lewis.recipes.config.type.ItemTemplate
import ru.lewis.recipes.service.ItemService

@Singleton
class RecipeMatcher @Inject constructor(
    private val recipeStorage: RecipeStorage,
) {
    private val cachedRecipes = mutableListOf<CustomRecipe>()

    fun invalidateCache() {
        cachedRecipes.clear()
        cachedRecipes.addAll(recipeStorage.getAll())
    }

    fun findMatch(matrix: Array<ItemStack?>): CustomRecipe? {
        if (matrix.size != GRID_SIZE) return null

        val cached = cachedRecipes
        if (cached.isEmpty()) {
            invalidateCache()
        }

        return cached.firstOrNull { recipe -> matches(recipe, matrix) }
    }

    fun matches(recipe: CustomRecipe, matrix: Array<ItemStack?>): Boolean {
        val grid = recipe.toGrid()
        if (matrix.size != grid.size) return false

        for (i in grid.indices) {
            val ingredient = grid[i]
            val item = matrix[i]

            when {
                ingredient == null -> {
                    if (item != null && item.type != Material.AIR) return false
                }

                ingredient is Ingredient.MaterialIngredient -> {
                    if (!matchesMaterial(item, ingredient)) return false
                }

                ingredient is Ingredient.TemplateIngredient -> {
                    if (!matchesTemplate(item, ingredient)) return false
                }
            }
        }
        return true
    }

    private fun matchesMaterial(
        item: ItemStack?,
        ingredient: Ingredient.MaterialIngredient
    ): Boolean {
        if (item == null || item.type == Material.AIR) return false
        if (item.type != ingredient.material) return false
        return !hasPluginPdc(item)
    }

    private fun matchesTemplate(
        item: ItemStack?,
        ingredient: Ingredient.TemplateIngredient
    ): Boolean {
        if (item == null || item.type == Material.AIR) return false

        val meta = item.itemMeta ?: return false
        val pdc = meta.persistentDataContainer

        return ingredient.pdc.all { (key, value) ->
            pdc.get(
                NamespacedKey(ItemService.NAMESPACE, key.lowercase()),
                PersistentDataType.STRING
            ) == value
        }
    }

    fun hasPluginPdc(item: ItemStack?): Boolean {
        val meta = item?.itemMeta ?: return false
        return meta.persistentDataContainer.keys.any { key ->
            key.namespace == ItemService.NAMESPACE
        }
    }

    fun identifyIngredient(item: ItemStack?): Ingredient? {
        if (item == null || item.type == Material.AIR) return null

        val display = ItemTemplate.fromItemStack(item)

        if (hasPluginPdc(item)) {
            val pdc = linkedMapOf<String, String>()
            val meta = item.itemMeta!!
            meta.persistentDataContainer.keys
                .filter { it.namespace == ItemService.NAMESPACE }
                .forEach { key ->
                    meta.persistentDataContainer.get(key, PersistentDataType.STRING)?.let { value ->
                        pdc[key.key] = value
                    }
                }

            return Ingredient.TemplateIngredient(
                templateId = pdc.values.firstOrNull() ?: "custom",
                pdc = pdc,
                display = display
            )
        }

        return Ingredient.MaterialIngredient(
            material = item.type,
            display = display
        )
    }

    companion object {
        const val GRID_SIZE = 9
    }
}