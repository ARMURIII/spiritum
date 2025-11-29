package arr.armuriii.spiritum.mixin;

import arr.armuriii.spiritum.block.entity.RitualPedestalEntity;
import arr.armuriii.spiritum.init.SpiritumBlocks;
import arr.armuriii.spiritum.init.SpiritumRituals;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.Block;
import net.minecraft.block.CropBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.function.Predicate;

@Mixin(CropBlock.class)
public class CropBlockMixin {
    @ModifyReturnValue(method = "getAvailableMoisture", at = @At("RETURN"))
    private static float spiritum$shouldGrowFast(float original, Block block, BlockView world, BlockPos pos) {
        int multiplier = 1;
        for (BlockPos ignored : spiritum$findClosestList(pos, blockPos -> {
            RitualPedestalEntity pedestal = world.getBlockEntity(blockPos, SpiritumBlocks.RITUAL_PEDESTAL_BLOCK_ENTITY).orElse(null);
            return pedestal != null && pedestal.isActive() && pedestal.getRitual() == SpiritumRituals.CAUSE_ABUNDANCE;
        })) {
            multiplier*=4;
        }

        return original*multiplier;
    }

    @Unique
    private static List<BlockPos> spiritum$findClosestList(BlockPos pos, Predicate<BlockPos> condition) {
        List<BlockPos> blocks = new java.util.ArrayList<>(List.of());
        for (BlockPos blockPos : BlockPos.iterateOutwards(pos, 8, 8, 8)) {
            if (condition.test(blockPos)) {
                blocks.add(blockPos);
            }
        }
        return blocks;
    }
}
