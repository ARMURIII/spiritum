package arr.armuriii.spiritum.rituals;

import arr.armuriii.spiritum.block.entity.RitualPedestalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.world.event.GameEvent;

import java.util.Objects;
import java.util.UUID;

public class Ritual {
    private final arr.armuriii.spiritum.rituals.Ritual.Type type;
    private final Ingredient component;
    private final int spiritAmount;
    public Ritual(Type type, Ingredient component, int spiritAmount) {
        this.type = Objects.requireNonNull(type);
        this.component = Objects.requireNonNull(component);
        this.spiritAmount = spiritAmount;
    }

    public boolean onApply(RitualPedestalEntity pedestal, UUID owner) {
        pedestal.removeComponentsItem(pedestal.getItemMatching(component,pedestal.getItems().stream().map(ItemStack::getItem).toList()));
        pedestal.removeSoul(spiritAmount);
        pedestal.updateListeners();
        return true;
    }

    public boolean onRemove(RitualPedestalEntity pedestal, UUID owner) {
        return true;
    }

    public boolean instant(RitualPedestalEntity pedestal, UUID owner) {
        return type == Type.INSTANT;
    }

    public boolean shouldLoop(RitualPedestalEntity pedestal, UUID owner) {
        return type == Type.LASTING;
    }

    public void serverTick(RitualPedestalEntity pedestal, UUID owner) {

    }

    public void clientTick(RitualPedestalEntity pedestal, UUID owner) {

    }

    public Ingredient getComponent() {
        return component;
    }

    public int getSpiritAmount() {
        return spiritAmount;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        INSTANT(),
        LASTING();
    }
}
