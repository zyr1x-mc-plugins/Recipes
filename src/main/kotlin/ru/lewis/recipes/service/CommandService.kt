package ru.lewis.recipes.service

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.rollczi.litecommands.LiteCommands
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin
import ru.lewis.recipes.command.RecipeCommand

@Singleton
class CommandService @Inject constructor(
    private val plugin: Plugin,
    private val recipeCommand: RecipeCommand,
) {
    private lateinit var commands: LiteCommands<CommandSender>

    fun register() {
        commands = LiteBukkitFactory.builder(plugin.name, plugin)
            .annotations { }
            .commands(recipeCommand)
            .build()
    }

    fun unregister() {
        commands.unregister()
    }
}