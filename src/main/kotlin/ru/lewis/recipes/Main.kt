package ru.lewis.recipes

import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.bukkit.plugin.Plugin
import ru.lewis.recipes.config.RecipeStorage
import ru.lewis.recipes.listener.ChatInputListener
import ru.lewis.recipes.listener.CraftingListener
import ru.lewis.recipes.service.CommandService
import ru.lewis.recipes.service.ConfigurationService
import xyz.xenondevs.invui.InvUI

@Singleton
class Main @Inject constructor(
    private val plugin: Plugin,
    private val configurationService: ConfigurationService,
    private val commandService: CommandService,
    private val recipeStorage: RecipeStorage,
    private val craftingListener: CraftingListener,
    private val chatInputListener: ChatInputListener,
) {
    fun enable() {
        InvUI.getInstance().setPlugin(plugin)
        configurationService.run()
        recipeStorage.load()
        commandService.register()
        registerListeners()
    }

    fun disable() {
        recipeStorage.save()
        commandService.unregister()
    }

    private fun registerListeners() {
        val pluginManager = plugin.server.pluginManager
        pluginManager.registerEvents(craftingListener, plugin)
        pluginManager.registerEvents(chatInputListener, plugin)
    }
}
