package arr.armuriii.spiritum.init;

import arr.armuriii.spiritum.Spiritum;
import arr.armuriii.spiritum.entity.ImpEntity;
import arr.armuriii.spiritum.entity.SpiritEntity;
import arr.armuriii.spiritum.entity.projectile.SpitProjectileEntity;
import arr.armuriii.spiritum.rituals.Ritual;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAttachmentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.Vec3d;

public class SpiritumEntities {

    public static final TagKey<EntityType<?>> HAS_SOUL = TagKey.of(Registries.ENTITY_TYPE.getKey(), Spiritum.id("has_soul"));

    public static final EntityType<SpiritEntity> SPIRIT = register(EntityType.Builder.create(SpiritEntity::new,SpawnGroup.MISC)
            .dimensions(0.5f,0.8f)
            .eyeHeight(0.6f)
            .passengerAttachments(new Vec3d(0, 0.9f, 0))
            .build("spirit")
            ,"spirit");

    public static final EntityType<ImpEntity> IMP = register(EntityType.Builder.create(ImpEntity::new,SpawnGroup.MISC)
            .dimensions(0.7f,1)
            .eyeHeight(0.8f)
            .passengerAttachments(new Vec3d(0, 1.5625, 0))
            .build("imp")
            ,"imp");

    public static final EntityType<SpitProjectileEntity> SPIT = register(EntityType.Builder.create(SpitProjectileEntity::new,SpawnGroup.MISC)
            .dimensions(0.5f,0.5f)
            .build("spit")
            ,"spit");

    public static void register() {
        Spiritum.LOGGER.info("registered {} Entities",Spiritum.MOD_ID);

        FabricDefaultAttributeRegistry.register(IMP,ImpEntity.createImpAttributes());
        FabricDefaultAttributeRegistry.register(SPIRIT,SpiritEntity.createSpiritAttributes());
    }

    private static <T extends Entity> EntityType<T> register(EntityType<T> entity, String id) {
        return Registry.register(Registries.ENTITY_TYPE, Spiritum.id(id), entity);
    }
}
