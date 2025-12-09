package arr.armuriii.spiritum.entity;

import arr.armuriii.spiritum.init.SpiritumItems;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.function.Predicate;

public class SpiritEntity extends TameableEntity {
    protected static final TrackedData<Byte> SPIRIT_FLAGS;
    @Nullable
    LivingEntity owner;
    @Nullable
    private BlockPos bounds;
    private boolean alive;
    private int lifeTicks;

    public SpiritEntity(EntityType<? extends SpiritEntity> entityType, World world) {
        super(entityType, world);
        this.moveControl = new SpiritMoveControl(this);
        this.experiencePoints = 3;
    }

    public void move(MovementType movementType, Vec3d movement) {
        super.move(movementType, movement);
        this.checkBlockCollision();
    }

    public void tick() {
        this.noClip = true;
        super.tick();
        this.noClip = false;
        this.setNoGravity(true);
        if (this.alive && --this.lifeTicks <= 0) {
            this.lifeTicks = 20;
            this.damage(this.getDamageSources().starve(), 1.0F);
        }

    }

    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(4, new SpiritEntity.ChargeTargetGoal());
        this.goalSelector.add(8, new SpiritEntity.LookAtTargetGoal());
        this.goalSelector.add(9, new LookAtEntityGoal(this, PlayerEntity.class, 3.0F, 1.0F));
        this.goalSelector.add(10, new LookAtEntityGoal(this, MobEntity.class, 8.0F));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class,false,
                (target-> this.getOwner() == null || this.getOwner().getUuid() != target.getUuid())));
    }

    public static DefaultAttributeContainer.Builder createSpiritAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 8.0F)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0F);
    }

    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(SPIRIT_FLAGS, (byte)0);
    }

    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("BoundX"))
            this.bounds = new BlockPos(nbt.getInt("BoundX"), nbt.getInt("BoundY"), nbt.getInt("BoundZ"));

        if (nbt.contains("LifeTicks"))
            this.setLifeTicks(nbt.getInt("LifeTicks"));
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return false;
    }

    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (itemStack.isOf(Items.GLASS_BOTTLE)) {
            itemStack.splitUnlessCreative(1, player);
            if (itemStack.isEmpty())
                player.setStackInHand(hand,SpiritumItems.SPIRIT_BOTTLE.getDefaultStack());
            else
                player.giveItemStack(SpiritumItems.SPIRIT_BOTTLE.getDefaultStack());
            this.getWorld().playSound(null,player.getBlockPos(),SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.PLAYERS);
            this.discard();
            return ActionResult.success(this.getWorld().isClient);
        } else {
            return super.interactMob(player, hand);
        }
    }

    public void copyFrom(Entity original) {
        super.copyFrom(original);
        if (original instanceof SpiritEntity spirit) {
            this.owner = spirit.getOwner();
        }

    }

    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (this.bounds != null) {
            nbt.putInt("BoundX", this.bounds.getX());
            nbt.putInt("BoundY", this.bounds.getY());
            nbt.putInt("BoundZ", this.bounds.getZ());
        }

        if (this.alive) {
            nbt.putInt("LifeTicks", this.lifeTicks);
        }

    }

    @Nullable
    public LivingEntity getOwner() {
        return this.owner;
    }

    @Nullable
    public BlockPos getBounds() {
        return this.bounds;
    }

    public void setBounds(@Nullable BlockPos bounds) {
        this.bounds = bounds;
    }

    private boolean areFlagsSet() {
        int i = this.dataTracker.get(SPIRIT_FLAGS);
        return (i & 1) != 0;
    }

    private void setFlags(boolean value) {
        int i = this.dataTracker.get(SPIRIT_FLAGS);
        if (value)
            i |= 1;
        else
            i &= ~1;


        this.dataTracker.set(SPIRIT_FLAGS, (byte)(i & 255));
    }

    public boolean isCharging() {
        return this.areFlagsSet();
    }

    public void setCharging(boolean charging) {
        this.setFlags(charging);
    }

    public void setOwner(@Nullable LivingEntity owner) {
        this.owner = owner;
    }

    public void setLifeTicks(int lifeTicks) {
        this.alive = true;
        this.lifeTicks = lifeTicks;
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_PHANTOM_AMBIENT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_VEX_HURT;
    }

    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_VEX_HURT;
    }

    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        Random random = world.getRandom();
        this.initEquipment(random, difficulty);
        this.updateEnchantments(world, random, difficulty);
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    @Override
    public @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
        this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        this.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    static {
        SPIRIT_FLAGS = DataTracker.registerData(SpiritEntity.class, TrackedDataHandlerRegistry.BYTE);
    }

    class SpiritMoveControl extends MoveControl {
        public SpiritMoveControl(final SpiritEntity owner) {
            super(owner);
        }

        public void tick() {
            if (this.state == State.MOVE_TO) {
                Vec3d vec3d = new Vec3d(this.targetX - SpiritEntity.this.getX(), this.targetY - SpiritEntity.this.getY(), this.targetZ - SpiritEntity.this.getZ());
                double d = vec3d.length();
                if (d < SpiritEntity.this.getBoundingBox().getAverageSideLength()) {
                    this.state = State.WAIT;
                    SpiritEntity.this.setVelocity(SpiritEntity.this.getVelocity().multiply(0.5F));
                } else {
                    SpiritEntity.this.setVelocity(SpiritEntity.this.getVelocity().add(vec3d.multiply(this.speed * 0.05 / d)));
                    if (SpiritEntity.this.getTarget() == null) {
                        Vec3d vec3d2 = SpiritEntity.this.getVelocity();
                        SpiritEntity.this.setYaw(-((float)MathHelper.atan2(vec3d2.x, vec3d2.z)) * (180F / (float)Math.PI));
                    } else {
                        double e = SpiritEntity.this.getTarget().getX() - SpiritEntity.this.getX();
                        double f = SpiritEntity.this.getTarget().getZ() - SpiritEntity.this.getZ();
                        SpiritEntity.this.setYaw(-((float)MathHelper.atan2(e, f)) * (180F / (float)Math.PI));
                    }
                    SpiritEntity.this.bodyYaw = SpiritEntity.this.getYaw();
                }

            }
        }
    }

    class ChargeTargetGoal extends Goal {
        public ChargeTargetGoal() {
            this.setControls(EnumSet.of(Control.MOVE));
        }

        public boolean canStart() {
            LivingEntity livingEntity = SpiritEntity.this.getTarget();
            if (livingEntity != null && livingEntity.isAlive() && !SpiritEntity.this.getMoveControl().isMoving() && SpiritEntity.this.random.nextInt(toGoalTicks(7)) == 0) {
                return SpiritEntity.this.squaredDistanceTo(livingEntity) > (double)4.0F;
            } else {
                return false;
            }
        }

        public boolean shouldContinue() {
            return SpiritEntity.this.getMoveControl().isMoving() && SpiritEntity.this.isCharging() && SpiritEntity.this.getTarget() != null && SpiritEntity.this.getTarget().isAlive();
        }

        public void start() {
            LivingEntity livingEntity = SpiritEntity.this.getTarget();
            if (livingEntity != null) {
                Vec3d vec3d = livingEntity.getEyePos();
                SpiritEntity.this.moveControl.moveTo(vec3d.x, vec3d.y, vec3d.z, 1.0F);
            }

            SpiritEntity.this.setCharging(true);
        }

        public void stop() {
            SpiritEntity.this.setCharging(false);
        }

        public boolean shouldRunEveryTick() {
            return true;
        }

        public void tick() {
            LivingEntity livingEntity = SpiritEntity.this.getTarget();
            if (livingEntity != null) {
                if (SpiritEntity.this.getBoundingBox().intersects(livingEntity.getBoundingBox())) {
                    SpiritEntity.this.tryAttack(livingEntity);
                    SpiritEntity.this.setCharging(false);
                } else {
                    double d = SpiritEntity.this.squaredDistanceTo(livingEntity);
                    if (d < (double)9.0F) {
                        Vec3d vec3d = livingEntity.getEyePos();
                        SpiritEntity.this.moveControl.moveTo(vec3d.x, vec3d.y, vec3d.z, 1.0F);
                    }
                }

            }
        }
    }

    class LookAtTargetGoal extends Goal {
        public LookAtTargetGoal() {
            this.setControls(EnumSet.of(Control.MOVE));
        }

        public boolean canStart() {
            return !SpiritEntity.this.getMoveControl().isMoving() && SpiritEntity.this.random.nextInt(toGoalTicks(7)) == 0;
        }

        public boolean shouldContinue() {
            return false;
        }

        public void tick() {
            BlockPos blockPos = SpiritEntity.this.getBounds();
            if (blockPos == null) {
                blockPos = SpiritEntity.this.getBlockPos();
            }

            for(int i = 0; i < 3; ++i) {
                BlockPos blockPos2 = blockPos.add(SpiritEntity.this.random.nextInt(15) - 7, SpiritEntity.this.random.nextInt(11) - 5, SpiritEntity.this.random.nextInt(15) - 7);
                if (SpiritEntity.this.getWorld().isAir(blockPos2)) {
                    SpiritEntity.this.moveControl.moveTo((double)blockPos2.getX() + 0.5, (double)blockPos2.getY() + 0.5, (double)blockPos2.getZ() + 0.5, 0.25);
                    if (SpiritEntity.this.getTarget() == null) {
                        SpiritEntity.this.getLookControl().lookAt((double)blockPos2.getX() + 0.5, (double)blockPos2.getY() + 0.5, (double)blockPos2.getZ() + 0.5, 180.0F, 20.0F);
                    }
                    break;
                }
            }

        }
    }
}