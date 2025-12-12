package arr.armuriii.spiritum.entity.goal;

import arr.armuriii.spiritum.entity.ImpEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.Path;
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

    private static final long MAX_HOLDING_TIME = 20L;
    private long lastUpdateTime;

    public HoldingGoal(ImpEntity mob, double speed, boolean pauseWhenMobIdle) {
        this.mob = mob;
        this.speed = speed;
        this.pauseWhenMobIdle = pauseWhenMobIdle;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        long l = this.mob.getWorld().getTime();
        if (l - this.lastUpdateTime < MAX_HOLDING_TIME)
            return false;
        this.lastUpdateTime = l;
        if (this.mob.hasPassengers())
            return false;
        if (this.mob.getHealth() < this.mob.getMaxHealth()/1.5)
            return false;
        if (!this.mob.getHelpRequired() && !this.mob.hasVehicle()) {
            List<ImpEntity> imps = this.mob.getWorld().getEntitiesByClass(ImpEntity.class, new Box(this.mob.getBlockPos()).expand(10, 3, 10),
                    (imp -> imp.isOwner(this.mob.getOwner()) && imp.getHelpRequired() && !imp.hasVehicle()));
            if (!imps.isEmpty()) {
                help = imps.getFirst();
                if (!help.isAlive())
                    return false;
                this.path = this.mob.getNavigation().findPathTo(help, 0);
                return this.path != null || this.mob.isInAttackRange(help);
            }
        }
        return false;
    }

    @Override
    public boolean shouldContinue() {
        if (!help.isAlive()) {
            return false;
        } else if (help.hasVehicle() && help.getVehicle() != this.mob) {
            return false;
        } else if (!this.pauseWhenMobIdle && !this.mob.getNavigation().isIdle()) {
            return true;
        } else return this.mob.isInWalkTargetRange(help.getBlockPos());
    }

    @Override
    public void start() {
        this.mob.getNavigation().startMovingAlong(this.path, this.speed);
        this.updateCountdownTicks = 0;
        this.cooldown = 0;
    }

    @Override
    public void tick() {
        if (help != null && !mob.hasPassenger(help)) {
            this.mob.getLookControl().lookAt(help, 30.0F, 30.0F);
            this.updateCountdownTicks = Math.max(this.updateCountdownTicks - 1, 0);
            if ((this.pauseWhenMobIdle || this.mob.getVisibilityCache().canSee(help))
                    && this.updateCountdownTicks <= 0
                    && (
                    this.targetX == 0.0f && this.targetY == 0.0f && this.targetZ == 0.0f
                            || help.squaredDistanceTo(this.targetX, this.targetY, this.targetZ) >= 1.0f
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


    @Override
    public void stop() {
        super.stop();
        this.mob.getNavigation().stop();
    }

    protected void attack(LivingEntity target) {
        this.mob.swingHand(Hand.MAIN_HAND);
        if (this.mob.distanceTo(target) < 3) {
            target.removeAllPassengers();
            target.startRiding(this.mob,true);
        }
    }
}
