package com.example.ccperipherals.block;

import com.example.ccperipherals.CCPeripherals;
import com.example.ccperipherals.nfc.NfcCardData;
import com.example.ccperipherals.nfc.NfcColor;
import com.example.ccperipherals.peripheral.NfcReaderPeripheral;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class NfcReaderBlockEntity extends BlockEntity {
    private ItemStack card = ItemStack.EMPTY;
    private final NfcReaderPeripheral peripheral = new NfcReaderPeripheral(this);

    public NfcReaderBlockEntity(BlockPos pos, BlockState state) {
        super(CCPeripherals.NFC_READER_ENTITY, pos, state);
    }

    public NfcReaderPeripheral getPeripheral() {
        return peripheral;
    }

    public boolean hasCard() {
        return !card.isEmpty();
    }

    /** The card in the reader (the live stack, so changes to its data are kept), or empty. */
    public ItemStack getCard() {
        return card;
    }

    /** Called after the card's data has been changed. */
    public void cardChanged() {
        markDirty();
    }

    /** Insert one card. Returns false if the reader is full or the stack is not an NFC card. */
    public boolean insertCard(ItemStack single) {
        if (hasCard() || !NfcCardData.isCard(single)) return false;

        NfcCardData.ensureId(single);
        card = single;
        markDirty();
        setHasCardState(true);
        peripheral.fireEvent("nfc_card_inserted", card);
        return true;
    }

    /** Take the card out (player or computer eject). Returns the card, or empty if there was none. */
    public ItemStack removeCard() {
        if (!hasCard()) return ItemStack.EMPTY;

        ItemStack out = card;
        card = ItemStack.EMPTY;
        markDirty();
        setHasCardState(false);
        peripheral.fireEvent("nfc_card_removed", out);
        return out;
    }

    /** Take the card without events or block state changes, for when the block is being broken. */
    public ItemStack takeCardSilently() {
        ItemStack out = card;
        card = ItemStack.EMPTY;
        return out;
    }

    private void setHasCardState(boolean value) {
        if (world == null || world.isClient) return;
        BlockState current = world.getBlockState(pos);
        if (current.isOf(CCPeripherals.NFC_READER)) {
            world.setBlockState(pos, current.with(NfcReaderBlock.HAS_CARD, value), Block.NOTIFY_ALL);
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (hasCard()) nbt.put("Card", card.writeNbt(new NbtCompound()));
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        card = nbt.contains("Card") ? ItemStack.fromNbt(nbt.getCompound("Card")) : ItemStack.EMPTY;
    }
}
