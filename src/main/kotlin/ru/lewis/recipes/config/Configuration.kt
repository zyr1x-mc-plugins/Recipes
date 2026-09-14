package ru.lewis.recipes.config

import org.bukkit.Material
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import ru.lewis.recipes.config.type.ItemTemplate
import ru.lewis.recipes.config.type.MenuConfig
import ru.lewis.recipes.extensions.asMiniMessageComponent

@ConfigSerializable
data class Configuration(
    val recipeListMenu: MenuConfig = MenuConfig(
        title = "<gold><bold>Кастомные рецепты".asMiniMessageComponent(),
        structure = listOf(
            "a a a a a a a a a",
            ". . . . . . . . .",
            ". . . . . . . . .",
            ". . . . . . . . .",
            ". . . . . . . . .",
            "b a a a a a a a n"
        ),
        customItems = linkedMapOf(
            'a' to ItemTemplate(
                type = Material.GRAY_STAINED_GLASS_PANE,
                displayName = " ".asMiniMessageComponent()
            )
        ),
        templates = linkedMapOf(
            'b' to ItemTemplate(
                type = Material.ARROW,
                displayName = "<gray>« Назад</gray>".asMiniMessageComponent()
            ),
            'n' to ItemTemplate(
                type = Material.ARROW,
                displayName = "<gray>Вперед »</gray>".asMiniMessageComponent()
            )
        )
    ),
    val recipeViewMenu: MenuConfig = MenuConfig(
        title = "<gold><bold>Рецепт".asMiniMessageComponent(),
        structure = listOf(
            "a a a a a a a a a",
            "a i i i a r a a a",
            "a i i i a a a a a",
            "a i i i a a a a a",
            "a a a a a a a a a",
            "a a a a b a a a a"
        ),
        customItems = linkedMapOf(
            'a' to ItemTemplate(
                type = Material.GRAY_STAINED_GLASS_PANE,
                displayName = " ".asMiniMessageComponent()
            )
        ),
        templates = linkedMapOf(
            'b' to ItemTemplate(
                type = Material.ARROW,
                displayName = "<gray>« Назад</gray>".asMiniMessageComponent()
            )
        )
    ),
    val recipeCreateMenu: MenuConfig = MenuConfig(
        title = "<gold><bold>Создание рецепта".asMiniMessageComponent(),
        structure = listOf(
            "a a a a a a a a a",
            "a g g g a r a a a",
            "a g g g a a a a a",
            "a g g g a a a a a",
            "a a a a a a a a a",
            "a a a s a a a a a"
        ),
        customItems = linkedMapOf(
            'a' to ItemTemplate(
                type = Material.GRAY_STAINED_GLASS_PANE,
                displayName = " ".asMiniMessageComponent()
            )
        ),
        templates = linkedMapOf(
            's' to ItemTemplate(
                type = Material.LIME_WOOL,
                displayName = "<green>Сохранить рецепт</green>".asMiniMessageComponent()
            ),
            'g' to ItemTemplate(
                type = Material.BLACK_STAINED_GLASS_PANE,
                displayName = " ".asMiniMessageComponent()
            ),
            'r' to ItemTemplate(
                type = Material.BLACK_STAINED_GLASS_PANE,
                displayName = " ".asMiniMessageComponent()
            )
        )
    ),
)