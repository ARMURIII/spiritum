package arr.armuriii.spiritum.client.entity.render.feature;

import arr.armuriii.spiritum.Spiritum;
import arr.armuriii.spiritum.client.entity.model.imp.ImpModel;
import arr.armuriii.spiritum.entity.ImpEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.feature.EyesFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;

public class ImpEmissiveFeatureRenderer <T extends ImpEntity> extends EyesFeatureRenderer<T, ImpModel<T>> {
    private static final RenderLayer SKIN = RenderLayer.getEyes(Spiritum.id("textures/entity/imp_emissive.png"));

    public ImpEmissiveFeatureRenderer(FeatureRendererContext<T, ImpModel<T>> featureRendererContext) {
        super(featureRendererContext);
    }

    @Override
    public RenderLayer getEyesTexture() {
        return SKIN;
    }
}

