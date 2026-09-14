package ru.lewis.recipes.menu

import com.google.inject.Inject
import com.google.inject.Provider
import com.google.inject.Singleton
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ru.lewis.recipes.config.RecipeStorage
import ru.lewis.recipes.config.type.MenuConfig
import ru.lewis.recipes.config.type.openMenu
import ru.lewis.recipes.config.type.templateProvider
import ru.lewis.recipes.craft.CustomRecipe
import ru.lewis.recipes.craft.RecipeMatcher
import ru.lewis.recipes.listener.ChatInputListener
import ru.lewis.recipes.service.ConfigurationService
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.inventory.VirtualInventory
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.item.impl.SimpleItem
import java.util.UUID

@Singleton
class RecipeCreateMenu @Inject constructor(
    private val configurationService: ConfigurationService,
    private val recipeStorage: RecipeStorage,
    private val recipeMatcher: RecipeMatcher,
    private val chatInputListener: ChatInputListener,
    private val recipeListMenuProvider: Provider<RecipeListMenu>,
) {
    fun open(player: Player) {
        val config = configurationService.configurationSection.recipeCreateMenu
        val gridInventory = VirtualInventory(9)
        val resultInventory = VirtualInventory(1)

        val gui = Gui.normal().apply {
            setStructure(*config.structure.toTypedArray())
            addIngredient('g', gridInventory, config.templateProvider('g'))
            addIngredient('r', resultInventory, config.templateProvider('r'))
            addIngredient('s', SimpleItem(
                config.templateProvider('s')
            ) { click ->
                handleSave(click.player, gridInventory, resultInventory, config)
            })
            config.customItems.forEach { (key, item) ->
                addIngredient(key, ItemWrapper(item.toItem()))
            }
        }.build()

        player.openMenu(config, gui)
    }

    private fun handleSave(player: Player, gridInventory: VirtualInventory, resultInventory: VirtualInventory, config: MenuConfig) {
        val resultItem = resultInventory.getItem(0)
        if (resultItem == null || resultItem.type == Material.AIR) {
            player.sendMessage("§cУкажите результат рецепта!")
            return
        }

        val gridItems = gridInventory.items
        val hasAnyIngredient = gridItems.any { it != null && it.type != Material.AIR }
        if (!hasAnyIngredient) {
            player.sendMessage("§cДобавьте хотя бы один ингредиент!")
            return
        }

        val grid = arrayOfNulls<ru.lewis.recipes.craft.Ingredient>(9)
        var ingredientCount = 0
        for (i in 0 until 9) {
            val item = gridItems[i]
            if (item != null && item.type != Material.AIR) {
                val ingredient = recipeMatcher.identifyIngredient(item)
                grid[i] = ingredient
                if (ingredient != null) ingredientCount++
            }
        }

        if (ingredientCount == 0) {
            player.sendMessage("§cНе удалось распознать ингредиенты!")
            return
        }

        player.closeInventory()
        player.sendMessage("§eВведите ID рецепта (только латиница, цифры, _):")

        chatInputListener.awaitInput(player) { input ->
            val id = input.trim().lowercase().replace(Regex("[^a-z0-9_]"), "")
            if (id.isBlank()) {
                player.sendMessage("§cНекорректный ID!")
                return@awaitInput
            }

            if (recipeStorage.getById(id) != null) {
                player.sendMessage("§cРецепт с ID §f$id §cуже существует!")
                return@awaitInput
            }

            val resultTemplate = ru.lewis.recipes.config.type.ItemTemplate.fromItemStack(resultItem)
            val recipe = CustomRecipe.fromGrid(id, grid, resultTemplate)
            recipeStorage.add(recipe)
            recipeMatcher.invalidateCache()
            player.sendMessage("§aРецепт §f$id §aуспешно создан!")
        }
    }
}