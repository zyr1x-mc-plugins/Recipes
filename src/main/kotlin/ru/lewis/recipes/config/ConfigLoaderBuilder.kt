package ru.lewis.recipes.config

import com.google.inject.Inject
import com.google.inject.Singleton
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.sound.Sound
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType
import org.spongepowered.configurate.kotlin.objectMapperFactory
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import ru.lewis.recipes.config.serializer.AttributeModifierSerializer
import ru.lewis.recipes.config.serializer.AttributeSerializer
import ru.lewis.recipes.config.serializer.BossBarColorSerializer
import ru.lewis.recipes.config.serializer.BossBarOverlaySerializer
import ru.lewis.recipes.config.serializer.ColorSerializer
import ru.lewis.recipes.config.serializer.DurationSerializer
import ru.lewis.recipes.config.serializer.EnchantmentSerializer
import ru.lewis.recipes.config.serializer.IngredientSerializer
import ru.lewis.recipes.config.serializer.ItemFlagSerializer
import ru.lewis.recipes.config.serializer.MaterialSerializer
import ru.lewis.recipes.config.serializer.MiniMessageComponentSerializer
import ru.lewis.recipes.config.serializer.PotionEffectSerializer
import ru.lewis.recipes.config.serializer.PotionEffectTypeSerializer
import ru.lewis.recipes.config.serializer.PotionTypeSerializer
import ru.lewis.recipes.config.serializer.SoundSourceSerializer
import ru.lewis.recipes.config.type.MiniMessageComponent
import ru.lewis.recipes.craft.Ingredient
import java.awt.Color
import java.time.Duration

@Singleton
class ConfigLoaderBuilder @Inject constructor(
    private val materialSerializer: MaterialSerializer,
    private val miniMessageComponentSerializer: MiniMessageComponentSerializer,
    private val colorSerializer: ColorSerializer,
    private val potionEffectSerializer: PotionEffectSerializer,
    private val enchantmentSerializer: EnchantmentSerializer,
    private val attributeModifierSerializer: AttributeModifierSerializer,
    private val potionEffectTypeSerializer: PotionEffectTypeSerializer,
    private val itemFlagSerializer: ItemFlagSerializer,
    private val attributeSerializer: AttributeSerializer,
    private val potionTypeSerializer: PotionTypeSerializer,
    private val durationSerializer: DurationSerializer,
    private val soundSourceSerializer: SoundSourceSerializer,
    private val bossBarColorSerializer: BossBarColorSerializer,
    private val bossBarOverlaySerializer: BossBarOverlaySerializer,
    private val ingredientSerializer: IngredientSerializer,
) {
    fun builder(): YamlConfigurationLoader.Builder {
        return YamlConfigurationLoader.builder()
            .defaultOptions { options ->
                options.serializers { builder ->
                    builder
                        .register(PotionType::class.java, potionTypeSerializer)
                        .register(Attribute::class.java, attributeSerializer)
                        .register(ItemFlag::class.java, itemFlagSerializer)
                        .register(MiniMessageComponent::class.java, miniMessageComponentSerializer)
                        .register(Material::class.java, materialSerializer)
                        .register(Color::class.java, colorSerializer)
                        .register(PotionEffect::class.java, potionEffectSerializer)
                        .register(Enchantment::class.java, enchantmentSerializer)
                        .register(Duration::class.java, durationSerializer)
                        .register(AttributeModifier::class.java, attributeModifierSerializer)
                        .register(PotionEffectType::class.java, potionEffectTypeSerializer)
                        .register(Sound.Source::class.java, soundSourceSerializer)
                        .register(BossBar.Color::class.java, bossBarColorSerializer)
                        .register(BossBar.Overlay::class.java, bossBarOverlaySerializer)
                        .register(Ingredient::class.java, ingredientSerializer)
                        .registerAnnotatedObjects(objectMapperFactory())
                }
            }
            .indent(2)
            .nodeStyle(NodeStyle.BLOCK)
    }
}