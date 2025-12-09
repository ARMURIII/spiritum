package arr.armuriii.spiritum.entity.goal;

import arr.armuriii.spiritum.entity.ImpEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;

import java.util.EnumSet;
import java.util.List;

public class HoldingGoal  extends Goal {
    protected final ImpEntity mob;
    protected ImpEntity help = null;
    private final double speed;
    private final boolean pauseWhenMobIdle;
    private Path path;
    private double targetX;
    private double targetY;
    private double targetZ;
    private int updateCountdownTicks;
    private int cooldown;
    private long lastUpdateTime;
    private static final long MAX_ATTACK_TIME = 20L;

    public HoldingGoal(ImpEntity mob, double speed, boolean pauseWhenMobIdle) {
        this.mob = mob;
        this.speed = speed;
        this.pauseWhenMobIdle = pauseWhenMobIdle;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        long l = this.mob.getWorld().getTime();
        if (l - this.lastUpdateTime >= MAX_ATTACK_TIME && !this.mob.getHelpRequired() && !this.mob.hasVehicle()) {
            this.lastUpdateTime = l;
            List<ImpEntity> imps = this.mob.getWorld().getEntitiesByClass(ImpEntity.class, new Box(this.mob.getBlockPos()).expand(25, 5, 25),
                    (imp -> imp.getOwnerUuid() == this.mob.getOwnerUuid() && imp.getHelpRequired() && !imp.hasVehicle()));

            if (!imps.isEmpty() && imps.getFirst().isAlive()) {
                help = imps.getFirst();
                if (help == null) {
                    return false;
                } else if (!help.isAlive()) {
                    return false;
                } else {
                    this.path = this.mob.getNavigation().findPathTo(help, 0);
                    return this.path != null || this.mob.isInAttackRange(help);
                }
            }
        }
        return false;
    }

    @Override
    public boolean shouldContinue() {
        List<ImpEntity> imps = this.mob.getWorld().getEntitiesByClass(ImpEntity.class,new Box(this.mob.getBlockPos()).expand(25,5,25),
                (imp -> imp.getOwnerUuid() == this.mob.getOwnerUuid() && imp.getHelpRequired() &&
                        (imp.getVehicle() != null && imp.getVehicle().getUuid() == this.mob.getUuid())));

        if (!imps.isEmpty() && !this.mob.getHelpRequired() && imps.getFirst().isAlive()) {
            help = imps.getFirst();
            if (!help.isAlive()) {
                return false;
            } else if (!this.pauseWhenMobIdle && !this.mob.getNavigation().isIdle()) {
                return true;
            } else return this.mob.isInWalkTargetRange(help.getBlockPos());
        }
        return false;
    }

    @Override
    public void start() {
        this.mob.getNavigation().startMovingAlong(this.path, this.speed);
        this.updateCountdownTicks = 0;
        this.cooldown = 0;
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (help != null && !mob.hasPassenger(help)) {
            this.mob.getLookControl().lookAt(help, 30.0F, 30.0F);
            this.updateCountdownTicks = Math.max(this.updateCountdownTicks - 1, 0);
            if ((this.pauseWhenMobIdle || this.mob.getVisibilityCache().canSee(help))
                    && this.updateCountdownTicks <= 0
                    && (
                    this.targetX == 0.0 && this.targetY == 0.0 && this.targetZ == 0.0
                            || help.squaredDistanceTo(this.targetX, this.targetY, this.targetZ) >= 1.0
                            || this.mob.getRandom().nextFloat() < 0.05F
            )) {
                this.targetX = help.getX();
                this.targetY = help.getY();
                this.targetZ = help.getZ();
                this.updateCountdownTicks = 4 + this.mob.getRandom().nextInt(7);
                double d = this.mob.squaredDistanceTo(help);
                if (d > 1024.0) {
                    this.updateCountdownTicks += 10;
                } else if (d > 256.0) {
                    this.updateCountdownTicks += 5;
                }

                if (!this.mob.getNavigation().startMovingTo(help, this.speed)) {
                    this.updateCountdownTicks += 15;
                }

                this.updateCountdownTicks = this.getTickCount(this.updateCountdownTicks);
            }

            this.cooldown = Math.max(this.cooldown - 1, 0);
            this.attack(help);
        }
    }

    protected void attack(LivingEntity target) {
        this.mob.swingHand(Hand.MAIN_HAND);
        if (this.mob.distanceTo(target) < 3) {
            target.removeAllPassengers();
            target.startRiding(this.mob,true);
        }
    }
}
