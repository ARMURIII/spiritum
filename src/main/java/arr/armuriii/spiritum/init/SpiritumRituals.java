package arr.armuriii.spiritum.init;

import arr.armuriii.spiritum.Spiritum;
import arr.armuriii.spiritum.block.entity.RitualPedestalEntity;
import arr.armuriii.spiritum.entity.ImpEntity;
import arr.armuriii.spiritum.rituals.Ritual;
import arr.armuriii.spiritum.utils.MobSpawningLogic;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.UUID;

public class SpiritumRituals {

    public static final Ritual EMPTY = register(new Ritual(Ritual.Type.INSTANT, Ingredient.empty()),"dummy");

    public static final Ritual WEATHER = register(new Ritual(Ritual.Type.INSTANT, Ingredient.ofItems(Items.NAUTILUS_SHELL)) {
        @Override
        public boolean onApply(RitualPedestalEntity pedestal, UUID owner) {
            if (pedestal.hasWorld()) {
                World world = pedestal.getWorld();
                if (world != null && world.getServer() != null) {
                    world.getServer().getOverworld().setWeather(!world.isRaining() ? 20 : 0,
                            !world.isRaining() ? 0 : ServerWorld.RAIN_WEATHER_DURATION_PROVIDER.get(world.getServer().getOverworld().getRandom()),
                            !world.isRaining(),true);
                    return super.onApply(pedestal, owner);
                }
            }
            super.onApply(pedestal, owner);
            return false;
        }
    },"weather_control");

    public static final Ritual TIME = register(new Ritual(Ritual.Type.INSTANT, Ingredient.ofItems(Items.CLOCK)) {
        @Override
        public boolean onApply(RitualPedestalEntity pedestal, UUID owner) {
            if (pedestal.hasWorld()) {
                World world = pedestal.getWorld();
                if (world != null && world.getServer() != null) {
                    if (world instanceof ServerWorld serverWorld)
                        serverWorld.setTimeOfDay(serverWorld.getTimeOfDay()+12000L);
                    return super.onApply(pedestal, owner);
                }
            }
            super.onApply(pedestal, owner);
            return false;
        }
    },"time_control");

    public static final Ritual CONSECRATE_AREA = register(new Ritual(
            Ritual.Type.LASTING, Ingredient.ofItems(SpiritumItems.SILVER_INGOT)),"consecrate_area");

    public static final Ritual CAUSE_ABUNDANCE = register(new Ritual(
            Ritual.Type.LASTING, Ingredient.ofItems(Items.BONE_BLOCK)),"cause_abundance");

    public static final Ritual CURSE_CREATURE = register(new Ritual(
            Ritual.Type.INSTANT, Ingredient.ofItems(SpiritumItems.FLESH_CLUMP)){
        @Override
        public boolean onApply(RitualPedestalEntity pedestal, UUID owner) {
            if (pedestal.getWorld() != null)
                pedestal.getWorld().getEntitiesByClass(LivingEntity.class,Box.of(pedestal.getPos().toCenterPos(),16,16,16),(living)->true).forEach(
                                living -> {
                                    if (living.getUuid() != owner)
                                        living.addStatusEffect(new StatusEffectInstance(SpiritumPotions.LETHARGY, 3 * 60 * 20));
                                });

            return super.onApply(pedestal, owner);
        }
    },"curse_creature");

    public static final Ritual IMPS = register(new Ritual(Ritual.Type.INSTANT, Ingredient.ofItems(SpiritumItems.SUMMONING_TOKEN)) {
        @Override
        public boolean onApply(RitualPedestalEntity pedestal, UUID owner) {
            if (pedestal.hasWorld()) {
                World world = pedestal.getWorld();
                if (world != null) {
                    for (int i = 0; i < 2;i++) {
                        ImpEntity imp = new ImpEntity(SpiritumEntities.IMP, world);
                        imp.setTamed(true,true);
                        imp.setOwnerUuid(owner);
                        if (world instanceof ServerWorld serverWorld)
                            MobSpawningLogic.spawnNearby(serverWorld,pedestal.getPos(),imp,SpiritumEntities.IMP,2,4);
                    }

                    ItemStack last = pedestal.getStackFromItem(SpiritumItems.SUMMONING_TOKEN).copy();
                    if (last != null) {
                        if (world instanceof ServerWorld serverWorld)
                            last.damage(1,serverWorld,null,(item)->{});
                        pedestal.replaceItem(SpiritumItems.SUMMONING_TOKEN,last);
                        return true;
                    }
                }
            }
            return false;
        }
    },"conjure_imps");

    public static void register() {
        Spiritum.LOGGER.info("registered {} Rituals",Spiritum.MOD_ID);
    }

    private static Ritual register(Ritual ritual, String id) {
        return Registry.register(Spiritum.RITUAL, Spiritum.id(id), ritual);
    }
}
