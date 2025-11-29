package arr.armuriii.spiritum.init;

import arr.armuriii.spiritum.Spiritum;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

public class SpiritumDamageTypes {

    public static final RegistryKey<DamageType> HEXING = register("hex");

    public static void register() {
        Spiritum.LOGGER.info("registered {} Damage Types",Spiritum.MOD_ID);
    }

    public static DamageSource of(World world,RegistryKey<DamageType> type) {
        return new DamageSource(
                world.getRegistryManager()
                        .get(RegistryKeys.DAMAGE_TYPE)
                        .entryOf(type));
    }

    public static DamageSource of(RegistryKey<DamageType> type, Entity attacker) {
        return new DamageSource(
                attacker.getWorld().getRegistryManager()
                        .get(RegistryKeys.DAMAGE_TYPE)
                        .entryOf(type),attacker);
    }

    public static DamageSource of(RegistryKey<DamageType> type, Entity source, Entity attacker) {
        return new DamageSource(
                attacker.getWorld().getRegistryManager()
                        .get(RegistryKeys.DAMAGE_TYPE)
                        .entryOf(type),source,attacker);
    }

    private static RegistryKey<DamageType> register(String id) {
        return RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Spiritum.id(id));
    }
}
