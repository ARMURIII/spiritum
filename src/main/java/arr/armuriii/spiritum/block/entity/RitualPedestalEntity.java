package arr.armuriii.spiritum.block.entity;

import arr.armuriii.spiritum.Spiritum;
import arr.armuriii.spiritum.init.SpiritumBlocks;
import arr.armuriii.spiritum.init.SpiritumRituals;
import arr.armuriii.spiritum.rituals.Ritual;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Clearable;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.*;
import java.util.function.Consumer;

public class RitualPedestalEntity  extends BlockEntity implements Clearable {

    private List<ItemStack> items = new ArrayList<>(List.of());
    private short soul = 0;
    private long time = -1;
    private boolean hasValidRitual = false;

    private boolean active = false;

    private Ritual ritual = SpiritumRituals.EMPTY;

    private UUID owner = null;

    public RitualPedestalEntity(BlockPos pos, BlockState state) {
        super(SpiritumBlocks.RITUAL_PEDESTAL_BLOCK_ENTITY, pos, state);
        clear();
    }

    @Override
    public void clear() {
        items.clear();
        soul = 0;
        time = -1;
        hasValidRitual = false;
        ritual = SpiritumRituals.EMPTY;
        active = false;
        owner = null;
    }

    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.readNbt(nbt, lookup);
        clear();
        items.clear();

        if (nbt.contains("Items",NbtCompound.LIST_TYPE)) {
            NbtList list = (NbtList) nbt.get("Items");
            if (list != null)
                for (int i = 0; i < list.size(); i++) {
                    items.add(ItemStack.fromNbtOrEmpty(lookup,list.getCompound(i)));
                }
        }

