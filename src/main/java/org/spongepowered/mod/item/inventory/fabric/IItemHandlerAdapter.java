package org.spongepowered.mod.item.inventory.fabric;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.item.inventory.adapter.impl.Adapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;
import org.spongepowered.common.item.inventory.lens.impl.comp.OrderedInventoryLensImpl;

public class IItemHandlerAdapter extends Adapter {

    public IItemHandlerAdapter(IItemHandler handler) {
        super(((Fabric) new IItemHandlerFabric(handler)));
    }

    @Override
    protected Lens<IInventory, ItemStack> initRootLens() {
        return new OrderedInventoryLensImpl(0, getInventory().getSize(), 1, slots);
    }

    @Override
    protected SlotCollection initSlots(Fabric<IInventory> inventory, Lens<IInventory, ItemStack> root, Inventory parent) {
        return new SlotCollection(inventory.getSize());
    }
}
