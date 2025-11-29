package arr.armuriii.spiritum.init;

import arr.armuriii.spiritum.Spiritum;
import arr.armuriii.spiritum.recipe.TippedNeedleShapedRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class SpiritumRecipes {

    public static final RecipeSerializer<TippedNeedleShapedRecipe> TIPPED_NEEDLE = register("tipping_needle",
            new SpecialRecipeSerializer<>(TippedNeedleShapedRecipe::new));

    public static void register() {
        Spiritum.LOGGER.info("registered {} Recipes",Spiritum.MOD_ID);
    }

    static <S extends RecipeSerializer<T>, T extends Recipe<?>> S register(String id, S serializer) {
        return Registry.register(Registries.RECIPE_SERIALIZER, Spiritum.id(id), serializer);
    }

}
