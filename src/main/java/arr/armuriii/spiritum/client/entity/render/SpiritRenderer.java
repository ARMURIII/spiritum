package arr.armuriii.spiritum.client.entity.render;

import arr.armuriii.spiritum.Spiritum;
import arr.armuriii.spiritum.client.SpiritumClient;
import arr.armuriii.spiritum.client.entity.model.spirit.SpiritModel;
import arr.armuriii.spiritum.entity.SpiritEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class SpiritRenderer extends MobEntityRenderer<SpiritEntity, SpiritModel> {
    private static final Identifier TEXTURE = Spiritum.id("textures/entity/spirit.png");

    public SpiritRenderer(EntityRendererFactory.Context context) {
        super(context, new SpiritModel(context.getPart(SpiritumClient.SPIRIT_LAYER)), 0F);
    }

    @Override
    public Identifier getTexture(SpiritEntity entity) {
        return TEXTURE;
    }

    @Override
    protected int getBlockLight(SpiritEntity entity, BlockPos pos) {
        return 15;
    }
}
