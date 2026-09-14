package ru.lewis.recipes.config.type

import org.bukkit.entity.Player
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import ru.lewis.recipes.extensions.asMiniMessageComponent
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.inventory.VirtualInventory
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.window.CartographyWindow
import xyz.xenondevs.invui.window.Window
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@ConfigSerializable
open class MenuConfig(
    val title: MiniMessageComponent = "".asMiniMessageComponent(),
    val structure: List<String> = listOf(),
    val customItems: Map<Char, ItemTemplate> = mapOf(),
    val templates: Map<Char, ItemTemplate> = mapOf(),
)

@OptIn(ExperimentalContracts::class)
inline fun Window.Builder.Normal.Single.import(
    player: Player,
    config: MenuConfig,
    guiModifier: PagedGui.Builder<Item>.() -> Unit
): PagedGui<Item> {
    contract {
        callsInPlace(guiModifier, InvocationKind.EXACTLY_ONCE)
    }

    val gui = config.createPagedGui(player).apply(guiModifier).build()
    import(config, gui)
    return gui
}

fun Window.Builder.Normal.Single.import(config: MenuConfig, gui: Gui) {
    setTitle(AdventureComponentWrapper(config.title.asComponent()))
    setGui(gui)
}

fun MenuConfig.createPagedGui(player: Player): PagedGui.Builder<Item> = PagedGui.items().apply {
    setStructure(*this@createPagedGui.structure.toTypedArray())
    addIngredient('.', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
    this@createPagedGui.customItems.forEach { (key, item) -> addIngredient(key, ItemWrapper(item.toItem())) }
}

fun MenuConfig.createNormalGui(): Gui.Builder.Normal = Gui.normal().apply {
    setStructure(*this@createNormalGui.structure.toTypedArray())
    this@createNormalGui.customItems.forEach { (key, item) -> addIngredient(key, ItemWrapper(item.toItem())) }
}

fun Player.openMenu(config: MenuConfig, gui: Gui) {
    Window.single()
        .setViewer(this)
        .setTitle(AdventureComponentWrapper(config.title.asComponent()))
        .setGui(gui)
        .build()
        .open()
}

fun MenuConfig.templateProvider(char: Char): ItemProvider {
    val template = templates[char] ?: return ItemProvider.EMPTY
    return ItemWrapper(template.toItem())
}