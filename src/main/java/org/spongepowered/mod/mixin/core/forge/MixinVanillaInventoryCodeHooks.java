/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.mod.mixin.core.forge;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.VanillaInventoryCodeHooks;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.interfaces.IMixinInventory;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.util.InventoryUtil;

@Mixin(VanillaInventoryCodeHooks.class)
public class MixinVanillaInventoryCodeHooks {

    @Inject(method = "insertHook", locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 1))
    private static void afterPutStackInSlots(TileEntityHopper hopper, CallbackInfoReturnable<Boolean> cir,
            EnumFacing hopperFacing, Pair<IItemHandler, Object> destinationResult, IItemHandler itemHandler,
            Object destination, int i, ItemStack originalSlotContents,
            ItemStack insertStack, ItemStack remainder) {
        // after putStackInInventoryAllSlots
        remainder = InventoryUtil.handleTransferPost(hopper, i, originalSlotContents, destination, remainder);
    }

    @Shadow private static ItemStack insertStack(TileEntity source, Object destination, IItemHandler destInventory, ItemStack stack, int slot) {
        throw new AbstractMethodError("Shadow");
    }

    @Redirect(method = "putStackInInventoryAllSlots", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/items/VanillaInventoryCodeHooks;insertStack(Lnet/minecraft/tileentity/TileEntity;Ljava/lang/Object;Lnet/minecraftforge/items/IItemHandler;Lnet/minecraft/item/ItemStack;I)Lnet/minecraft/item/ItemStack;"))
    private static ItemStack onInsertStack(TileEntity source, Object destination, IItemHandler destInventory, ItemStack stack, int slot) {
        // capture Transaction
        // TODO handle IItemHandler
        if (!(destination instanceof InventoryAdapter && source instanceof IMixinInventory)) {
            return insertStack(source, destination, destInventory, stack, slot);
        }

        InventoryAdapter adapter = ((InventoryAdapter) destination);
        if (destination instanceof TileEntityChest) {
            adapter = ((InventoryAdapter) InventoryUtil.getDoubleChestInventory(((TileEntityChest) destination)).orElse(adapter));
        }
        return InventoryUtil
                .captureInsertRemote(((IInventory) source), adapter, slot, () -> insertStack(source, destination, destInventory, stack, slot));
    }

    @Inject(method = "insertHook", cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION, at = @At(value = "INVOKE", target = "Lnet/minecraftforge/items/VanillaInventoryCodeHooks;isFull(Lnet/minecraftforge/items/IItemHandler;)Z"))
    private static void onTransferItemsOut(TileEntityHopper hopper, CallbackInfoReturnable<Boolean> cir, EnumFacing hopperFacing, Pair<IItemHandler, Object> destinationResult, IItemHandler itemHandler, Object destination) {
        if (InventoryUtil.handleTransferPre(hopper, destination).isCancelled()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "dropperInsertHook", cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION, at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;copy()Lnet/minecraft/item/ItemStack;", ordinal = 0))
    private static void onDispense(World world, BlockPos pos, TileEntityDispenser dropper, int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir, EnumFacing enumFacing, BlockPos blockPos, Pair<IItemHandler, Object> destinationResult, IItemHandler itemHandler, Object destination) {
        if (InventoryUtil.handleTransferPre(dropper, destination).isCancelled()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "dropperInsertHook", locals = LocalCapture.CAPTURE_FAILEXCEPTION, at = @At(value = "RETURN", ordinal = 1))
    private static void afterDispense(World world, BlockPos pos, TileEntityDispenser dropper, int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir,
            EnumFacing enumFacing, BlockPos blockPos, Pair<IItemHandler, Object> destinationResult, IItemHandler itemHandler,
            Object destination, ItemStack dispensedStack, ItemStack remainder) {
        // after setInventorySlotContents if return false
        InventoryUtil.handleTransferPost(dropper, slot, stack, destination, remainder);
    }
}