        soul = nbt.getShort("Soul");
        time = nbt.getLong("Time");
        if (nbt.contains("Ritual", NbtElement.STRING_TYPE))
            Spiritum.RITUAL.getOrEmpty(Identifier.of(nbt.getString("Ritual")));
        active = nbt.getBoolean("Active");
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.writeNbt(nbt, lookup);
        NbtList list = new NbtList();
        for (ItemStack item : items) {
            if (!item.isEmpty())
                list.add(item.encode(lookup));
        }
        nbt.put("Items",list);
        nbt.putInt("Soul",soul);
        nbt.putLong("Time",time);
        if (getRitual() != SpiritumRituals.EMPTY)
            nbt.putString("Ritual",Spiritum.RITUAL.getEntry(ritual).getIdAsString());
        nbt.putBoolean("Active",active);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound nbt = new NbtCompound();
        NbtList list = new NbtList();
        for (ItemStack item : items) {
            if (!item.isEmpty())
                list.add(item.encode(registryLookup));
        }
        nbt.put("Items",list);
        nbt.putInt("Soul",soul);
        return nbt;
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, RitualPedestalEntity pedestal) {
        tick(world, pos, state, pedestal);
        if (pedestal.getRitual() != SpiritumRituals.EMPTY && pedestal.isActive()) {
            if (pedestal.getRitual().getType() == Ritual.Type.LASTING) {
                pedestal.getRitual().clientTick(pedestal,pedestal.getOwner());
            }
        }
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, RitualPedestalEntity pedestal) {
        tick(world, pos, state, pedestal);
        if (pedestal.getRitual() != SpiritumRituals.EMPTY && pedestal.isActive() && !pedestal.getRitual().instant(pedestal,pedestal.getOwner())) {
            if (pedestal.getRitual().getType() == Ritual.Type.LASTING)
                pedestal.getRitual().serverTick(pedestal,pedestal.getOwner());
        }else if ((pedestal.isActive() && (pedestal.getRitual() != SpiritumRituals.EMPTY || pedestal.getRitual().getType() == Ritual.Type.INSTANT))) {
            for (PlayerEntity player : world.getPlayers()) {
                player.sendMessage(Text.literal("CLEARED: " + pedestal.getRitual().getType().name()));
            }
            pedestal.setActive(false);
            pedestal.removeRitual();
        }
    }
    public static void tick(World world, BlockPos pos, BlockState state, RitualPedestalEntity pedestal) {
        pedestal.updateRitualValidity();
    }

    public void updateRitualValidity() {
        for (Map.Entry<RegistryKey<Ritual>, Ritual> entry : Spiritum.RITUAL.getEntrySet()) {
            if (ingredientMatchList(entry.getValue().getComponent(),this.getItems()) && getSoul() >= entry.getValue().getSpiritAmount()) {
                setRitualValidity(true);
                return;
            }
        }
        setOwner(null);
        setTime(-1);
        setRitualValidity(false);
    }

    public Optional<Ritual> getValidRitual() {
        if (hasValidRitual()) {
            for (Map.Entry<RegistryKey<Ritual>, Ritual> entry : Spiritum.RITUAL.getEntrySet()) {
                if (ingredientMatchList(entry.getValue().getComponent(),this.getItems()) && getSoul() >= entry.getValue().getSpiritAmount()) {
                    return Optional.of(entry.getValue());
                }
            }
        }
        return Optional.empty();
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public boolean addStack(ItemStack stack) {
        updateListeners();
        return this.items.add(stack);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (this.world != null)
            this.world.emitGameEvent(GameEvent.BLOCK_CHANGE, this.getPos(), GameEvent.Emitter.of(this.getCachedState()));
    }

    public List<ItemStack> setItems(List<ItemStack> list) {
        updateListeners();
        this.items = list;
        return this.getItems();
    }

    public void removeStack(ItemStack stack) {
        updateListeners();
        items.remove(stack);
    }

    public void removeStack(int i) {
        updateListeners();
        items.remove(i);
    }

    public ItemStack getLast() {
        updateListeners();
        return items.getLast();
    }

    public ItemStack setLast(ItemStack stack) {
        updateListeners();
        return items.set(items.size()-1,stack);
    }

    public ItemStack removeLast() {
        updateListeners();
        return items.removeLast();
    }

    public boolean removeComponents(List<ItemStack> stacks) {
        List<ItemStack> mutable = new ArrayList<>(stacks);
        updateListeners();
        return items.removeIf(stack -> {
            if (mutable.contains(stack)) {
                mutable.remove(stack);
                return true;
            }
            return false;
        });
    }

    public void removeComponentsItem(List<Item> items1) {
        List<Item> mutable = new ArrayList<>(items1);
        updateListeners();
        items.removeIf(stack -> {
            if (mutable.contains(stack.getItem())) {
                mutable.remove(stack.getItem());
                return true;
            }
            return false;
        });
    }

    public void replaceItem(Item item, ItemStack replacer) {
        updateListeners();
        for (ItemStack stack : items) {
            if (stack.isOf(item)) {
                items.set(items.indexOf(stack), replacer);
                break;
            }
        }
    }

    public ItemStack getStackFromItem(Item item) {
        for (ItemStack stack : items) {
            if (stack.isOf(item))
                return stack;
        }
        return null;
    }

    public void consumerItem(Consumer<ItemStack> consumer) {
        updateListeners();
        items.forEach(consumer);
    }

    public boolean ingredientMatchList(Ingredient ingredient, List<ItemStack> list) {
        if (ingredient.getMatchingStacks().length <= list.size() && ingredient != Ingredient.EMPTY) {
            for (ItemStack stack : ingredient.getMatchingStacks()) {
                if (!list.stream().map(ItemStack::getItem).toList().contains(stack.getItem()))
                    return false;
            }
            return true;
        }
        return false;
    }

    public List<ItemStack> getIngredientMatching(Ingredient ingredient, List<ItemStack> list) {
        if (ingredient.getMatchingStacks().length <= list.size()) {
            List<ItemStack> stacks = new java.util.ArrayList<>(List.of());
            for (ItemStack stack : ingredient.getMatchingStacks()) {
                if (list.stream().map(ItemStack::getItem).toList().contains(stack.getItem()))
                    stacks.add(stack);
            }
            return stacks;
        }
        return null;
    }

    public List<Item> getItemMatching(Ingredient ingredient, List<Item> list) {
        if (ingredient.getMatchingStacks().length <= list.size()) {
            List<Item> items1 = new java.util.ArrayList<>(List.of());
            for (Item item : Arrays.stream(ingredient.getMatchingStacks()).map(ItemStack::getItem).toList()) {
                if (list.contains(item))
                    items1.add(item);
            }
            return items1;
        }
        return null;
    }

    public int getSoul() {
        return soul;
    }

    public void setSoul(int soul) {
        updateListeners();
        this.soul = (short)soul;
    }

    public void addSoul(int soul) {
        updateListeners();
        this.soul += (short) soul;
    }

    public void removeSoul(int soul) {
        updateListeners();
        this.soul -= (short) soul;
    }

    public boolean hasValidRitual() {
        return hasValidRitual;
    }

    public void setRitualValidity(boolean hasValidRitual) {
        updateListeners();
        this.hasValidRitual = hasValidRitual;
    }

    public Ritual getRitual() {
        return ritual;
    }

    public void setRitual(Ritual ritual) {
        updateListeners();
        this.setActive(true);
        this.ritual = ritual;
    }

    public void removeRitual() {
        updateListeners();
        this.ritual.onRemove(this,getOwner());
        this.ritual = SpiritumRituals.EMPTY;
        this.setActive(false);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        updateListeners();
        this.owner = owner;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        updateListeners();
        this.time = time;
    }

    public void updateListeners() {
        this.markDirty();
        if (this.getWorld() != null)
            this.getWorld().updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL);
    }
}
