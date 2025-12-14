package arr.armuriii.spiritum.init;

import arr.armuriii.spiritum.Spiritum;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureSet;

public class SpiritumPotions {

    public static final RegistryEntry.Reference<StatusEffect> LETHARGY = register(new StatusEffect(StatusEffectCategory.HARMFUL,14548835)
            .addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED,Spiritum.id("effect.lethargy"),-0.2, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
            ,"lethargy");

    public static final RegistryEntry.Reference<Potion> INERTIA = register(new Potion(new StatusEffectInstance(LETHARGY,2*60*20)),"lethargy");

    public static void register() {
        Spiritum.LOGGER.info("registered {} Potions",Spiritum.MOD_ID);
        new BrewingRecipeRegistry.Builder(FeatureSet.empty()).registerPotionRecipe(Potions.WEAKNESS,SpiritumItems.FLESH_CLUMP,INERTIA);

        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> builder.registerPotionRecipe(
                    Potions.WEAKNESS,SpiritumItems.FLESH_CLUMP,INERTIA
            ));
    }

    @SuppressWarnings("SameParameterValue")
    private static RegistryEntry.Reference<StatusEffect> register(StatusEffect effect, String id) {
        return Registry.registerReference(Registries.STATUS_EFFECT, Spiritum.id(id), effect);
    }

    @SuppressWarnings("SameParameterValue")
    private static RegistryEntry.Reference<Potion> register(Potion potion, String id) {
        return Registry.registerReference(Registries.POTION, Spiritum.id(id),potion);
    }
}
