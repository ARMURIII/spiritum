package arr.armuriii.spiritum.entity.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.List;
import java.util.function.Predicate;

public class AvoidBlockGoal extends Goal {
    private final LivingEntity mob;
    private final float speed;
    private final int radius;
    private Path path;
    private EntityNavigation navigation;
    private PathAwareEntity pathEntity;
    private final Predicate<BlockPos> pos;
    private List<BlockPos> repellents;
    protected int cooldown;

    public AvoidBlockGoal(LivingEntity entity, Predicate<BlockPos> condition, int radius, float speed) {
        this.mob = entity;
        this.speed = speed;
        this.radius = radius;
        pos = condition;
        if (entity instanceof PathAwareEntity) {
            pathEntity = (PathAwareEntity) entity;
            navigation = pathEntity.getNavigation();
        }
    }

    @Override
    public boolean canStart() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        this.repellents = findClosestList(this.mob.getBlockPos(),radius,5,pos);
        this.cooldown = toGoalTicks(2);
        if (!repellents.isEmpty())
            return generatePath();

        return false;
    }

    @Override
    public boolean shouldContinue() {
        return !path.isFinished();
    }

    private boolean generatePath() {
        Vec3d pos = this.repellents.getFirst().toCenterPos();
        Vec3d newPos = null;
        for (int i = 0;i < 5;i++) {
            newPos = NoPenaltyTargeting.findFrom(pathEntity, 32, 6, pos);
            if (newPos == null)
                continue;
            if (newPos.squaredDistanceTo(pos) < mob.squaredDistanceTo(pos))
                continue;
            break;
        }
        if (newPos == null) return false;

        path = navigation.findPathTo(BlockPos.ofFloored(newPos), 0);
        if (path != null) {
            return true;
        }

        return false;
    }

    @Override
    public void start() {
        navigation.startMovingAlong(path, speed);
    }

    private static List<BlockPos> findClosestList(BlockPos pos, int horizontalRange, int verticalRange, Predicate<BlockPos> condition) {
        List<BlockPos> blocks = new java.util.ArrayList<>(List.of());
        for (BlockPos blockPos : BlockPos.iterateOutwards(pos, horizontalRange, verticalRange, horizontalRange)) {
            if (condition.test(blockPos)) {
                blocks.add(blockPos);
            }
        }
        return blocks;
    }
}