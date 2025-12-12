package arr.armuriii.spiritum.entity.goal;

import arr.armuriii.spiritum.entity.ImpEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class HealthMeleeAttackGoal extends MeleeAttackGoal {
    public HealthMeleeAttackGoal(PathAwareEntity mob, double speed, boolean pauseWhenMobIdle) {
        super(mob, speed, pauseWhenMobIdle);
    }

    @Override
    public boolean canStart() {
        return super.canStart() && mob.getHealth() > (mob.getMaxHealth()/2);
    }

    @Override
    public boolean shouldRunEveryTick() {
        return false;
    }

    @Override
    public boolean shouldContinue() {
        return super.shouldContinue() && mob.getHealth() > (mob.getMaxHealth()/2);
    }
}
