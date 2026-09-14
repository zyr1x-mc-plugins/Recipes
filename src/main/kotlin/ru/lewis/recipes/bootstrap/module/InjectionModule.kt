package ru.lewis.recipes.bootstrap.module

import com.google.inject.AbstractModule
import com.google.inject.Provides
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.plugin.Plugin

class InjectionModule(
    private val plugin: Plugin
) : AbstractModule() {

    override fun configure() {
    }

    @Provides
    fun providePlugin(): Plugin = plugin

    @Provides
    fun MiniMessage(): MiniMessage = MiniMessage.miniMessage()
}