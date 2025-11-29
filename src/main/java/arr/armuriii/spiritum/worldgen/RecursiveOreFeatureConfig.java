package arr.armuriii.spiritum.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig;

import java.util.List;

public record RecursiveOreFeatureConfig(List<OreFeatureConfig.Target> targets, int size, float discardOnAirChance, OreFeatureConfig recursiveConfig) implements FeatureConfig {

    public static final Codec<RecursiveOreFeatureConfig> CODEC = RecordCodecBuilder.create(
            (instance) -> instance.group(
                    Codec.list(OreFeatureConfig.Target.CODEC).fieldOf("targets").forGetter((config) -> config.targets),
                    Codec.intRange(0, 64).fieldOf("size").forGetter((config) -> config.size),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("discard_chance_on_air_exposure")
                            .forGetter((config) -> config.discardOnAirChance),
                    OreFeatureConfig.CODEC.fieldOf("reconfig").forGetter((config)-> config.recursiveConfig)
            ).apply(instance, RecursiveOreFeatureConfig::new));

}
