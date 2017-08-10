package org.spongepowered.mod.item.inventory.fabric;

import com.google.common.collect.ImmutableSet;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.api.text.translation.FixedTranslation;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.item.inventory.lens.Fabric;

import java.util.Collection;

public class ItemStackHandlerFabric implements Fabric<ItemStackHandler> {
    private final ItemStackHandler inventory;

    public ItemStackHandlerFabric(ItemStackHandler inventory) {
        this.inventory = inventory;
    }

    @Override
    public Collection<ItemStackHandler> allInventories() {
        return ImmutableSet.of(this.inventory);
    }

    @Override
    public ItemStackHandler get(int index) {
        return this.inventory;
    }

    @Override
    public ItemStack getStack(int index) {
        return this.inventory.getStackInSlot(index);
    }

    @Override
    public void setStack(int index, ItemStack stack) {
        // TODO reject
        // TODO returnvalue
    }

    @Override
    public int getMaxStackSize() {
        return this.inventory.getSlotLimit(0);
    }

    @Override
    public Translation getDisplayName() {
        return new FixedTranslation(getClass().getName());
    }

    @Override
    public int getSize() {
        return this.inventory.getSlots();
    }

    @Override
    public void clear() {
    }

    @Override
    public void markDirty() {
    }
}
