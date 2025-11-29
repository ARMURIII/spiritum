package arr.armuriii.spiritum;

import arr.armuriii.spiritum.init.*;
import arr.armuriii.spiritum.rituals.Ritual;
import arr.armuriii.spiritum.worldgen.RecursiveOreFeature;
import arr.armuriii.spiritum.worldgen.RecursiveOreFeatureConfig;
import com.mojang.datafixers.DataFixerBuilder;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.registry.*;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.structure.rule.RuleTestType;
import net.minecraft.structure.rule.TagMatchRuleTest;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.blockpredicate.BlockPredicateType;
import net.minecraft.world.gen.feature.OreFeature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Spiritum implements ModInitializer {
	public static final String MOD_ID = "spiritum";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final BooleanProperty FLESH = BooleanProperty.of("flesh");

	public static final RegistryKey<Registry<Ritual>> RITUAL_KEY = RegistryKey.ofRegistry(id("ritual"));

	public static final SimpleRegistry<Ritual> RITUAL =
			FabricRegistryBuilder.createSimple(RITUAL_KEY).buildAndRegister();

	public static final RegistryKey<PlacedFeature> ORE_HEXSTONE_PLACED_KEY = RegistryKey.of(
			RegistryKeys.PLACED_FEATURE, id("ore_hexstone"));

	public static final RegistryKey<PlacedFeature> ORE_SILVERED_HEXSTONE_PLACED_KEY = RegistryKey.of(
			RegistryKeys.PLACED_FEATURE, id("ore_silvered_hexstone"));

	/*public static final Identifier RECURSIVE_ORE_FEATURE_ID = id("recursive_ore");
	public static final RecursiveOreFeature RECURSIVE_ORE_FEATURE = new RecursiveOreFeature(RecursiveOreFeatureConfig.CODEC);*/

	@Override
	public void onInitialize() {
		SpiritumItems.register();
		SpiritumBlocks.register();
		SpiritumEntities.register();
		SpiritumRituals.register();
		SpiritumDamageTypes.register();
		SpiritumPotions.register();
		SpiritumRecipes.register();


		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, ORE_HEXSTONE_PLACED_KEY);
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, ORE_SILVERED_HEXSTONE_PLACED_KEY);
	}

	public static Identifier id(String id) {
		return Identifier.of(MOD_ID,id);
	}
}