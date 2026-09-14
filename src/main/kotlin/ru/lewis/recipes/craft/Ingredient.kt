package ru.lewis.recipes.craft

import org.bukkit.Material
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import ru.lewis.recipes.config.type.ItemTemplate

@ConfigSerializable
sealed class Ingredient {

    abstract val type: String
    abstract val display: ItemTemplate

    @ConfigSerializable
    data class MaterialIngredient(
        val material: Material,
        override val display: ItemTemplate,
    ) : Ingredient() {
        override val type: String = "material"
    }

    @ConfigSerializable
    data class TemplateIngredient(
        val templateId: String,
        val pdc: Map<String, String>,
        override val display: ItemTemplate,
    ) : Ingredient() {
        override val type: String = "template"
    }
}