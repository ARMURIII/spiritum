package arr.armuriii.spiritum.entity.projectile;

import arr.armuriii.spiritum.init.SpiritumEntities;
import arr.armuriii.spiritum.init.SpiritumParticles;
import net.minecraft.block.AbstractBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SpitProjectileEntity extends ProjectileEntity {
    public SpitProjectileEntity(EntityType<?> ignored, World world) {
        super(SpiritumEntities.SPIT, world);
    }

    @Override
    protected double getGravity() {
        return 0.07;
    }

    @Override
    public void tick() {
        super.tick();

        Vec3d vec3d = this.getVelocity();
        HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
        this.hitOrDeflect(hitResult);
        double d = this.getX() + vec3d.x;
        double e = this.getY() + vec3d.y;
        double f = this.getZ() + vec3d.z;
        this.updateRotation();
        if (this.getWorld().getStatesInBox(this.getBoundingBox()).noneMatch(AbstractBlock.AbstractBlockState::isAir)) {
            this.discard();
        } else if (this.isInsideWaterOrBubbleColumn()) {
            this.discard();
        } else {
            this.setVelocity(vec3d.multiply(0.98F));
            this.applyGravity();
            this.setPosition(d, e, f);
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        if (this.getOwner() instanceof LivingEntity livingEntity) {
            Entity entity = entityHitResult.getEntity();
            DamageSource damageSource = this.getDamageSources().spit(this, livingEntity);
            if (
                    entity instanceof Ownable ownable &&
                    ownable.getOwner() != null &&
                    ownable.getOwner().getUuid() == livingEntity.getUuid()
            )
                return;

            if (this.isOwner(entity))
                return;

            if (entity.damage(damageSource, 2.0F) && this.getWorld() instanceof ServerWorld serverWorld) {
                EnchantmentHelper.onTargetDamaged(serverWorld, entity, damageSource);
                this.getWorld().playSound(null,this.getBlockPos(), SoundEvents.BLOCK_SLIME_BLOCK_BREAK, SoundCategory.HOSTILE);
                if (entity instanceof LivingEntity living)
                    living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS,30*20),getOwner());
                if (!this.getWorld().isClient)
                    this.discard();
            }
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        if (!this.getWorld().isClient)
            this.discard();
        this.getWorld().playSound(null,this.getBlockPos(), SoundEvents.BLOCK_SLIME_BLOCK_HIT, SoundCategory.HOSTILE);
    }

    @Override
    public void handleStatus(byte status) {
        super.handleStatus(status);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
    }
}
