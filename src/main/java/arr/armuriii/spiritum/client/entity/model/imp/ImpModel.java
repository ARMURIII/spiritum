package arr.armuriii.spiritum.client.entity.model.imp;

import arr.armuriii.spiritum.entity.ImpEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.FrogEntityModel;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

public class ImpModel<T extends Entity> extends SinglePartEntityModel<T> {
	private final ModelPart root;
	private final ModelPart body;
	private final ModelPart leftArm;
	private final ModelPart rightArm;
	private final ModelPart head;
	private final ModelPart leftLeg;
	private final ModelPart rightLeg;
	public ImpModel(ModelPart root) {
		this.root = root.getChild("root");
		this.body = this.root.getChild("body");
		this.leftArm = this.body.getChild("left_arm");
		this.rightArm = this.body.getChild("right_arm");
		this.head = this.body.getChild("head");
		this.leftLeg = this.root.getChild("left_leg");
		this.rightLeg = this.root.getChild("right_leg");
	}
	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData root = modelPartData.addChild("root", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		ModelPartData body = root.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-4.5F, -11.0F, -4.0F, 9.0F, 11.0F, 8.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -5.0F, 0.0F));

		ModelPartData left_arm = body.addChild("left_arm", ModelPartBuilder.create().uv(28, 19).cuboid(-1.0F, -1.5F, -2.0F, 3.0F, 13.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(5.5F, -9.5F, 0.0F));

		ModelPartData right_arm = body.addChild("right_arm", ModelPartBuilder.create().uv(28, 19).mirrored().cuboid(-2.0F, -1.5F, -2.0F, 3.0F, 13.0F, 4.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.pivot(-5.5F, -9.5F, 0.0F));

		ModelPartData head = body.addChild("head", ModelPartBuilder.create().uv(0, 19).cuboid(-3.5F, -5.0F, -3.5F, 7.0F, 5.0F, 7.0F, new Dilation(0.0F))
				.uv(16, 31).cuboid(0.5F, -9.0F, -1.5F, 3.0F, 4.0F, 3.0F, new Dilation(0.0F))
				.uv(16, 31).mirrored().cuboid(-3.5F, -9.0F, -1.5F, 3.0F, 4.0F, 3.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.pivot(0.0F, -11.0F, 0.0F));

		ModelPartData left_leg = root.addChild("left_leg", ModelPartBuilder.create().uv(0, 31).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(2.5F, -5.0F, 0.0F));

		ModelPartData right_leg = root.addChild("right_leg", ModelPartBuilder.create().uv(0, 31).mirrored().cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.pivot(-2.5F, -5.0F, 0.0F));
		return TexturedModelData.of(modelData, 64, 64);
	}
	@Override
	public void setAngles(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.getPart().traverse().forEach(ModelPart::resetTransform);
		this.setHeadAngle(netHeadYaw, headPitch);
		//this.setLimbAngles(limbSwing, limbSwingAmount);
        if (entity instanceof ImpEntity imp) {
            this.animateArms(imp);
            if (!imp.getPassengerList().isEmpty())
                this.animate(ImpAnimation.HOLDING);
            else if (imp.getTarget() != null || imp.getAngryAt() != null || imp.getAngerTime() > 0 || imp.hasAngerTime())
                this.animate(ImpAnimation.ANGRY);

            this.animateMovement(ImpAnimation.WALK,limbSwing,limbSwingAmount,1,1);

            this.updateAnimation(imp.spitAnimationState,ImpAnimation.SPIT,ageInTicks);
        }

    }
	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, int color) {
		root.render(matrices, vertexConsumer, light, overlay, color);
	}

	@Override
	public ModelPart getPart() {
		return root;
	}

	private void setHeadAngle(float yaw, float pitch) {
		this.head.pitch = pitch * ((float)Math.PI / 180F);
		this.head.yaw = yaw * ((float)Math.PI / 180F);
	}

	private void setLimbAngles(float angle, float distance) {
		float f = Math.min(0.5F, 3.0F * distance);
		float g = angle * 0.8662F;
		float h = MathHelper.cos(g);
		float i = MathHelper.sin(g);
		float j = Math.min(0.35F, f);
		this.head.roll += 0.3F * i * f;
		this.head.pitch += 1.2F * MathHelper.cos(g + ((float)Math.PI / 2F)) * j;
		this.body.roll = 0.1F * i * f;
		this.body.pitch = 1.0F * h * j;
		this.leftLeg.pitch = 1.0F * h * f;
		this.rightLeg.pitch = 1.0F * MathHelper.cos(g + (float)Math.PI) * f;
		this.leftArm.pitch = -(0.8F * h * f);
		this.leftArm.roll = 0.0F;
		this.rightArm.pitch = -(0.8F * i * f);
		this.rightArm.roll = 0.0F;
	}

	protected void animateArms(ImpEntity entity) {
		if (!(this.handSwingProgress <= 0.0F)) {
			Arm arm = this.getPreferredArm(entity);
			ModelPart modelPart = this.getArm(arm);
			float f = this.handSwingProgress;
			this.body.yaw = MathHelper.sin(MathHelper.sqrt(f) * (float) (Math.PI * 2)) * 0.15F;
			if (arm == Arm.LEFT) {
				this.body.yaw *= -1.0F;
			}
			this.rightArm.pivotZ = MathHelper.sin(this.body.yaw) * 3.0F;
			this.rightArm.pivotX = -MathHelper.cos(this.body.yaw) * 3.0F;
			this.leftArm.pivotZ = -MathHelper.sin(this.body.yaw) * 3.0F;
			this.leftArm.pivotX = MathHelper.cos(this.body.yaw) * 3.0F;
			this.rightArm.yaw = this.rightArm.yaw + this.body.yaw;
			this.leftArm.yaw = this.leftArm.yaw + this.body.yaw;
			this.leftArm.pitch = this.leftArm.pitch + this.body.yaw;
			f = 1.0F - this.handSwingProgress;
			f *= f;
			f *= f;
			f = 1.0F - f;
			float g = MathHelper.sin(f * (float) Math.PI);
			float h = MathHelper.sin(this.handSwingProgress * (float) Math.PI) * -(this.head.pitch - 0.7F) * 0.75F;
			modelPart.pitch -= g * 1.2F + h;
			modelPart.yaw = modelPart.yaw + this.body.yaw * 2.0F;
			modelPart.roll = modelPart.roll + MathHelper.sin(this.handSwingProgress * (float) Math.PI) * -0.4F;
		}
	}

	protected ModelPart getArm(Arm arm) {
		return arm == Arm.LEFT ? this.leftArm : this.rightArm;
	}

	private Arm getPreferredArm(ImpEntity entity) {
		Arm arm = entity.getMainArm();
		return entity.preferredHand == Hand.MAIN_HAND ? arm : arm.getOpposite();
	}
}