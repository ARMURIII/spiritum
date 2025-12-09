package arr.armuriii.spiritum.mixin;

import arr.armuriii.spiritum.init.SpiritumItems;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.emi.trinkets.api.LivingEntityTrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ArmorFeatureRenderer.class)
public class ArmorFeatureRendererMixin {

    @WrapMethod(method = "renderArmor")
    private void spiritum$cancelHeadWhenHat(MatrixStack matrices, VertexConsumerProvider vertexConsumers, LivingEntity entity, EquipmentSlot armorSlot, int light, BipedEntityModel<?> model, Operation<Void> original) {
        if (armorSlot == EquipmentSlot.HEAD)
            if (TrinketsApi.getTrinketComponent(entity).orElse(new LivingEntityTrinketComponent(entity)).isEquipped(SpiritumItems.SUMMONING_HAT))
                return;
        original.call(matrices, vertexConsumers, entity, armorSlot, light, model);
    }
}
