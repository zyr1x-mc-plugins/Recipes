package ru.lewis.recipes.listener

import com.google.inject.Inject
import com.google.inject.Singleton
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Singleton
class ChatInputListener @Inject constructor() : Listener {

    private val waitingPlayers = ConcurrentHashMap<UUID, (String) -> Unit>()

    fun awaitInput(player: Player, callback: (String) -> Unit) {
        waitingPlayers[player.uniqueId] = callback
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onChat(event: AsyncPlayerChatEvent) {
        val callback = waitingPlayers.remove(event.player.uniqueId) ?: return
        event.isCancelled = true
        callback(event.message)
    }
}