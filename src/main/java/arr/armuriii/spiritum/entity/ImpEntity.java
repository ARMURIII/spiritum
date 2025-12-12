package arr.armuriii.spiritum.entity;

import arr.armuriii.spiritum.entity.goal.HealthMeleeAttackGoal;
import arr.armuriii.spiritum.entity.goal.HealthProjectileAttackGoal;
import arr.armuriii.spiritum.entity.goal.HoldingGoal;
import arr.armuriii.spiritum.entity.projectile.SpitProjectileEntity;
import arr.armuriii.spiritum.init.SpiritumItems;
import arr.armuriii.spiritum.init.SpiritumParticles;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ImpEntity extends TameableEntity implements Angerable,Ownable,RangedAttackMob {

    private static final TrackedData<Integer> ANGER_TIME = DataTracker.registerData(ImpEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> HELP_REQUIRED = DataTracker.registerData(ImpEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> SPITTING = DataTracker.registerData(ImpEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final UniformIntProvider ANGER_TIME_RANGE = TimeHelper.betweenSeconds(30, 60);
    public AnimationState spitAnimationState = new AnimationState();
    @Nullable
    private UUID angryAt;

    public ImpEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }

    public static ImpEntity create(EntityType<? extends TameableEntity> entityType, World world, PlayerEntity owner) {
        ImpEntity imp = new ImpEntity(entityType, world);
        imp.setOwner(owner);
        return imp;
    }

    protected void initGoals() {
        this.goalSelector.add(2, new SitGoal(this));
        this.goalSelector.add(3, new HoldingGoal(this,1.0f,true));
        this.goalSelector.add(4, new HealthMeleeAttackGoal(this, 1.0F, true));
        this.goalSelector.add(5, new HealthProjectileAttackGoal(this, 0.5F,30,25,5));
        this.goalSelector.add(6, new FollowOwnerGoal(this, 0.5F, 15.0F, 2.0F));
        this.goalSelector.add(7, new WanderAroundFarGoal(this, 0.5F));
        this.goalSelector.add(10, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(10, new LookAroundGoal(this));
        this.targetSelector.add(1, new TrackOwnerAttackerGoal(this));
        this.targetSelector.add(2, new AttackWithOwnerGoal(this));
        this.targetSelector.add(3, (new RevengeGoal(this)).setGroupRevenge());
        this.targetSelector.add(4, new ActiveTargetGoal<>(this, PlayerEntity.class, 10, true, false, this::shouldAngerAt));
        this.targetSelector.add(5, new ActiveTargetGoal<>(this, AnimalEntity.class,false, entity -> entity instanceof HostileEntity));
        this.targetSelector.add(8, new UniversalAngerGoal<>(this, true));
    }

    public static DefaultAttributeContainer.Builder createImpAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.40)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 16.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.5);
    }

    public boolean shouldTryTeleportToOwner() {
        LivingEntity livingEntity = this.getOwner();
        return livingEntity != null && this.squaredDistanceTo(this.getOwner()) >= 625;
    }

    @Override
    public boolean tryAttack(Entity target) {
        float attackDamage = (float)this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        if (
                TrinketsApi.getTrinketComponent(getOwner()).isPresent() &&
                TrinketsApi.getTrinketComponent(getOwner()).get().isEquipped(SpiritumItems.SUMMONING_HAT)
        )
            attackDamage *= 1.25f;

        DamageSource damageSource = this.getDamageSources().mobAttack(this);
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            attackDamage = EnchantmentHelper.getDamage(serverWorld, this.getWeaponStack(), target, damageSource, attackDamage);
        }

        boolean damaged = target.damage(damageSource, attackDamage);
        if (damaged) {
            float knockback = this.getKnockbackAgainst(target, damageSource);
            if (knockback > 0.0F && target instanceof LivingEntity livingEntity) {
                livingEntity.takeKnockback(knockback * 0.25F, MathHelper.sin(this.getYaw() * ((float)Math.PI / 180F)), -MathHelper.cos(this.getYaw() * ((float)Math.PI / 180F)));
                this.setVelocity(this.getVelocity().multiply(0.6, 1.0F, 0.6));
            }

            if (this.getWorld() instanceof ServerWorld serverWorld2)
                EnchantmentHelper.onTargetDamaged(serverWorld2, target, damageSource);

            this.onAttacking(target);
            this.playAttackSound();
        }

        return damaged;
    }

    @Override
    public boolean isExperienceDroppingDisabled() {
        return true;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(ANGER_TIME,0);
        builder.add(HELP_REQUIRED,false);
        builder.add(SPITTING,0);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (this.getWorld() instanceof ServerWorld serverWorld)
            this.tickAngerLogic(serverWorld, true);
    }

    @Override
    public boolean canAttackWithOwner(LivingEntity target, LivingEntity owner) {
        return !hasSameOwner(target,owner);
    }

    public boolean hasSameOwner(LivingEntity living) {
        return living instanceof Ownable ownable && ownable.getOwner() != null && ownable.getOwner().getUuid() == getOwnerUuid();
    }

    public boolean hasSameOwner(LivingEntity living, LivingEntity owner) {
        return living instanceof Ownable ownable && ownable.getOwner() != null && ownable.getOwner().getUuid() == owner.getUuid();
    }

    @Override
    public void baseTick() {
        super.baseTick();
        spitting(Math.max(getSpitting()-1, 0));
        if (getWorld().isClient()) {
            if (getSpitting() > 0)
                spitAnimationState.startIfNotRunning(age);
            else spitAnimationState.stop();
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        writeAngerToNbt(nbt);
        nbt.putInt("Spitting",getSpitting());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        readAngerFromNbt(getWorld(),nbt);
        spitting(nbt.getInt("Spitting"));
    }

    public void playSpawnEffects() {
        if (this.getWorld().isClient)
            for(int i = 0; i < 20; ++i) {
                double d = this.random.nextGaussian() * 0.02;
                double e = this.random.nextGaussian() * 0.02;
                double f = this.random.nextGaussian() * 0.02;
                this.getWorld().addParticle(SpiritumParticles.IMP_TYPE, this.offsetX(1.0)-d*10.0,
                        this.getRandomBodyY()-e*10.0,
                        this.getParticleZ(1.0)-f*10.0,
                        d,e,f);
            }
        else
            this.getWorld().sendEntityStatus(this, (byte)20);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return false;
    }

    @Override
    public @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    @Override
    public int getAngerTime() {
        return this.dataTracker.get(ANGER_TIME);
    }

    @Override
    public void setAngerTime(int angerTime) {
        this.dataTracker.set(ANGER_TIME,angerTime);
    }

    @Override
    public @Nullable UUID getAngryAt() {
        return angryAt;
    }

    @Override
    public boolean canTarget(LivingEntity target) {
        return target == null || (super.canTarget(target) && !(target instanceof ImpEntity imp && imp.getOwnerUuid() != null && imp.getOwnerUuid() == this.getOwnerUuid()));
    }

    @Override
    public @Nullable LivingEntity getTarget() {
        return super.getTarget();
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (canTarget(target))
            super.setTarget(target);
    }

    @Override
    public void setAngryAt(@Nullable UUID angryAt) {
            this.angryAt = angryAt;
    }

    @Override
    public void chooseRandomAngerTime() {
        this.setAngerTime(ANGER_TIME_RANGE.get(this.random));
    }

    public void setHelpRequired(boolean help) {
        this.dataTracker.set(HELP_REQUIRED,help);
    }

    public boolean getHelpRequired() {
        return this.dataTracker.get(HELP_REQUIRED);
    }

    public void spitting(int spit) {
        this.dataTracker.set(SPITTING,spit);
    }

    public int getSpitting() {
        return this.dataTracker.get(SPITTING);
    }

    @Override
    public @Nullable LivingEntity getOwner() {
        return super.getOwner();
    }

    @Override
    public void shootAt(LivingEntity target, float pullProgress) {
        SpitProjectileEntity spit = new SpitProjectileEntity(null,getWorld());
        spit.setOwner(this.getOwner());
        spit.setPos(getX(),getEyeY() - 0.1F,getZ());
        double d = target.getX() - this.getX();
        double e = target.getBodyY(1/3f) - spit.getY();
        double f = target.getZ() - this.getZ();
        double g = Math.sqrt(d * d + f * f);
        spit.setVelocity(d, e + g * (double)0.2F, f, 1.6F, (float)(14 - this.getWorld().getDifficulty().getId() * 4));
        this.getWorld().spawnEntity(spit);
        spitting(30);
    }
}
