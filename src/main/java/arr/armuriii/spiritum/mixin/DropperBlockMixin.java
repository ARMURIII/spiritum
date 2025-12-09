package arr.armuriii.spiritum.mixin;

import arr.armuriii.spiritum.block.entity.RitualPedestalEntity;
import arr.armuriii.spiritum.init.SpiritumItems;
import arr.armuriii.spiritum.init.SpiritumRituals;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import net.minecraft.block.DropperBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DropperBlock.class)
public class DropperBlockMixin {
    @Shadow @Final private static DispenserBehavior BEHAVIOR;

    @Inject(method = "dispense", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/HopperBlockEntity;getInventoryAt(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/inventory/Inventory;",shift = At.Shift.AFTER), cancellable = true)
    private void spiritum$dispenseInPedestal(ServerWorld world, BlockState state, BlockPos pos, CallbackInfo ci, @Local Direction direction, @Local ItemStack itemStack, @Local DispenserBlockEntity dispenserBlockEntity, @Local int i, @Local BlockPointer pointer) {
        BlockEntity blockEntity = world.getBlockEntity(pos.offset(direction));
        if (blockEntity instanceof RitualPedestalEntity pedestal) {
            if (itemStack.isOf(SpiritumItems.SPIRIT_BOTTLE) && pedestal.hasValidRitual() && pedestal.getRitual() == SpiritumRituals.EMPTY) {
                itemStack.decrement(1);
                dispenserBlockEntity.setStack(i,itemStack);
                if (itemStack.getCount() == 0)
                    dispenserBlockEntity.setStack(i,new ItemStack(Items.GLASS_BOTTLE));
                else new ItemDispenserBehavior().dispense(pointer,new ItemStack(Items.GLASS_BOTTLE));

                pedestal.setRitual(pedestal.getValidRitual().orElse(null));
                pedestal.getRitual().onApply(pedestal,null);
                pedestal.setTime(world.getTime());
                world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, SoundCategory.BLOCKS, 1.0F, 1.0F);
            } else {
                pedestal.addStack(itemStack.copyWithCount(1));
                itemStack.decrement(1);
                dispenserBlockEntity.setStack(i,itemStack);
                world.playSound(null, (double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F,
                        SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
            ci.cancel();
        }
    }
}
