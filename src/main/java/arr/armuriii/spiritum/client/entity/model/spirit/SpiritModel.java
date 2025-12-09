package arr.armuriii.spiritum.client.entity.model.spirit;

import arr.armuriii.spiritum.entity.SpiritEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import org.joml.Vector3f;

public class SpiritModel extends SinglePartEntityModel<SpiritEntity> {
	private final ModelPart body;
	public SpiritModel(ModelPart root) {
		this.body = root.getChild("body");
	}
	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData body = modelPartData.addChild("body", ModelPartBuilder
				.create().uv(0, 0)
				.cuboid(-5.0F, -5.0F, -5.0F, 10.0F, 15.0F, 10.0F, new Dilation(0.0F))
		.uv(0, 0)
				.cuboid(-5.0F, -2.5F, -5.0F, 10.0F, 0.0F, 10.0F, new Dilation(0.0F)),
				ModelTransform.pivot(0.0F, 5.0F, 0.0F));
		return TexturedModelData.of(modelData, 64, 64);
	}
	@Override
	public void setAngles(SpiritEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		//this.getPart().traverse().forEach(ModelPart::resetTransform);
		this.animateMovement(SpiritAnimation.IDLE,limbSwing,limbSwingAmount,16.5F, 2.5F);
	}

	@Override
	public ModelPart getPart() {
		return body;
	}
}