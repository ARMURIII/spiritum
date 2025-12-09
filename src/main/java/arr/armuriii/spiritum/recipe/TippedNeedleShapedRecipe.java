package arr.armuriii.spiritum.recipe;

import arr.armuriii.spiritum.init.SpiritumItems;
import arr.armuriii.spiritum.init.SpiritumRecipes;
import arr.armuriii.spiritum.utils.ImmutableTwin;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TippedNeedleShapedRecipe extends SpecialCraftingRecipe {
    public TippedNeedleShapedRecipe(CraftingRecipeCategory craftingRecipeCategory) {
        super(craftingRecipeCategory);
    }

    private static final List<Item> PATTERN = List.of(
            Items.AIR,SpiritumItems.SILVER_NEEDLE,Items.AIR,
            SpiritumItems.SILVER_NEEDLE,Items.POTION,SpiritumItems.SILVER_NEEDLE,
            Items.AIR,SpiritumItems.SILVER_NEEDLE,Items.AIR
    );

    public boolean matches(CraftingRecipeInput input, World world) {
        if (input.getWidth() >= 3 && input.getHeight() >= 3) {
            for (Map.Entry<ImmutableTwin<Integer>,ItemStack> entry : getStacks(input).entrySet()) {
                    if (!patternMatches(entry.getValue(),(entry.getKey().getLeft()*3)+entry.getKey().getRight()))
                        return false;
            }
            return true;
        }
        return false;
    }

    private static Map<ImmutableTwin<Integer>,ItemStack> getStacks(CraftingRecipeInput input) {
        Map<ImmutableTwin<Integer>,ItemStack> map = new HashMap<>();
        for (int height = 0; height < input.getHeight(); height++)
            for (int width = 0; width < input.getWidth(); width++)
                map.putIfAbsent(new ImmutableTwin<>(width,height),input.getStackInSlot(width, height));
        return map;
    }

    private boolean patternMatches(ItemStack stack, int index) {
        if (PATTERN.get(index) == Items.AIR && stack.isEmpty()) return true;
        return stack.isOf(PATTERN.get(index));
    }

    public ItemStack craft(CraftingRecipeInput craftingRecipeInput, RegistryWrapper.WrapperLookup wrapperLookup) {
        ItemStack potion = craftingRecipeInput.getStackInSlot(1, 1);
        if (!potion.isOf(Items.POTION))
            return ItemStack.EMPTY;

        ItemStack needle = new ItemStack(SpiritumItems.TIPPED_SILVER_NEEDLE, 4);
        needle.set(DataComponentTypes.POTION_CONTENTS, potion.get(DataComponentTypes.POTION_CONTENTS));
        return needle;
    }

    @Override
    public boolean fits(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SpiritumRecipes.TIPPED_NEEDLE;
    }
}
