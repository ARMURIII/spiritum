package arr.armuriii.spiritum.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AbstractFurnaceBlockEntity.class)
public class AbstractFurnaceBlockEntityMixin {
    @ModifyConstant(method = "craftRecipe", constant = @Constant(intValue = 1,ordinal = 0))
    private static int spiritum$countToRecipe(int constant, DynamicRegistryManager registryManager, RecipeEntry<?> recipe, DefaultedList<ItemStack> slots, int count) {
        return recipe.value().getResult(registryManager).getCount();
    }

    @ModifyExpressionValue(method = "canAcceptRecipeOutput", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getCount()I"))
    private static int spiritum$doNotAcceptOverflow(int original,DynamicRegistryManager registryManager, RecipeEntry<?> recipe) {
        return original+recipe.value().getResult(registryManager).getCount()-1;
    }
}
