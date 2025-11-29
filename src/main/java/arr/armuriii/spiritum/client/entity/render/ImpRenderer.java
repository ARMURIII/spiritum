package arr.armuriii.spiritum.client.entity.render;

import arr.armuriii.spiritum.Spiritum;
import arr.armuriii.spiritum.client.SpiritumClient;
import arr.armuriii.spiritum.client.entity.model.imp.ImpModel;
import arr.armuriii.spiritum.client.entity.render.feature.ImpEmissiveFeatureRenderer;
import arr.armuriii.spiritum.entity.ImpEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class ImpRenderer extends MobEntityRenderer<ImpEntity, ImpModel<ImpEntity>> {
    private static final Identifier TEXTURE = Spiritum.id("textures/entity/imp.png");

    public ImpRenderer(EntityRendererFactory.Context context) {
        super(context, new ImpModel<>(context.getPart(SpiritumClient.IMP_LAYER)), 1F);
        this.addFeature(new ImpEmissiveFeatureRenderer<>(this));
    }

    @Override
    public Identifier getTexture(ImpEntity entity) {
        return TEXTURE;
    }
}