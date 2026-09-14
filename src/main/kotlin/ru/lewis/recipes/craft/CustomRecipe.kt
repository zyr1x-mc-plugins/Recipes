package ru.lewis.recipes.craft

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import ru.lewis.recipes.config.type.ItemTemplate

@ConfigSerializable
data class CustomRecipe(
    val id: String,
    val ingredients: Map<String, Ingredient>,
    val result: ItemTemplate,
) {
    fun toGrid(): Array<Ingredient?> {
        val grid = arrayOfNulls<Ingredient>(9)
        ingredients.forEach { (slot, ingredient) ->
            grid[slot.toInt()] = ingredient
        }
        return grid
    }

    companion object {
        fun fromGrid(id: String, grid: Array<Ingredient?>, result: ItemTemplate): CustomRecipe {
            val ingredients = LinkedHashMap<String, Ingredient>()
            grid.forEachIndexed { index, ingredient ->
                if (ingredient != null) {
                    ingredients["$index"] = ingredient
                }
            }
            return CustomRecipe(
                id = id,
                ingredients = ingredients,
                result = result
            )
        }
    }
}