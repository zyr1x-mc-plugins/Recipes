package ru.lewis.recipes.listener

import com.google.inject.Inject
import com.google.inject.Singleton
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.inventory.CraftingInventory
import org.bukkit.inventory.ItemStack
import ru.lewis.recipes.craft.CustomRecipe
import ru.lewis.recipes.craft.RecipeMatcher

@Singleton
class CraftingListener @Inject constructor(
    private val recipeMatcher: RecipeMatcher,
) : Listener {

    @EventHandler
    fun onPrepareCraft(event: PrepareItemCraftEvent) {
        val matrix = event.inventory.matrix
        val match = recipeMatcher.findMatch(matrix) ?: return
        event.inventory.result = match.result.toItem()
    }

    @EventHandler
    fun onCraftClick(event: InventoryClickEvent) {
        if (event.inventory !is CraftingInventory) return
        if (event.rawSlot != RESULT_SLOT) return

        val player = event.whoClicked as? Player ?: return
        val craftingInventory = event.inventory as CraftingInventory
        val matrix = craftingInventory.matrix

        val recipe = recipeMatcher.findMatch(matrix) ?: return
        val resultItem = recipe.result.toItem()

        event.isCancelled = true

        if (event.isShiftClick) {
            handleShiftCraft(player, craftingInventory, recipe, resultItem, matrix)
        } else {
            handleNormalCraft(player, event, craftingInventory, resultItem, matrix)
        }

        player.updateInventory()
    }

    private fun handleNormalCraft(
        player: Player,
        event: InventoryClickEvent,
        craftingInventory: CraftingInventory,
        resultItem: ItemStack,
        matrix: Array<ItemStack?>,
    ) {
        val cursor = event.cursor

        if (cursor.type != Material.AIR && !resultItem.isSimilar(cursor)) return
        if (cursor.type != Material.AIR && cursor.amount + resultItem.amount > cursor.maxStackSize) return

        val ingredientSlots = getIngredientSlots(matrix)
        if (!consumeSlots(craftingInventory, matrix, ingredientSlots)) return

        event.view.setCursor(resultItem.clone())
        player.updateInventory()
    }

    private fun handleShiftCraft(
        player: Player,
        craftingInventory: CraftingInventory,
        recipe: CustomRecipe,
        resultItem: ItemStack,
        matrix: Array<ItemStack?>,
    ) {
        val ingredientSlots = getIngredientSlots(matrix)
        val maxCrafts = calculateMaxCrafts(player, recipe, resultItem, matrix, ingredientSlots)
        if (maxCrafts <= 0) return

        val totalResult = resultItem.clone().apply { amount = resultItem.amount * maxCrafts }

        val available = player.inventory.addItem(totalResult)
        if (available.isNotEmpty()) return

        consumeMultiple(craftingInventory, matrix, ingredientSlots, maxCrafts)
        player.updateInventory()
    }

    private fun getIngredientSlots(matrix: Array<ItemStack?>): List<Int> {
        return matrix.indices.filter { i ->
            val item = matrix[i]
            item != null && item.type != Material.AIR
        }
    }

    private fun calculateMaxCrafts(
        player: Player,
        recipe: CustomRecipe,
        resultItem: ItemStack,
        matrix: Array<ItemStack?>,
        ingredientSlots: List<Int>,
    ): Int {
        if (ingredientSlots.isEmpty()) return 0

        var maxCrafts = Int.MAX_VALUE
        for (slot in ingredientSlots) {
            val item = matrix.getOrNull(slot) ?: return 0
            if (item.type == Material.AIR) return 0
            maxCrafts = minOf(maxCrafts, item.amount)
        }

        val maxFitInInventory = calculateInventorySpace(player, resultItem)
        val maxByInventory = maxFitInInventory / resultItem.amount

        return minOf(maxCrafts, maxByInventory).coerceAtMost(MAX_SHIFT_CRAFTS)
    }

    private fun calculateInventorySpace(player: Player, item: ItemStack): Int {
        var space = 0
        for (slot in player.inventory.contents) {
            if (slot == null || slot.type == Material.AIR) {
                space += item.maxStackSize
            } else if (slot.isSimilar(item)) {
                space += item.maxStackSize - slot.amount
            }
        }
        return space
    }

    private fun consumeSlots(
        craftingInventory: CraftingInventory,
        matrix: Array<ItemStack?>,
        slots: List<Int>,
    ): Boolean {
        for (slot in slots) {
            val item = matrix.getOrNull(slot) ?: return false
            if (item.type == Material.AIR) return false

            item.amount -= 1
            if (item.amount <= 0) {
                craftingInventory.setItem(slot + 1, null)
            } else {
                craftingInventory.setItem(slot + 1, item)
            }
        }
        return true
    }

    private fun consumeMultiple(
        craftingInventory: CraftingInventory,
        matrix: Array<ItemStack?>,
        slots: List<Int>,
        count: Int,
    ) {
        for (slot in slots) {
            val item = matrix.getOrNull(slot) ?: continue

            item.amount -= count
            if (item.amount <= 0) {
                craftingInventory.setItem(slot + 1, null)
            } else {
                craftingInventory.setItem(slot + 1, item)
            }
        }
    }

    companion object {
        private const val RESULT_SLOT = 0
        private const val MAX_SHIFT_CRAFTS = 64
    }
}