package arr.armuriii.spiritum.mixin.accessor;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.SleepManager;
import net.minecraft.world.PlayerSaveHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftServer.class)
public interface MinecraftServerAccessor {

    @Accessor("saveHandler")
    PlayerSaveHandler getSaveHandler();
}
