package ru.lewis.recipes.menu

import com.google.inject.Inject
import com.google.inject.Provider
import com.google.inject.Singleton
import org.bukkit.entity.Player
import ru.lewis.recipes.config.RecipeStorage
import ru.lewis.recipes.config.type.ItemTemplate
import ru.lewis.recipes.config.type.MenuConfig
import ru.lewis.recipes.config.type.openMenu
import ru.lewis.recipes.config.type.templateProvider
import ru.lewis.recipes.service.ConfigurationService
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.item.impl.SimpleItem
import xyz.xenondevs.invui.item.impl.controlitem.PageItem

@Singleton
class RecipeListMenu @Inject constructor(
    private val configurationService: ConfigurationService,
    private val recipeStorage: RecipeStorage,
    private val recipeViewMenuProvider: Provider<RecipeViewMenu>,
) {
    fun open(player: Player) {
        val config = configurationService.configurationSection.recipeListMenu
        val recipes = recipeStorage.getAll()

        val items = recipes.map { recipe ->
            SimpleItem(recipe.result.toItem()) { click ->
                recipeViewMenuProvider.get().open(click.player, recipe.id)
            }
        }

        val gui = PagedGui.items().apply {
            setStructure(*config.structure.toTypedArray())
            addIngredient('.', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            addIngredient('b', PreviousPageItem(config.templateProvider('b')))
            addIngredient('n', NextPageItem(config.templateProvider('n')))
            config.customItems.forEach { (key, item) ->
                addIngredient(key, ItemWrapper(item.toItem()))
            }
            setContent(items)
        }.build()

        player.openMenu(config, gui)
    }

    private class NextPageItem(private val provider: ItemProvider) : PageItem(true) {
        override fun getItemProvider(pagedGui: PagedGui<*>): ItemProvider = provider
    }

    private class PreviousPageItem(private val provider: ItemProvider) : PageItem(false) {
        override fun getItemProvider(pagedGui: PagedGui<*>): ItemProvider = provider
    }
}