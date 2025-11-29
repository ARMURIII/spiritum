package arr.armuriii.spiritum.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ChunkSectionCache;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.BitSet;
import java.util.Objects;
import java.util.function.Function;

public class RecursiveOreFeature extends Feature<RecursiveOreFeatureConfig> {

    public RecursiveOreFeature(Codec<RecursiveOreFeatureConfig> codec) {
        super(codec);
    }

    public boolean generate(FeatureContext<RecursiveOreFeatureConfig> context) {
        Random random = context.getRandom();
        BlockPos blockPos = context.getOrigin();
        StructureWorldAccess world = context.getWorld();
        RecursiveOreFeatureConfig config = context.getConfig();
        float wavingRandom = random.nextFloat() * (float)Math.PI;
        float eightiethSize = (float)config.size() / 8.0F;
        int chunkSize = MathHelper.ceil(((float)config.size() / 16.0F * 2.0F + 1.0F) / 2.0F);
        double startX = (double)blockPos.getX() + Math.sin(wavingRandom) * (double)eightiethSize;
        double endX = (double)blockPos.getX() - Math.sin(wavingRandom) * (double)eightiethSize;
        double startZ = (double)blockPos.getZ() + Math.cos(wavingRandom) * (double)eightiethSize;
        double endZ = (double)blockPos.getZ() - Math.cos(wavingRandom) * (double)eightiethSize;
        double startY = blockPos.getY() + random.nextInt(3) - 2;
        double endY = blockPos.getY() + random.nextInt(3) - 2;
        int n = blockPos.getX() - MathHelper.ceil(eightiethSize) - chunkSize;
        int o = blockPos.getY() - 2 - chunkSize;
        int p = blockPos.getZ() - MathHelper.ceil(eightiethSize) - chunkSize;
        int width = 2 * (MathHelper.ceil(eightiethSize) + chunkSize);
        int height = 2 * (2 + chunkSize);

        for(int s = n; s <= n + width; ++s) {
            for(int t = p; t <= p + width; ++t) {
                if (o <= world.getTopY(Heightmap.Type.OCEAN_FLOOR_WG, s, t)) {
                    return this.generateVeinPart(world, random, config, startX, endX, startZ, endZ, startY, endY, n, o, p, width, height);
                }
                if (random.nextInt(32) == 16) {
                    FeatureContext<OreFeatureConfig> recontext = new FeatureContext<>(context.getFeature(),world, context.getGenerator(),random,blockPos,config.recursiveConfig());
                    new OreFeature(OreFeatureConfig.CODEC).generate(recontext);
                }
            }
        }

        return false;
    }

    protected boolean generateVeinPart(StructureWorldAccess world, Random random, RecursiveOreFeatureConfig config, double startX, double endX, double startZ, double endZ, double startY, double endY, int x, int y, int z, int horizontalSize, int verticalSize) {
        int i = 0;
        BitSet bitSet = new BitSet(horizontalSize * verticalSize * horizontalSize);
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        int j = config.size();
        double[] ds = new double[j * 4];

        for(int k = 0; k < j; ++k) {
            float f = (float)k / (float)j;
            double d = MathHelper.lerp(f, startX, endX);
            double e = MathHelper.lerp(f, startY, endY);
            double g = MathHelper.lerp(f, startZ, endZ);
            double h = random.nextDouble() * (double)j / (double)16.0F;
            double l = ((double)(MathHelper.sin((float)Math.PI * f) + 1.0F) * h + (double)1.0F) / (double)2.0F;
            ds[k * 4] = d;
            ds[k * 4 + 1] = e;
            ds[k * 4 + 2] = g;
            ds[k * 4 + 3] = l;
        }

        for(int k = 0; k < j - 1; ++k) {
            if (!(ds[k * 4 + 3] <= (double)0.0F)) {
                for(int m = k + 1; m < j; ++m) {
                    if (!(ds[m * 4 + 3] <= (double)0.0F)) {
                        double d = ds[k * 4] - ds[m * 4];
                        double e = ds[k * 4 + 1] - ds[m * 4 + 1];
                        double g = ds[k * 4 + 2] - ds[m * 4 + 2];
                        double h = ds[k * 4 + 3] - ds[m * 4 + 3];
                        if (h * h > d * d + e * e + g * g) {
                            if (h > (double)0.0F) {
                                ds[m * 4 + 3] = -1.0F;
                            } else {
                                ds[k * 4 + 3] = -1.0F;
                            }
                        }
                    }
                }
            }
        }

        try (ChunkSectionCache chunkSectionCache = new ChunkSectionCache(world)) {
            for(int m = 0; m < j; ++m) {
                double d = ds[m * 4 + 3];
                if (!(d < (double)0.0F)) {
                    double e = ds[m * 4];
                    double g = ds[m * 4 + 1];
                    double h = ds[m * 4 + 2];
                    int n = Math.max(MathHelper.floor(e - d), x);
                    int o = Math.max(MathHelper.floor(g - d), y);
                    int p = Math.max(MathHelper.floor(h - d), z);
                    int q = Math.max(MathHelper.floor(e + d), n);
                    int r = Math.max(MathHelper.floor(g + d), o);
                    int s = Math.max(MathHelper.floor(h + d), p);

                    for(int t = n; t <= q; ++t) {
                        double u = ((double)t + (double)0.5F - e) / d;
                        if (u * u < (double)1.0F) {
                            for(int v = o; v <= r; ++v) {
                                double w = ((double)v + (double)0.5F - g) / d;
                                if (u * u + w * w < (double)1.0F) {
                                    for(int aa = p; aa <= s; ++aa) {
                                        double ab = ((double)aa + (double)0.5F - h) / d;
                                        if (u * u + w * w + ab * ab < (double)1.0F && !world.isOutOfHeightLimit(v)) {
                                            int ac = t - x + (v - y) * horizontalSize + (aa - z) * horizontalSize * verticalSize;
                                            if (!bitSet.get(ac)) {
                                                bitSet.set(ac);
                                                mutable.set(t, v, aa);
                                                if (world.isValidForSetBlock(mutable)) {
                                                    ChunkSection chunkSection = chunkSectionCache.getSection(mutable);
                                                    if (chunkSection != null) {
                                                        int ad = ChunkSectionPos.getLocalCoord(t);
                                                        int ae = ChunkSectionPos.getLocalCoord(v);
                                                        int af = ChunkSectionPos.getLocalCoord(aa);
                                                        BlockState blockState = chunkSection.getBlockState(ad, ae, af);

                                                        for(OreFeatureConfig.Target target : config.targets()) {
                                                            Objects.requireNonNull(chunkSectionCache);
                                                            if (shouldPlace(blockState, chunkSectionCache::getBlockState, random, config, target, mutable)) {
                                                                chunkSection.setBlockState(ad, ae, af, target.state, false);
                                                                ++i;
                                                                break;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return i > 0;
    }

    public static boolean shouldPlace(BlockState state, Function<BlockPos, BlockState> posToState, Random random, RecursiveOreFeatureConfig config, OreFeatureConfig.Target target, BlockPos.Mutable pos) {
        if (!target.target.test(state, random)) {
            return false;
        } else if (shouldNotDiscard(random, config.discardOnAirChance())) {
            return true;
        } else {
            return !isExposedToAir(posToState, pos);
        }
    }

    protected static boolean shouldNotDiscard(Random random, float chance) {
        if (chance <= 0.0F) {
            return true;
        } else if (chance >= 1.0F) {
            return false;
        } else {
            return random.nextFloat() >= chance;
        }
    }
}
