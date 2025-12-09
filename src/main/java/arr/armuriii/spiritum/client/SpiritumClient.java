package arr.armuriii.spiritum.client;

import arr.armuriii.spiritum.Spiritum;
import arr.armuriii.spiritum.client.block.RitualPedestalRenderer;
import arr.armuriii.spiritum.client.entity.model.imp.ImpModel;
import arr.armuriii.spiritum.client.entity.model.spirit.SpiritModel;
import arr.armuriii.spiritum.client.entity.render.ImpRenderer;
import arr.armuriii.spiritum.client.entity.render.SpiritRenderer;
import arr.armuriii.spiritum.client.entity.render.SpitRenderer;
import arr.armuriii.spiritum.client.entity.render.feature.HatFeatureRenderer;
import arr.armuriii.spiritum.init.SpiritumBlocks;
import arr.armuriii.spiritum.init.SpiritumItems;
import arr.armuriii.spiritum.init.SpiritumParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.particle.*;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.particle.SimpleParticleType;

import static arr.armuriii.spiritum.init.SpiritumEntities.*;

public class SpiritumClient implements ClientModInitializer {

    public static final EntityModelLayer IMP_LAYER = new EntityModelLayer(Spiritum.id("imp"),"main");
    public static final EntityModelLayer SPIRIT_LAYER = new EntityModelLayer(Spiritum.id("spirit"),"main");

    @Override
    public void onInitializeClient() {
        ColorProviderRegistry.ITEM.register((stack,tintIndex) ->
                tintIndex != 1 ? -1 :  stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).getColor()
        ,SpiritumItems.TIPPED_SILVER_NEEDLE);

        EntityModelLayerRegistry.registerModelLayer(IMP_LAYER, ImpModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(SPIRIT_LAYER, SpiritModel::getTexturedModelData);

        EntityRendererRegistry.register(IMP, ImpRenderer::new);
        EntityRendererRegistry.register(SPIRIT, SpiritRenderer::new);
        EntityRendererRegistry.register(SPIT, SpitRenderer::new);

        BlockEntityRendererFactories.register(SpiritumBlocks.RITUAL_PEDESTAL_BLOCK_ENTITY, RitualPedestalRenderer::new);

        ParticleFactoryRegistry.getInstance()
                .register(SpiritumParticles.IMP_TYPE, QuickFactory::new);

        ParticleFactoryRegistry.getInstance()
                .register(SpiritumParticles.HEXFLAME_TYPE, FlameParticle.Factory::new);

        LivingEntityFeatureRendererRegistrationCallback.EVENT.register(
                (type,renderer,helper,ctx)-> {
                    helper.register(new HatFeatureRenderer<>(renderer, ctx.getHeldItemRenderer()));
                });
    }

    @Environment(EnvType.CLIENT)
    public static class QuickFactory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public QuickFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(SimpleParticleType simpleParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
            SpellParticle particle = new SpellParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
            particle.setMaxAge(10);
            return particle;
        }
    }
}
