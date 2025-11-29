package arr.armuriii.spiritum.block;

import arr.armuriii.spiritum.block.entity.RitualPedestalEntity;
import arr.armuriii.spiritum.init.SpiritumBlocks;
import arr.armuriii.spiritum.init.SpiritumItems;
import arr.armuriii.spiritum.init.SpiritumRituals;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RitualPedestal extends BlockWithEntity implements Waterloggable {

    public static final MapCodec<RitualPedestal> CODEC =
            RecordCodecBuilder.mapCodec((instance) ->
                    instance.group(createSettingsCodec()).apply(instance, RitualPedestal::new));


    protected static final VoxelShape SHAPE = Block.createCuboidShape(1F, 0F, 1F, 15F, 16F, 15F);

    public RitualPedestal(Settings settings) {
        super(settings);
    }

    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof RitualPedestalEntity pedestal ) {
            ItemStack itemStack = player.getStackInHand(hand);
            if (itemStack.isEmpty())
                return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

            if (itemStack.isOf(SpiritumItems.SPIRIT_BOTTLE)) {
                pedestal.addSoul(1);
                if (itemStack.getCount() == 1) {
                    player.setStackInHand(hand,new ItemStack(Items.GLASS_BOTTLE));
                }else {
                    player.giveItemStack(new ItemStack(Items.GLASS_BOTTLE));
                    itemStack.decrement(1);
                }
                world.playSound(null, (double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F,
                        SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                return ItemActionResult.SUCCESS;
            } else if (pedestal.addStack(itemStack.splitUnlessCreative(1, player))) {
                world.playSound(null, (double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F,
                        SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
                return ItemActionResult.SUCCESS;
            }
        }

        return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (player.getMainHandStack().isEmpty()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof RitualPedestalEntity pedestal) {
                if (pedestal.hasValidRitual() && pedestal.getRitual() == SpiritumRituals.EMPTY) {
                    pedestal.setRitual(pedestal.getValidRitual().orElse(null));
                    pedestal.setOwner(player.getUuid());
                    pedestal.getRitual().onApply(pedestal,player.getUuid());
                    pedestal.setTime(world.getTime());
                    world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, SoundCategory.BLOCKS, 1.0F, 1.0F);
                }else if (pedestal.isActive()) {
                    world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    pedestal.removeRitual();
                }else if (player.isSneaking() && !pedestal.getItems().isEmpty() && pedestal.getLast() != null && pedestal.getRitual() == SpiritumRituals.EMPTY) {
                    world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), pedestal.getLast());
                    pedestal.removeLast();
                } else return super.onUse(state, world, pos, player, hit);

                return ActionResult.SUCCESS;
            }
        }
        return super.onUse(state, world, pos, player, hit);
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (world instanceof ServerWorld serverWorld) {
            this.update(state, serverWorld, pos);
        }
    }

    public void update(BlockState state, ServerWorld world, BlockPos pos) {
        boolean bl = world.isReceivingRedstonePower(pos);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (bl && blockEntity instanceof RitualPedestalEntity pedestal) {
            if (pedestal.hasValidRitual() && pedestal.getRitual() == null) {
                pedestal.setRitual(pedestal.getValidRitual().orElse(null));
                pedestal.getRitual().onApply(pedestal,null);
                pedestal.setTime(world.getTime());
                world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
        }
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof RitualPedestalEntity pedestal)) return super.getComparatorOutput(state, world, pos);

        List<ItemFrameEntity> entities = world.getEntitiesByClass(ItemFrameEntity.class,
                Box.of(pos.toBottomCenterPos(),1,1,1),(frame -> frame.isSupportedBy(pos)));

        int count = 0;
        for (ItemFrameEntity frame : entities) {
            if (frame.getHeldItemStack().isEmpty()) count = pedestal.getItems().size();
            if (count != 0) break;

            for (ItemStack item : pedestal.getItems())
                if (item.isOf(frame.getHeldItemStack().getItem())) count++;

        }
        return Math.min(count,15);
    }

    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof RitualPedestalEntity pedestal)
                ItemScatterer.spawn(world, pos, new DefaultedList<>(pedestal.getItems(),ItemStack.EMPTY));
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RitualPedestalEntity(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient) {
            return validateTicker(type, SpiritumBlocks.RITUAL_PEDESTAL_BLOCK_ENTITY, RitualPedestalEntity::clientTick);
        } else {
            return validateTicker(type, SpiritumBlocks.RITUAL_PEDESTAL_BLOCK_ENTITY, RitualPedestalEntity::serverTick);
        }
    }
}
