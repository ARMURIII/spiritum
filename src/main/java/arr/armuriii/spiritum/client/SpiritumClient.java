package arr.armuriii.spiritum.client;

import arr.armuriii.spiritum.Spiritum;
import arr.armuriii.spiritum.client.block.RitualPedestalRenderer;
import arr.armuriii.spiritum.client.entity.model.imp.ImpModel;
import arr.armuriii.spiritum.client.entity.render.ImpRenderer;
import arr.armuriii.spiritum.command.ARRegistryEntryArgumentType;
import arr.armuriii.spiritum.init.SpiritumBlocks;
import arr.armuriii.spiritum.init.SpiritumItems;
import arr.armuriii.spiritum.rituals.Ritual;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.math.MathHelper;

import java.awt.*;

import static arr.armuriii.spiritum.init.SpiritumEntities.IMP;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class SpiritumClient implements ClientModInitializer {

    public static final EntityModelLayer IMP_LAYER = new EntityModelLayer(Spiritum.id("imp"),"main");

    @Override
    public void onInitializeClient() {
        ColorProviderRegistry.ITEM.register((stack,tintIndex) ->
                tintIndex != 1 ? -1 :  stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).getColor()
        ,SpiritumItems.TIPPED_SILVER_NEEDLE);

        /*ColorProviderRegistry.ITEM.register((stack,tintIndex) ->
                tintIndex != 1 ? -1 : interpolateColor(stack.getOrDefault(DataComponentTypes.DAMAGE, 56)/56.,new Color(0xDDFF63),new Color(0xFF6363))
        ,SpiritumItems.SUMMONING_TOKEN);*/

        EntityModelLayerRegistry.registerModelLayer(IMP_LAYER, ImpModel::getTexturedModelData);

        EntityRendererRegistry.register(IMP, ImpRenderer::new);

        BlockEntityRendererFactories.register(SpiritumBlocks.RITUAL_PEDESTAL_BLOCK_ENTITY, RitualPedestalRenderer::new);

        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, access) -> {
            dispatcher.register(literal("ritual")
                    .then(argument("ritual", ARRegistryEntryArgumentType.registryEntry(access,Spiritum.RITUAL_KEY))
                            .executes(context -> {
                                RegistryEntry<Ritual> entry = ARRegistryEntryArgumentType.getFabricRegistryEntry(context,"ritual",Spiritum.RITUAL_KEY);
                                context.getSource().sendFeedback(Text.translatable("commands.ritual.success",entry.getIdAsString(),entry.value().getComponent().getMatchingStacks(),entry.value().getSpiritAmount()));
                                return 0;
                            })
                    ));
        }));
    }

    private static int interpolateColor(double i, Color color1, Color color2) {
        return safeColor((int)(color1.getRed()*i+color2.getRed()*i),(int)(color1.getGreen()*i+color2.getGreen()*i),(int)(color1.getBlue()*i+color2.getBlue()*i)).getRGB();
    }

    private static Color safeColor(int r,int g,int b) {
        return new Color(MathHelper.clamp(r,0,255),MathHelper.clamp(g,0,255),MathHelper.clamp(b,0,255));
    }
}
