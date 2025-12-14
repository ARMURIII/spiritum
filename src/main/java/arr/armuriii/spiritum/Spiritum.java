package arr.armuriii.spiritum;

import arr.armuriii.spiritum.entity.SpiritEntity;
import arr.armuriii.spiritum.init.*;
import arr.armuriii.spiritum.rituals.Ritual;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.registry.*;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Spiritum implements ModInitializer {
	public static final String MOD_ID = "spiritum";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final BooleanProperty FLESH = BooleanProperty.of("flesh");
	public static final BooleanProperty ACTIVE = BooleanProperty.of("active");

	public static final RegistryKey<Registry<Ritual>> RITUAL_KEY = RegistryKey.ofRegistry(id("ritual"));

	public static final SimpleRegistry<Ritual> RITUAL =
			FabricRegistryBuilder.createSimple(RITUAL_KEY).buildAndRegister();

	public static final RegistryKey<PlacedFeature> ORE_HEXSTONE_PLACED_KEY = RegistryKey.of(
			RegistryKeys.PLACED_FEATURE, id("ore_hexstone"));

	public static final RegistryKey<PlacedFeature> ORE_SILVERED_HEXSTONE_PLACED_KEY = RegistryKey.of(
			RegistryKeys.PLACED_FEATURE, id("ore_silvered_hexstone"));

	@Override
	public void onInitialize() {

		SpiritumItems.register();
		SpiritumBlocks.register();
		SpiritumEntities.register();
		SpiritumRituals.register();
		SpiritumDamageTypes.register();
		SpiritumPotions.register();
		SpiritumRecipes.register();
		SpiritumParticles.register();

		ServerLivingEntityEvents.AFTER_DEATH.register(((self, damageSource) -> {
			if (self.getType().isIn(SpiritumEntities.HAS_SOUL) && self.hasStatusEffect(SpiritumPotions.LETHARGY)) {
				SpiritEntity spirit = SpiritumEntities.SPIRIT.create(self.getWorld());
                if (spirit != null) {
                    spirit.setPosition(self.getPos());
                    self.getWorld().spawnEntity(spirit);
                }
            }
		}));

		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, ORE_HEXSTONE_PLACED_KEY);
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, ORE_SILVERED_HEXSTONE_PLACED_KEY);
	}

	public static Identifier id(String id) {
		return Identifier.of(MOD_ID,id);
	}
}