package ru.lewis.recipes.command

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.rollczi.litecommands.annotations.argument.Arg
import dev.rollczi.litecommands.annotations.command.Command
import dev.rollczi.litecommands.annotations.context.Sender
import dev.rollczi.litecommands.annotations.description.Description
import dev.rollczi.litecommands.annotations.execute.Execute
import dev.rollczi.litecommands.annotations.permission.Permission
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import ru.lewis.recipes.config.RecipeStorage
import ru.lewis.recipes.craft.RecipeMatcher
import ru.lewis.recipes.menu.RecipeCreateMenu
import ru.lewis.recipes.menu.RecipeListMenu
import ru.lewis.recipes.service.ConfigurationService
import ru.lewis.recipes.service.ItemService

@Command(name = "recipes", aliases = ["crafts"])
@Description("Управление рецептами")
@Singleton
class RecipeCommand @Inject constructor(
    private val recipeListMenu: RecipeListMenu,
    private val recipeCreateMenu: RecipeCreateMenu,
    private val recipeStorage: RecipeStorage,
    private val recipeMatcher: RecipeMatcher,
    private val configurationService: ConfigurationService,
) {

    @Execute
    fun openList(@Sender player: Player) {
        recipeListMenu.open(player)
    }

    @Execute(name = "create")
    @Permission("recipes.admin")
    fun openCreate(@Sender player: Player) {
        recipeCreateMenu.open(player)
    }

    @Execute(name = "delete")
    @Permission("recipes.admin")
    fun delete(@Sender player: Player, @Arg("id") recipeId: String) {
        if (recipeStorage.remove(recipeId)) {
            recipeMatcher.invalidateCache()
            player.sendMessage("§aРецепт §f$recipeId §aудалён!")
        } else {
            player.sendMessage("§cРецепт с ID §f$recipeId §cне найден!")
        }
    }

    @Execute(name = "reload")
    @Permission("recipes.admin")
    fun reload(@Sender player: Player) {
        configurationService.run()
        recipeStorage.load()
        recipeMatcher.invalidateCache()
        player.sendMessage("§aКонфигурация и рецепты перезагружены!")
    }

    @Execute(name = "tag")
    @Permission("recipes.admin")
    fun tag(@Sender player: Player, @Arg("key") key: String, @Arg("value") value: String) {
        val item = player.inventory.itemInMainHand
        if (item.type == Material.AIR) {
            player.sendMessage("§cДержите предмет в руке!")
            return
        }

        val meta = item.itemMeta ?: return
        meta.persistentDataContainer.set(
            NamespacedKey(ItemService.NAMESPACE, key.lowercase()),
            PersistentDataType.STRING,
            value
        )
        item.itemMeta = meta
        player.sendMessage("§aPDC §f${key.lowercase()} §a= §f$value §aналожено на предмет!")
    }

    @Execute(name = "untag")
    @Permission("recipes.admin")
    fun untag(@Sender player: Player, @Arg("key") key: String) {
        val item = player.inventory.itemInMainHand
        if (item.type == Material.AIR) {
            player.sendMessage("§cДержите предмет в руке!")
            return
        }

        val meta = item.itemMeta ?: return
        val pdc = meta.persistentDataContainer
        val tagKey = NamespacedKey(ItemService.NAMESPACE, key.lowercase())
        val existed = pdc.get(tagKey, PersistentDataType.STRING) != null
        pdc.remove(tagKey)
        item.itemMeta = meta

        if (existed) {
            player.sendMessage("§aPDC §f${key.lowercase()} §aудалён с предмета!")
        } else {
            player.sendMessage("§cPDC §f${key.lowercase()} §cне найден на предмете!")
        }
    }

    @Execute(name = "tags")
    @Permission("recipes.admin")
    fun tags(@Sender player: Player) {
        val item = player.inventory.itemInMainHand
        if (item.type == Material.AIR) {
            player.sendMessage("§cДержите предмет в руке!")
            return
        }

        val meta = item.itemMeta ?: return
        val pdc = meta.persistentDataContainer

        val entries = pdc.keys
            .filter { it.namespace == ItemService.NAMESPACE }
            .mapNotNull { key ->
                val value = pdc.get(key, PersistentDataType.STRING) ?: return@mapNotNull null
                "§7${key.key} §f= §e$value"
            }

        if (entries.isEmpty()) {
            player.sendMessage("§7На предмете нет PDC под namespace §f${ItemService.NAMESPACE}§7.")
        } else {
            player.sendMessage("§6PDC предмета:")
            entries.forEach { player.sendMessage(it) }
        }
    }
}