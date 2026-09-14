package ru.lewis.recipes.config.serializer

import com.google.inject.Inject
import org.bukkit.Material
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.extensions.set
import org.spongepowered.configurate.serialize.TypeSerializer
import ru.lewis.recipes.config.type.ItemTemplate
import ru.lewis.recipes.craft.Ingredient
import java.lang.reflect.Type

class IngredientSerializer @Inject constructor() : TypeSerializer<Ingredient> {

    override fun deserialize(type: Type, node: ConfigurationNode): Ingredient? {
        val ingredientType = node.node("type").string ?: return null
        val display = node.node("display").get(ItemTemplate::class.java)
            ?: return null

        return when (ingredientType) {
            "material" -> Ingredient.MaterialIngredient(
                material = node.node("material").get(Material::class.java)
                    ?: return null,
                display = display
            )

            "template" -> Ingredient.TemplateIngredient(
                templateId = node.node("templateId").string ?: "",
                pdc = LinkedHashMap<String, String>().apply {
                    node.node("pdc").childrenMap().forEach { (key, child) ->
                        this[key.toString()] = child.string ?: ""
                    }
                },
                display = display
            )

            else -> null
        }
    }

    override fun serialize(type: Type, obj: Ingredient?, node: ConfigurationNode) {
        when (obj) {
            is Ingredient.MaterialIngredient -> {
                node.node("type").set(String::class, "material")
                node.node("material").set(Material::class.java, obj.material)
                node.node("display").set(ItemTemplate::class.java, obj.display)
            }

            is Ingredient.TemplateIngredient -> {
                node.node("type").set(String::class, "template")
                node.node("templateId").set(String::class, obj.templateId)
                obj.pdc.forEach { (key, value) ->
                    node.node("pdc", key).set(String::class, value)
                }
                node.node("display").set(ItemTemplate::class.java, obj.display)
            }

            null -> node.raw(null)
        }
    }
}