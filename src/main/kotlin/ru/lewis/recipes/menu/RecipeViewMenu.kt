package ru.lewis.recipes.menu

import com.google.inject.Inject
import com.google.inject.Provider
import com.google.inject.Singleton
import org.bukkit.entity.Player
import ru.lewis.recipes.config.RecipeStorage
import ru.lewis.recipes.config.type.createNormalGui
import ru.lewis.recipes.config.type.openMenu
import ru.lewis.recipes.config.type.templateProvider
import ru.lewis.recipes.service.ConfigurationService
import xyz.xenondevs.invui.item.impl.SimpleItem

@Singleton
class RecipeViewMenu @Inject constructor(
    private val configurationService: ConfigurationService,
    private val recipeStorage: RecipeStorage,
    private val recipeListMenuProvider: Provider<RecipeListMenu>,
) {
    fun open(player: Player, recipeId: String) {
        val recipe = recipeStorage.getById(recipeId) ?: return
        val config = configurationService.configurationSection.recipeViewMenu

        val slots = config.slotIndices()
        val gui = config.createNormalGui().build()

        slots['i']?.forEachIndexed { index, slot ->
            val ingredient = recipe.toGrid().getOrNull(index)
            if (ingredient != null) {
                gui.setItem(slot, SimpleItem(ingredient.display.toItem()))
            }
        }

        slots['r']?.forEach { slot ->
            gui.setItem(slot, SimpleItem(recipe.result.toItem()))
        }

        slots['b']?.forEach { slot ->
            gui.setItem(slot, SimpleItem(
                config.templateProvider('b')
            ) { click ->
                recipeListMenuProvider.get().open(click.player)
            })
        }

        player.openMenu(config, gui)
    }
}

private fun ru.lewis.recipes.config.type.MenuConfig.slotIndices(): Map<Char, List<Int>> {
    val result = mutableMapOf<Char, MutableList<Int>>()
    structure.forEachIndexed { rowIdx, row ->
        val tokens = row.split(" ")
        tokens.forEachIndexed { colIdx, token ->
            if (token.length == 1 && token[0] != '.') {
                result.getOrPut(token[0]) { mutableListOf() }.add(rowIdx * 9 + colIdx)
            }
        }
    }
    return result
}