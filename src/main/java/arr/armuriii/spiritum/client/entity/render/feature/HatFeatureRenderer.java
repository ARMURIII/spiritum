package arr.armuriii.spiritum.client.entity.render.feature;

import arr.armuriii.spiritum.init.SpiritumItems;
import dev.emi.trinkets.api.LivingEntityTrinketComponent;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.SkullBlock;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.SkullBlockEntityModel;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LimbAnimator;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

import java.util.Map;
import java.util.Optional;

public class HatFeatureRenderer<T extends LivingEntity, M extends EntityModel<T> & ModelWithHead> extends FeatureRenderer<T,M > {
    private final float scaleX;
    private final float scaleY;
    private final float scaleZ;
    private final HeldItemRenderer heldItemRenderer;

    public HatFeatureRenderer(FeatureRendererContext context, HeldItemRenderer heldItemRenderer) {
        this(context, 1.0F, 1.0F, 1.0F, heldItemRenderer);
    }

    public HatFeatureRenderer(FeatureRendererContext context, float scaleX, float scaleY, float scaleZ, HeldItemRenderer heldItemRenderer) {
        super(context);
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
        this.heldItemRenderer = heldItemRenderer;
    }

    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, T entity, float f, float g, float h, float j, float k, float l) {
        Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(entity);
        if (component.isPresent() && component.get().isEquipped(SpiritumItems.SUMMONING_HAT)) {
            ItemStack itemStack = component.get().getEquipped(SpiritumItems.SUMMONING_HAT).getFirst().getRight();

            matrixStack.push();
            matrixStack.scale(this.scaleX, this.scaleY, this.scaleZ);
            boolean forehead = entity instanceof VillagerEntity || entity instanceof ZombieVillagerEntity;
            if (entity.isBaby() && !(entity instanceof VillagerEntity)) {
                matrixStack.translate(0.0F, 0.03125F, 0.0F);
                matrixStack.scale(0.7F, 0.7F, 0.7F);
                matrixStack.translate(0.0F, 1.0F, 0.0F);
            }

            this.getContextModel().getHead().rotate(matrixStack);
            HeadFeatureRenderer.translate(matrixStack, forehead);
            this.heldItemRenderer.renderItem(
                    entity, itemStack, ModelTransformationMode.HEAD,
                    false, matrixStack, vertexConsumerProvider, i);

            matrixStack.pop();
        }
    }
}
