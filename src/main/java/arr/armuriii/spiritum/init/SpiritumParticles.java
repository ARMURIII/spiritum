package arr.armuriii.spiritum.init;

import arr.armuriii.spiritum.Spiritum;
import arr.armuriii.spiritum.recipe.TippedNeedleShapedRecipe;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class SpiritumParticles {

    public static final SimpleParticleType IMP_TYPE = register("imp",
            FabricParticleTypes.simple(true));

    public static final SimpleParticleType HEXFLAME_TYPE = register("hexflame",
            FabricParticleTypes.simple(true));

    public static void register() {
        Spiritum.LOGGER.info("registered {} Particles",Spiritum.MOD_ID);
    }

    static <S extends net.minecraft.particle.ParticleType<T>, T extends ParticleEffect> S register(String id, S type) {
        return Registry.register(Registries.PARTICLE_TYPE, Spiritum.id(id), type);
    }

}
