package arr.armuriii.spiritum.entity.goal;

import arr.armuriii.spiritum.entity.ImpEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.mob.PathAwareEntity;

public class HealthMeleeAttackGoal extends MeleeAttackGoal {
    public HealthMeleeAttackGoal(PathAwareEntity mob, double speed, boolean pauseWhenMobIdle) {
        super(mob, speed, pauseWhenMobIdle);
    }

    @Override
    public boolean canStart() {
        return super.canStart() && !(mob.getTarget() instanceof ImpEntity imp &&
                mob instanceof ImpEntity imp1 && imp.getOwner() != null && imp1.getOwner() != null &&
                imp.getOwner().getUuid() == imp1.getOwner().getUuid()) && mob.getHealth() > (mob.getMaxHealth()/2);
    }

    @Override
    public boolean shouldContinue() {
        return super.shouldContinue() && mob.getHealth() > (mob.getMaxHealth()/2);
    }
}
