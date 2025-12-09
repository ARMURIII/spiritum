package arr.armuriii.spiritum.mixin;

import arr.armuriii.spiritum.block.entity.RitualPedestalEntity;
import arr.armuriii.spiritum.init.SpiritumBlocks;
import arr.armuriii.spiritum.init.SpiritumRituals;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Predicate;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity {

    @Shadow public abstract @Nullable LivingEntity getTarget();

    @Shadow public abstract void setTarget(@Nullable LivingEntity target);

    private MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {super(entityType, world);}

    @WrapMethod(method = "setTarget")
    private void spiritum$cannotTarget(LivingEntity target, Operation<Void> original) {
        if (!(((MobEntity)(Object)this) instanceof HostileEntity) || target == null || findClosestList(target.getBlockPos(), blockPos -> {
            RitualPedestalEntity pedestal = this.getWorld().getBlockEntity(blockPos, SpiritumBlocks.RITUAL_PEDESTAL_BLOCK_ENTITY).orElse(null);
            return pedestal != null && pedestal.isActive() && pedestal.getRitual() == SpiritumRituals.CONSECRATE_AREA;
        }).isEmpty()) original.call(target);
    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void spiritum$removeTarget(CallbackInfo ci) {
        if (getTarget() != null && !findClosestList(getTarget().getBlockPos(), blockPos -> {
            RitualPedestalEntity pedestal = this.getWorld().getBlockEntity(blockPos, SpiritumBlocks.RITUAL_PEDESTAL_BLOCK_ENTITY).orElse(null);
            return pedestal != null && pedestal.isActive() && pedestal.getRitual() == SpiritumRituals.CONSECRATE_AREA;
        }).isEmpty())
            setTarget(null);
    }

    @Unique
    private static List<BlockPos> findClosestList(BlockPos pos, Predicate<BlockPos> condition) {
        List<BlockPos> blocks = new java.util.ArrayList<>(List.of());
        for (BlockPos blockPos : BlockPos.iterateOutwards(pos, 32, 5, 32)) {
            if (condition.test(blockPos)) {
                blocks.add(blockPos);
            }
        }
        return blocks;
    }
}
