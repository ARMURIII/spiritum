package arr.armuriii.spiritum.init;


import arr.armuriii.spiritum.Spiritum;
import arr.armuriii.spiritum.block.entity.RitualPedestalEntity;
import arr.armuriii.spiritum.item.NeedleItem;
import arr.armuriii.spiritum.mixin.accessor.StatusEffectInstanceAccessor;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.*;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;

import java.util.List;

import static arr.armuriii.spiritum.init.SpiritumBlocks.*;

public class SpiritumItems {

    public static final Item FLESH_CLUMP = register(new Item(new Item.Settings()),"flesh_clump");
    public static final Item POPPET = register(new Item(new Item.Settings()),"poppet");
    public static final Item RAW_SILVER = register(new Item(new Item.Settings()),"raw_silver");
    public static final Item SILVER_INGOT = register(new Item(new Item.Settings()),"silver_ingot");
    public static final Item SILVER_NUGGET = register(new Item(new Item.Settings()),"silver_nugget");
    public static final Item SILVER_NEEDLE = register(new NeedleItem(new Item.Settings().maxCount(8)),"silver_needle");
    public static final Item TIPPED_SILVER_NEEDLE = register(new NeedleItem(new Item.Settings().maxCount(8)
            .component(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT)) {
        @Override
        protected void pokePlayer(PlayerEntity player, PlayerEntity attacker) {
            super.pokePlayer(player, attacker);
            for (Hand hand : Hand.values()) {
                ItemStack stack = attacker.getStackInHand(hand);
                if (stack.isOf(TIPPED_SILVER_NEEDLE) && !player.isMainPlayer()) {
                    PotionContentsComponent component = stack.get(DataComponentTypes.POTION_CONTENTS);
                    if (component != null && component.potion().isPresent())

                        for (StatusEffectInstance instance : component.getEffects()) {
                            StatusEffectInstance copy = new StatusEffectInstance(instance);
                            ((StatusEffectInstanceAccessor)copy).setDuration(instance.getDuration()/2);
                            player.addStatusEffect(copy);
                        }
                }
            }
        }

        @Override
        public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
            PotionContentsComponent potionContentsComponent = stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
            potionContentsComponent.buildTooltip(tooltip::add, 0.5F, context.getUpdateTickRate());
        }
    },"tipped_silver_needle");
    public static final Item SPIRIT_BOTTLE = register(new Item(new Item.Settings()),"spirit_bottle");
    public static final Item SUMMONING_TOKEN = register(new Item(new Item.Settings().maxDamage(56)),"summoning_token");

    public static final ItemGroup SPIRITUM_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(SUMMONING_TOKEN))
            .displayName(Text.translatable("itemGroup.spiritum.spiritum_group"))
            .entries((context, entries) -> {
                entries.add(FLESH_CLUMP);
                entries.add(FLESH_BLOCK);
                entries.add(POPPET);
                entries.add(HEXSTONE);
                entries.add(HEXSTONE_STAIRS);
                entries.add(HEXSTONE_SLAB);
                entries.add(HEXSTONE_WALL);
                entries.add(SILVERED_HEXSTONE);
                entries.add(HEXSTONE_BRICKS);
                entries.add(HEXSTONE_BRICKS_STAIRS);
                entries.add(HEXSTONE_BRICKS_SLAB);
                entries.add(HEXSTONE_BRICKS_WALL);
                entries.add(POLISHED_HEXSTONE);
                entries.add(POLISHED_HEXSTONE_STAIRS);
                entries.add(POLISHED_HEXSTONE_SLAB);
                entries.add(POLISHED_HEXSTONE_WALL);
                entries.add(SPIRIT_BOTTLE);
                entries.add(SUMMONING_TOKEN);
                entries.add(RITUAL_PEDESTAL);
                entries.add(RAW_SILVER);
                entries.add(SILVER_INGOT);
                entries.add(SILVER_NUGGET);
                entries.add(SILVER_BLOCK);
                entries.add(POLISHED_SILVER_BLOCK);
                entries.add(POLISHED_SILVER_STAIRS);
                entries.add(POLISHED_SILVER_SLAB);
                entries.add(POLISHED_SILVER_WALL);
                entries.add(SILVER_NEEDLE);
                context.lookup().getOptionalWrapper(RegistryKeys.POTION).ifPresent((registryWrapper) ->
                    addPotions(entries, registryWrapper, TIPPED_SILVER_NEEDLE, ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS, context.enabledFeatures()));
            })
            .build();

    public static void register() {
        Spiritum.LOGGER.info("registered {} Items",Spiritum.MOD_ID);
        Registry.register(Registries.ITEM_GROUP, Spiritum.id("spiritum_group"), SPIRITUM_GROUP);

        DispenserBlock.registerBehavior(SPIRIT_BOTTLE, new ItemDispenserBehavior() {
            @Override
            protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                if (pointer.world().getBlockEntity(BlockPos.ofFloored(DispenserBlock.getOutputLocation(pointer))) instanceof RitualPedestalEntity pedestal) {
                    stack.decrement(1);
                    pedestal.addSoul(1);
                    return stack;
                }
                return super.dispenseSilently(pointer, stack);
            }
        });
    }

    private static void addPotions(ItemGroup.Entries entries, RegistryWrapper<Potion> registryWrapper, Item item, ItemGroup.StackVisibility visibility, FeatureSet enabledFeatures) {
        registryWrapper.streamEntries().filter(
                (potionEntry) -> potionEntry.value().isEnabled(enabledFeatures)).map(
                        (entry) -> PotionContentsComponent.createStack(item, entry)).forEach(
                                (stack) -> entries.add(stack, visibility));
    }

    private static Item register(Item item,String id) {
        return Registry.register(Registries.ITEM, Spiritum.id(id), item);
    }
}
