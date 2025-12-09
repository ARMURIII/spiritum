package arr.armuriii.spiritum.client.block;

import arr.armuriii.spiritum.Spiritum;
import arr.armuriii.spiritum.block.entity.RitualPedestalEntity;
import arr.armuriii.spiritum.client.SpiritumClient;
import arr.armuriii.spiritum.entity.projectile.SpitProjectileEntity;
import arr.armuriii.spiritum.init.SpiritumParticles;
import arr.armuriii.spiritum.init.SpiritumRituals;
import arr.armuriii.spiritum.rituals.Ritual;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RitualPedestalRenderer implements BlockEntityRenderer<RitualPedestalEntity> {
    private final ItemRenderer itemRenderer;
    private final BlockRenderManager renderManager;

    public RitualPedestalRenderer(BlockEntityRendererFactory.Context ctx) {
        this.itemRenderer = ctx.getItemRenderer();
        renderManager = ctx.getRenderManager();
    }

    @Override
    public void render(RitualPedestalEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        renderPlacedItems(entity, tickDelta, matrices, vertexConsumers, light, overlay);
        renderRitualComponents(entity, tickDelta, matrices, vertexConsumers, light, overlay);
    }

    private void renderPlacedItems(RitualPedestalEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity.getItems().isEmpty())
            return;

        List<ItemStack> items = new ArrayList<>(entity.getItems());
        items.removeIf(ItemStack::isEmpty);
        for (int i = 0; i < items.size(); i++) {
            matrices.push();
            matrices.translate(0.5,1.25,0.5);
            //this is probably hideous, but I guess it works, and I'm not going to steal someone's code
            float tick = ((entity.getWorld() == null ? 20 : (entity.getWorld().getTime()))%256)+tickDelta;
            double angle = (Math.PI*tick)/128;
            double offset = (Math.PI*2/items.size())*i;

            if (items.size() > 1) {
                matrices.translate(Math.cos(angle+offset)/3,
                        0,
                        Math.sin(angle+offset)/3);
            }
            matrices.translate(0,
                    Math.cos((angle+offset)*2)/16,
                    0);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotation((float) (angle+offset)));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation((float) (angle+offset)));
            matrices.scale(0.5f,0.5f,0.5f);
            this.itemRenderer
                    .renderItem(items.get(i), ModelTransformationMode.FIXED,
                            light, overlay, matrices, vertexConsumers, entity.getWorld(), 0);
            matrices.pop();
        }
    }

    private void renderRitualComponents(RitualPedestalEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (!entity.isActive())
            return;
        if (entity.getRitual() == SpiritumRituals.EMPTY)
            return;
        if (entity.getRitual().getType() == Ritual.Type.INSTANT)
            return;

        List<ItemStack> items = new ArrayList<>(List.of(entity.getRitual().getComponent().getMatchingStacks()));
        items.removeIf(ItemStack::isEmpty);
        for (int i = 0; i < items.size(); i++) {
            matrices.push();
            matrices.translate(0.5,1.25,0.5);
            //this is probably hideous, but I guess it works, and I'm not going to steal someone's code
            float tick = ((entity.getWorld() == null ? 20 : (entity.getWorld().getTime()))%128)+tickDelta;
            double angle = (Math.PI*tick)/64;
            double offset = (Math.PI*2/items.size())*i;

            matrices.translate(Math.cos(angle+offset)/2,
                    0,
                    Math.sin(angle+offset)/2);

            double height = Math.cos((angle + offset) * 2) / 4;
            matrices.translate(0,
                    height+1,
                    0);
            Random random = new Random(entity.getWorld().getTime()+(long)(10*tickDelta));
            if (MinecraftClient.getInstance() != null && random.nextInt(25) == 0)
                spawnParticles(5,MinecraftClient.getInstance(),
                        Math.cos(angle+offset)/2+entity.getPos().toCenterPos().x,
                        height+1.75+entity.getPos().toCenterPos().y,
                        Math.sin(angle+offset)/2+entity.getPos().toCenterPos().z
                );
            matrices.multiply(RotationAxis.POSITIVE_Z.rotation((float) (angle+offset)));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation((float) (angle+offset)));
            matrices.scale(0.5f,0.5f,0.5f);
            this.itemRenderer
                    .renderItem(items.get(i), ModelTransformationMode.FIXED,
                            light, overlay, matrices, vertexConsumers, entity.getWorld(), 0);
            matrices.pop();
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void spawnParticles(int amount, MinecraftClient client, double x, double y, double z) {
        for (int j = 0; j < amount; j++)
            client.particleManager.addParticle(
                    SpiritumParticles.HEXFLAME_TYPE,
                    x, y, z,
                    0, 0, 0
            );
    }
}
