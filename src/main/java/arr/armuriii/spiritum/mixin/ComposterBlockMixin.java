package arr.armuriii.spiritum.mixin;

import arr.armuriii.spiritum.Spiritum;
import arr.armuriii.spiritum.init.SpiritumItems;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ComposterBlock;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ComposterBlock.class)
public class ComposterBlockMixin {
    @Unique
    private static final BooleanProperty FLESH = Spiritum.FLESH;

    @ModifyReceiver(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;with(Lnet/minecraft/state/property/Property;Ljava/lang/Comparable;)Ljava/lang/Object;"))
    private BlockState spiritum$addProperty(BlockState instance, Property<?> property, Comparable<?> comparable) {
        return instance.with(FLESH,false);
    }

    @Unique
    private static final Object2FloatMap<Ingredient> FLESH_TO_LEVEL_INCREASE_CHANCE = new Object2FloatOpenHashMap<>();

    @Inject(method = "registerDefaultCompostableItems", at = @At("HEAD"))
    private static void spiritum$registerFlesh(CallbackInfo ci) {
        FLESH_TO_LEVEL_INCREASE_CHANCE.defaultReturnValue(-1.0f);
        registerFleshItem(0.75f, Ingredient.fromTag(ItemTags.MEAT));
        registerFleshItem(0.5f, Ingredient.ofItems(Items.ROTTEN_FLESH));
    }

    @ModifyExpressionValue(method = {"onUseWithItem","compost"}, at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/Object2FloatMap;containsKey(Ljava/lang/Object;)Z"))
    private static boolean spiritum$acceptsFlesh(boolean original, @Local(argsOnly = true) ItemStack stack, @Local(argsOnly = true) BlockState state, @Local int level) {
        if (contains(stack) && (level == 0 || state.get(FLESH))) {
            state.with(FLESH,true);
            return true;
        }
        return (original && !state.get(FLESH));
    }

    @ModifyExpressionValue(method = {"addToComposter"}, at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/Object2FloatMap;getFloat(Ljava/lang/Object;)F"))
    private static float spiritum$addToComposter(float original, @Local(argsOnly = true) ItemStack stack, @Local(argsOnly = true) BlockState state, @Local int level) {
        return original < 0f ? get(stack) : original;
    }

    @ModifyReceiver(method = {"addToComposter"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;with(Lnet/minecraft/state/property/Property;Ljava/lang/Comparable;)Ljava/lang/Object;"))
    private static BlockState spiritum$addFleshState(BlockState instance, Property<?> property, Comparable<?> comparable, @Local(argsOnly = true) ItemStack stack, @Local(argsOnly = true) BlockState state) {
        return contains(stack) ? instance.with(FLESH,true) : instance;
    }

    @ModifyArg(method = {"emptyFullComposter"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;<init>(Lnet/minecraft/item/ItemConvertible;)V"))
    private static ItemConvertible spiritum$addToComposter(ItemConvertible original, @Local(argsOnly = true) BlockState state) {
        boolean flesh = state.get(FLESH);
        return flesh ? SpiritumItems.FLESH_CLUMP : original;
    }

    @ModifyReceiver(method = {"emptyComposter"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;with(Lnet/minecraft/state/property/Property;Ljava/lang/Comparable;)Ljava/lang/Object;"))
    private static BlockState spiritum$resetFleshProperty(BlockState instance, Property<?> property, Comparable<?> comparable) {
        return instance.with(FLESH,false);
    }

    @Inject(method = "appendProperties", at = @At("TAIL"))
    private void spiritum$addProperties(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(Spiritum.FLESH);
    }

    @Unique
    private static boolean contains(ItemStack stack) {
        return get(stack) != -1f;
    }

    @Unique
    private static float get(ItemStack stack) {
        for (Object2FloatMap.Entry<Ingredient> entry : FLESH_TO_LEVEL_INCREASE_CHANCE.object2FloatEntrySet()) {
            if (entry.getKey().test(stack))
                return entry.getFloatValue();
        }
        return -1f;
    }

    @Unique
    private static void registerFleshItem(float levelIncreaseChance, Ingredient item) {
        FLESH_TO_LEVEL_INCREASE_CHANCE.put(item, levelIncreaseChance);
    }
}
