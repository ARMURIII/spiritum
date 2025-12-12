package arr.armuriii.spiritum.entity.goal;

import arr.armuriii.spiritum.entity.ImpEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class HealthProjectileAttackGoal extends Goal {
    private final PathAwareEntity mob;
    private final RangedAttackMob owner;
    @Nullable
    private LivingEntity target;
    private int updateCountdownTicks;
    private final double mobSpeed;
    private int seenTargetTicks;
    private final int minIntervalTicks;
    private final int maxIntervalTicks;
    private final float maxShootRange;
    private final float squaredMaxShootRange;
    private final float squaredMinShootRange;
    @Nullable
    protected Path fleePath;

    public HealthProjectileAttackGoal(RangedAttackMob mob, double mobSpeed, int intervalTicks, float maxShootRange, float minShootRange) {
        this(mob, mobSpeed, intervalTicks, intervalTicks, maxShootRange,minShootRange);
    }

    public HealthProjectileAttackGoal(RangedAttackMob mob, double mobSpeed, int minIntervalTicks, int maxIntervalTicks, float maxShootRange, float minShootRange) {
        this.updateCountdownTicks = -1;
        if (!(mob instanceof LivingEntity)) {
            throw new IllegalArgumentException("ArrowAttackGoal requires Mob implements RangedAttackMob");
        } else {
            this.owner = mob;
            this.mob = (PathAwareEntity) mob;
            this.mobSpeed = mobSpeed;
            this.minIntervalTicks = minIntervalTicks;
            this.maxIntervalTicks = maxIntervalTicks;
            this.maxShootRange = maxShootRange;
            this.squaredMaxShootRange = maxShootRange * maxShootRange;
            this.squaredMinShootRange = minShootRange * minShootRange;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }
    }

    public boolean canStart() {
        LivingEntity livingEntity = this.mob.getTarget();
        if (livingEntity != null && livingEntity.isAlive() && mob.getHealth()<=(mob.getMaxHealth()/2) &&
                !(livingEntity instanceof ImpEntity imp && mob instanceof ImpEntity imp1 && imp.getOwner() != null && imp1.getOwner() != null &&
                        imp.getOwner().getUuid() == imp1.getOwner().getUuid())) {
            this.target = livingEntity;
            Vec3d vec3d = NoPenaltyTargeting.findFrom(this.mob, 16, 7, this.target.getPos());
            if (vec3d != null && this.target.squaredDistanceTo(vec3d.x, vec3d.y, vec3d.z) > this.target.squaredDistanceTo(this.mob))
                this.fleePath = this.mob.getNavigation().findPathTo(vec3d.x, vec3d.y, vec3d.z, 0);
            return true;
        } else {
            return false;
        }
    }

    public boolean shouldContinue() {
        return (this.canStart() || (this.target != null && this.target.isAlive()) && !this.mob.getNavigation().isIdle()) && mob.getHealth()<=(mob.getMaxHealth()/2);
    }

    public void stop() {
        this.target = null;
        this.seenTargetTicks = 0;
        this.updateCountdownTicks = -1;

        this.mob.dismountVehicle();

        if (this.mob instanceof ImpEntity imp)
            imp.setHelpRequired(false);
    }

    @Override
    public boolean shouldRunEveryTick() {
        return false;
    }

    public void tick() {
        if (this.target == null) return;
        double d = this.mob.squaredDistanceTo(this.target.getX(), this.target.getY(), this.target.getZ());

        boolean bl = this.mob.getVisibilityCache().canSee(this.target);
        if (bl) {
            ++this.seenTargetTicks;
        } else {
            this.seenTargetTicks = 0;
        }

        if (!(d > (double)this.squaredMaxShootRange) && this.seenTargetTicks >= 5) {
            if (!(d <= (double)this.squaredMinShootRange)) {
                this.mob.dismountVehicle();
                if (this.mob instanceof ImpEntity imp)
                    imp.setHelpRequired(false);
                this.mob.getNavigation().stop();
            }else {
                this.mob.getNavigation().startMovingAlong(this.fleePath,this.mob.hasVehicle() ? 1.0 : this.mobSpeed);
                if (this.mob instanceof ImpEntity imp)
                    imp.setHelpRequired(true);
                return;
            }
        } else {
            this.mob.getNavigation().startMovingTo(this.target,this.mob.hasVehicle() ? 1.0 : this.mobSpeed);
            if (this.mob instanceof ImpEntity imp)
                imp.setHelpRequired(true);
            return;
        }

        this.mob.getLookControl().lookAt(this.target, 45.0F, 45.0F);
        if (--this.updateCountdownTicks == 0) {

            float f = (float)Math.sqrt(d) / this.maxShootRange;
            float g = MathHelper.clamp(f, 0.1F, 1.0F);
            if (this.mob instanceof ImpEntity imp)
                imp.shootAt(this.target, g);
            this.updateCountdownTicks = MathHelper.floor(f * (float)(this.maxIntervalTicks - this.minIntervalTicks) + (float)this.minIntervalTicks);
        } else if (this.updateCountdownTicks < 0) {
            this.updateCountdownTicks = MathHelper.floor(MathHelper.lerp(Math.sqrt(d) / (double)this.maxShootRange, this.minIntervalTicks, this.maxIntervalTicks));
        }

    }
}
