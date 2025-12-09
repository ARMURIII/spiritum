package arr.armuriii.spiritum.init;

import arr.armuriii.spiritum.Spiritum;
import arr.armuriii.spiritum.block.RitualPedestal;
import arr.armuriii.spiritum.block.entity.RitualPedestalEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class SpiritumBlocks {

    public static final TagKey<Block> HEXSTONES = TagKey.of(RegistryKeys.BLOCK, Spiritum.id("hexstones"));

    public static final Block HEXSTONE = register(new Block(AbstractBlock.Settings.copy(Blocks.ANDESITE)),"hexstone", new Item.Settings());
    public static final Block HEXSTONE_STAIRS = register(createStairs(HEXSTONE),"hexstone_stairs", new Item.Settings());
    public static final Block HEXSTONE_SLAB = register(new SlabBlock(AbstractBlock.Settings.copy(HEXSTONE)),"hexstone_slab", new Item.Settings());
    public static final Block HEXSTONE_WALL = register(new WallBlock(AbstractBlock.Settings.copy(HEXSTONE).solid()),"hexstone_wall", new Item.Settings());

    public static final Block HEXSTONE_BRICKS = register(new Block(AbstractBlock.Settings.copy(Blocks.DEEPSLATE_BRICKS)),"hexstone_bricks", new Item.Settings());
    public static final Block HEXSTONE_BRICKS_STAIRS = register(createStairs(HEXSTONE_BRICKS),"hexstone_bricks_stairs", new Item.Settings());
    public static final Block HEXSTONE_BRICKS_SLAB = register(new SlabBlock(AbstractBlock.Settings.copy(HEXSTONE_BRICKS)),"hexstone_bricks_slab", new Item.Settings());
    public static final Block HEXSTONE_BRICKS_WALL = register(new WallBlock(AbstractBlock.Settings.copy(HEXSTONE_BRICKS).solid()),"hexstone_bricks_wall", new Item.Settings());

    public static final Block POLISHED_HEXSTONE = register(new Block(AbstractBlock.Settings.copy(Blocks.POLISHED_DEEPSLATE)),"polished_hexstone", new Item.Settings());
    public static final Block POLISHED_HEXSTONE_STAIRS = register(createStairs(POLISHED_HEXSTONE),"polished_hexstone_stairs", new Item.Settings());
    public static final Block POLISHED_HEXSTONE_SLAB = register(new SlabBlock(AbstractBlock.Settings.copy(POLISHED_HEXSTONE)),"polished_hexstone_slab", new Item.Settings());
    public static final Block POLISHED_HEXSTONE_WALL = register(
            new WallBlock(AbstractBlock.Settings.copy(POLISHED_HEXSTONE).solid()),"polished_hexstone_wall", new Item.Settings());

    public static final Block SILVERED_HEXSTONE = register(new Block(AbstractBlock.Settings.copy(Blocks.IRON_ORE)),"silvered_hexstone", new Item.Settings());



    public static final Block SILVER_BLOCK = register(new Block(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK)),"silver_block", new Item.Settings());

    public static final Block POLISHED_SILVER_BLOCK = register(new Block(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK)),"polished_silver_block", new Item.Settings());
    public static final Block POLISHED_SILVER_STAIRS = register(createStairs(POLISHED_SILVER_BLOCK),"polished_silver_stairs", new Item.Settings());
    public static final Block POLISHED_SILVER_SLAB = register(new SlabBlock(AbstractBlock.Settings.copy(POLISHED_SILVER_BLOCK)),"polished_silver_slab", new Item.Settings());
    public static final Block POLISHED_SILVER_WALL = register(new WallBlock(AbstractBlock.Settings.copy(POLISHED_SILVER_BLOCK).solid()),"polished_silver_wall", new Item.Settings());



    public static final Block FLESH_BLOCK = register(new Block(AbstractBlock.Settings.create().strength(0.5F)
            .sounds(BlockSoundGroup.CORAL).velocityMultiplier(0.5F).jumpVelocityMultiplier(0.5F).nonOpaque().mapColor(MapColor.DARK_RED)) {
        private static final VoxelShape SHAPE = Block.createCuboidShape(1, 0, 1, 15, 15, 15);

        @Override
        protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
            return SHAPE;
        }
    },"flesh_block", new Item.Settings());



    public static final Block RITUAL_PEDESTAL = register(
            new RitualPedestal(AbstractBlock.Settings.create().requiresTool().strength(3.5F).solid().nonOpaque().mapColor(MapColor.DARK_RED)),"ritual_pedestal", new Item.Settings());

    public static final BlockEntityType<RitualPedestalEntity> RITUAL_PEDESTAL_BLOCK_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,Spiritum.id("ritual_pedestal"), BlockEntityType.Builder.create(RitualPedestalEntity::new, RITUAL_PEDESTAL).build());



    public static void register() {
        Spiritum.LOGGER.info("registered {} Blocks",Spiritum.MOD_ID);
    }
    private static Block register(Block block,String id) {
        return Registry.register(Registries.BLOCK,Spiritum.id(id),block);
    }

    private static Block register(Block block, String id, Item.Settings settings) {
        Registry.register(Registries.ITEM,Spiritum.id(id),new BlockItem(block,settings));
        return Registry.register(Registries.BLOCK,Spiritum.id(id),block);
    }

    private static Block createStairs(Block base) {
        return new StairsBlock(base.getDefaultState(), AbstractBlock.Settings.copy(base));
    }
}
