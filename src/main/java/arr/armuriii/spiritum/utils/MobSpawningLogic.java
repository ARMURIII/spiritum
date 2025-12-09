package arr.armuriii.spiritum.utils;

import arr.armuriii.spiritum.init.SpiritumParticles;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.event.GameEvent;

public class MobSpawningLogic {

    public static <T extends Entity> void spawnNearby(ServerWorld world, BlockPos pos, T entity,EntityType<T> type , int spawnCount, double spawnRange) {
        Random random = world.getRandom();
        for(int i = 0; i < spawnCount; ++i) {
            double d = pos.getX() + (random.nextDouble() - random.nextDouble()) * spawnRange + 0.5;
            double e = pos.getY() + random.nextInt(3) - 1;
            double f = pos.getZ() + (random.nextDouble() - random.nextDouble()) * spawnRange + 0.5;

            Entity copy = type.create(world);
            if (copy != null)
                copy.copyFrom(entity);
            else
                copy = entity;

            if (world.isSpaceEmpty(type.getSpawnBox(d, e, f))) {
                BlockPos blockPos = BlockPos.ofFloored(d, e, f);
                if (!type.getSpawnGroup().isPeaceful() && world.getDifficulty() == Difficulty.PEACEFUL)
                    continue;
                if (!SpawnRestriction.canSpawn(type, world, SpawnReason.MOB_SUMMONED, blockPos, world.getRandom()))
                    continue;

                copy.refreshPositionAndAngles(d, e, f, random.nextFloat() * 360.0F, 0.0F);
                if (copy instanceof MobEntity mobEntity)
                    mobEntity.initialize(world, world.getLocalDifficulty(mobEntity.getBlockPos()), SpawnReason.MOB_SUMMONED, null);
                copy.setVelocity(Vec3d.ZERO);
                if (!world.spawnNewEntityAndPassengers(copy))
                    return;

                for(int s = 0; s < 20; ++s) {
                    double t = (double)pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
                    double u = (double)pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
                    double v = (double)pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
                    world.addParticle(SpiritumParticles.HEXFLAME_TYPE, t, u, v, 0, 0, 0);
                }
                world.emitGameEvent(copy, GameEvent.ENTITY_PLACE, blockPos);
                if (copy instanceof MobEntity mobEntity)
                    mobEntity.playSpawnEffects();
            }
        }
    }
}
