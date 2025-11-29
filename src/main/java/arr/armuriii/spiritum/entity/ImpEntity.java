package arr.armuriii.spiritum.entity;

import arr.armuriii.spiritum.entity.goal.HealthMeleeAttackGoal;
import arr.armuriii.spiritum.entity.goal.HealthProjectileAttackGoal;
import arr.armuriii.spiritum.entity.goal.HoldingGoal;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ImpEntity extends TameableEntity implements Angerable, RangedAttackMob {

    private static final TrackedData<Integer> ANGER_TIME = DataTracker.registerData(ImpEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> HELP_REQUIRED = DataTracker.registerData(ImpEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final UniformIntProvider ANGER_TIME_RANGE = TimeHelper.betweenSeconds(30, 45);
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
        //this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new SitGoal(this));
        this.goalSelector.add(3, new HoldingGoal(this,1.0f,false));
        this.goalSelector.add(4, new HealthMeleeAttackGoal(this, 1.0F, true));
        this.goalSelector.add(5, new HealthProjectileAttackGoal(this, 0.5F,40,25f,5f));
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
    public boolean isExperienceDroppingDisabled() {
        return true;
    }

    /*@Override
    public void baseTick() {
        super.baseTick();
        if ((getTarget() != null || getAngryAt() != null) && horizontalSpeed > 0) {
            angryAnimationState.start(age);
        }else {
            angryAnimationState.stop();
        }
        if (!getPassengerList().isEmpty() && horizontalSpeed > 0)
            holdingAnimationState.start(age);
        else
            holdingAnimationState.stop();
    }*/

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(ANGER_TIME,0);
        builder.add(HELP_REQUIRED,false);
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

    @Override
    public @Nullable LivingEntity getOwner() {
        return super.getOwner();
    }

    @Override
    public void shootAt(LivingEntity target, float pullProgress) {
        if (this.getWorld().isClient()) spitAnimationState.stop();
        ItemStack itemStack = new ItemStack(Items.BOW);
        ItemStack itemStack2 = new ItemStack(Items.TIPPED_ARROW);
        itemStack2.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Potions.SLOWNESS));

        PersistentProjectileEntity persistentProjectileEntity = ProjectileUtil.createArrowProjectile(this, itemStack2, pullProgress, itemStack);
        double d = target.getX() - this.getX();
        double e = target.getBodyY(0.3333333333333333) - persistentProjectileEntity.getY();
        double f = target.getZ() - this.getZ();
        double g = Math.sqrt(d * d + f * f);
        persistentProjectileEntity.setVelocity(d, e + g * (double)0.2F, f, 1.6F, (float)(14 - this.getWorld().getDifficulty().getId() * 4));
        this.getWorld().spawnEntity(persistentProjectileEntity);
        if (this.getWorld().isClient()) spitAnimationState.start(this.age);
    }
}
