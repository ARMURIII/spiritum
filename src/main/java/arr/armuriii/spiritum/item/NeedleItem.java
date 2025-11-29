package arr.armuriii.spiritum.item;

import arr.armuriii.spiritum.init.SpiritumDamageTypes;
import arr.armuriii.spiritum.init.SpiritumItems;
import arr.armuriii.spiritum.mixin.accessor.MinecraftServerAccessor;
import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.PlayerSaveHandler;
import net.minecraft.world.World;

import java.util.List;
import java.util.Objects;

public class NeedleItem extends Item {
    public NeedleItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = getPoppet(user.getStackInHand(hand),user.getStackInHand(hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND));
        ItemStack needle = getNeedle(user.getStackInHand(hand),user.getStackInHand(hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND));
        if (stack.isOf(SpiritumItems.POPPET) && !user.getItemCooldownManager().isCoolingDown(SpiritumItems.POPPET) && !world.isClient() && world.getServer() != null && world instanceof ServerWorld serverWorld) {
            String target = stack.getOrDefault(DataComponentTypes.CUSTOM_NAME, Text.empty()).getString();
            if (serverWorld.getServer().getPlayerManager().getPlayer(target) != null) {
                pokePlayer(serverWorld.getServer().getPlayerManager().getPlayer(target),user);
            }else {
                PlayerSaveHandler saveHandler = ((MinecraftServerAccessor) serverWorld.getServer()).getSaveHandler();
                if (serverWorld.getServer().getUserCache() != null) {
                    GameProfile targetProfile = serverWorld.getServer().getUserCache().findByName(target).orElse(null);
                    if (targetProfile != null) {
                        ServerPlayerEntity player = FakePlayer.get(serverWorld, targetProfile);
                        saveHandler.loadPlayerData(player);
                        pokePlayer(player,user);
                        saveHandler.savePlayerData(player);
                    }
                }
            }
            user.getItemCooldownManager().set(SpiritumItems.POPPET,200);
            needle.decrement(1);
            return TypedActionResult.success(user.getStackInHand(hand));
        }
        return super.use(world, user, hand);
    }

    protected ItemStack getPoppet(ItemStack main,ItemStack off) {
        if (main.isOf(SpiritumItems.POPPET)) return main;
        if (off.isOf(SpiritumItems.POPPET)) return off;
        return ItemStack.EMPTY;
    }

    protected ItemStack getNeedle(ItemStack main,ItemStack off) {
        if (main.isOf(SpiritumItems.SILVER_NEEDLE)||main.isOf(SpiritumItems.TIPPED_SILVER_NEEDLE)) return main;
        if (off.isOf(SpiritumItems.SILVER_NEEDLE)||off.isOf(SpiritumItems.TIPPED_SILVER_NEEDLE)) return off;
        return ItemStack.EMPTY;
    }

    protected void pokePlayer(PlayerEntity player, PlayerEntity attacker) {
        if (player != null) {
            player.damage(SpiritumDamageTypes.of(SpiritumDamageTypes.HEXING,attacker),2);
        }
    }
}
