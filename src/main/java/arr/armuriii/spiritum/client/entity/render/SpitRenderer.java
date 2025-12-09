package arr.armuriii.spiritum.client.entity.render;

import arr.armuriii.spiritum.Spiritum;
import arr.armuriii.spiritum.entity.projectile.SpitProjectileEntity;
import arr.armuriii.spiritum.init.SpiritumParticles;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class SpitRenderer extends EntityRenderer<SpitProjectileEntity> {
    private static final Identifier TEXTURE = Spiritum.id("textures/entity/imp.png");

    public SpitRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(SpitProjectileEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
        if (MinecraftClient.getInstance() != null)
            spawnParticles(5,entity,MinecraftClient.getInstance(),
                    MathHelper.lerp(tickDelta,entity.prevX,entity.getX()),
                    MathHelper.lerp(tickDelta,entity.prevY,entity.getY()),
                    MathHelper.lerp(tickDelta,entity.prevZ,entity.getZ())
            );
    }

    @SuppressWarnings("SameParameterValue")
    private void spawnParticles(int amount, SpitProjectileEntity entity, MinecraftClient client, double x, double y, double z) {
        for (int j = 0; j < amount; j++)
            client.particleManager.addParticle(
                    SpiritumParticles.IMP_TYPE,
                    x, y, z,
                    entity.getVelocity().x, entity.getVelocity().y, entity.getVelocity().z
            );
    }

    @Override
    public Identifier getTexture(SpitProjectileEntity entity) {
        return null;
    }
}