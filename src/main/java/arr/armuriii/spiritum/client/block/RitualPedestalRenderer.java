package arr.armuriii.spiritum.client.block;

import arr.armuriii.spiritum.block.entity.RitualPedestalEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.RotationAxis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RitualPedestalRenderer implements BlockEntityRenderer<RitualPedestalEntity> {
    private final ItemRenderer itemRenderer;

    public RitualPedestalRenderer(BlockEntityRendererFactory.Context ctx) {
        this.itemRenderer = ctx.getItemRenderer();
    }

    @Override
    public void render(RitualPedestalEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (!entity.getItems().isEmpty()) {
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
    }
}
