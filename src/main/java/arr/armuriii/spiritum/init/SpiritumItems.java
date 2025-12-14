package arr.armuriii.spiritum.init;


import arr.armuriii.spiritum.Spiritum;
import arr.armuriii.spiritum.entity.SpiritEntity;
import arr.armuriii.spiritum.item.NeedleItem;
import arr.armuriii.spiritum.utils.ImmutableTwin;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponents;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.potion.Potion;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.event.GameEvent;

import java.util.List;

import static arr.armuriii.spiritum.init.SpiritumBlocks.*;

public class SpiritumItems {

    public static final Item FLESH_CLUMP = register(new Item(new Item.Settings().food(FoodComponents.GOLDEN_CARROT)),"flesh_clump");
    public static final Item POPPET = register(new Item(new Item.Settings().maxCount(1)),"poppet");
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
                            if (instance.getEffectType().value().isInstant())
                                instance.getEffectType().value().applyInstantEffect(attacker, attacker, player, instance.getAmplifier(), 1.0);
                            else
                                player.addStatusEffect(new StatusEffectInstance(
                                        instance.getEffectType(),
                                        instance.mapDuration(duration -> duration/2),
                                        instance.getAmplifier(),
                                        instance.isAmbient(),
                                        instance.shouldShowParticles()
                                ));
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
    public static final Item SPIRIT_BOTTLE = register(new Item(new Item.Settings()){
        @Override
        public ActionResult useOnBlock(ItemUsageContext context) {
            if (context.getPlayer() != null) {
                ItemStack stack = context.getStack();
                stack.split(1);
                if (stack.isEmpty())
                    context.getPlayer().setStackInHand(context.getHand(),Items.GLASS_BOTTLE.getDefaultStack());
                else
                    context.getPlayer().giveItemStack(Items.GLASS_BOTTLE.getDefaultStack());
                if (!context.getWorld().isClient()) {
                    SpiritEntity spirit = SpiritumEntities.SPIRIT.create(context.getWorld());
                    if (spirit != null) {
                        spirit.setOwner(context.getPlayer());
                        spirit.setPosition(context.getHitPos());
                        context.getWorld().spawnEntity(spirit);
                    }
                }
                context.getWorld().playSound(null,context.getPlayer().getBlockPos(),SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.PLAYERS);
                return ActionResult.success(context.getWorld().isClient());
            }
            return super.useOnBlock(context);
        }
    },"spirit_bottle");


    public static final Item SUMMONING_TOKEN = register(new Item(new Item.Settings().maxDamage(64)),"summoning_token");

    public static final Item SUMMONING_HAT = register(new TrinketItem(new Item.Settings().maxCount(1)){
        @Override
        public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
            super.appendTooltip(stack, context, tooltip, type);
            tooltip.add(Text.translatable("item.spiritum.summoning_hat.tooltip").withColor(0x933333));
        }

        public static ImmutableTwin<RegistryEntry<SoundEvent>> getEquipSounds() {
            return new ImmutableTwin<>(SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,SoundEvents.ITEM_ARMOR_EQUIP_ELYTRA);
        }
        @Override
        public void onUnequip(ItemStack stack, SlotReference ref, LivingEntity user) {
            super.onUnequip(stack, ref, user);
            RegistryEntry<SoundEvent> soundEvent = getEquipSounds().getLeft();
            if (!stack.isEmpty() && soundEvent != null) {
                user.emitGameEvent(GameEvent.EQUIP);
                user.playSound(soundEvent.value(), 1.0F, 1.0F);
            }
        }
        @Override
        public void onEquip(ItemStack stack, SlotReference ref, LivingEntity user) {
            super.onEquip(stack, ref, user);
            RegistryEntry<SoundEvent> soundEvent = getEquipSounds().getRight();
            if (!stack.isEmpty() && soundEvent != null) {
                user.emitGameEvent(GameEvent.EQUIP);
                user.playSound(soundEvent.value(), 1.0F, 1.0F);
            }
        }
    },"summoning_hat");

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
                entries.add(SUMMONING_HAT);
            })
            .build();

    public static final ItemGroup TIPPED_NEEDLE_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(SILVER_NEEDLE))
            .displayName(Text.translatable("itemGroup.spiritum.tipped_needle_group"))
            .entries((context, entries) -> {
                context.lookup().getOptionalWrapper(RegistryKeys.POTION).ifPresent((registryWrapper) ->
                    addPotions(entries, registryWrapper, TIPPED_SILVER_NEEDLE, ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS, context.enabledFeatures()));
            })
            .build();

    private static final Identifier ANCIENT_CITY_CHEST_LOOT_TABLE_ID = Identifier.ofVanilla("chests/ancient_city");

    public static void register() {
        Spiritum.LOGGER.info("registered {} Items",Spiritum.MOD_ID);
        Registry.register(Registries.ITEM_GROUP, Spiritum.id("spiritum_group"), SPIRITUM_GROUP);
        Registry.register(Registries.ITEM_GROUP, Spiritum.id("tipped_needle_group"), TIPPED_NEEDLE_GROUP);

        LootTableEvents.MODIFY.register((key, builder, source, lookup) -> {
            if (ANCIENT_CITY_CHEST_LOOT_TABLE_ID.equals(key.getValue())) {
                LootPool tokenPool = LootPool.builder()
                        .rolls(UniformLootNumberProvider.create(0, 1))
                        .conditionally(RandomChanceLootCondition.builder(1).build())
                        .with(ItemEntry.builder(SUMMONING_TOKEN).build()).build();
                LootPool silverPool = LootPool.builder()
                        .rolls(UniformLootNumberProvider.create(0, 5))
                        .conditionally(RandomChanceLootCondition.builder(1).build())
                        .with(ItemEntry.builder(SILVER_INGOT).build()).build();
                builder.pool(tokenPool);
                builder.pool(silverPool);
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
