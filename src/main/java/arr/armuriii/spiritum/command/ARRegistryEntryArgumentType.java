package arr.armuriii.spiritum.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;

public class ARRegistryEntryArgumentType<T> extends RegistryEntryReferenceArgumentType<T> {
    public ARRegistryEntryArgumentType(CommandRegistryAccess registryAccess, RegistryKey<? extends Registry<T>> registryRef) {
        super(registryAccess, registryRef);
    }

    public static <T> RegistryEntry.Reference<T> getFabricRegistryEntry(CommandContext<FabricClientCommandSource> context, String name, RegistryKey<Registry<T>> registryRef) throws CommandSyntaxException {
        RegistryEntry.Reference<T> reference = context.getArgument(name, RegistryEntry.Reference.class);
        RegistryKey<?> registryKey = reference.registryKey();
        if (registryKey.isOf(registryRef)) {
            return reference;
        } else {
            throw INVALID_TYPE_EXCEPTION.create(registryKey.getValue(), registryKey.getRegistry(), registryRef.getValue());
        }
    }
}
