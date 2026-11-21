package com.example.ccperipherals.nfc;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/** Reads and writes the data stored on an NFC card item stack (in its NBT). */
public final class NfcCardData {
    /** The most characters a card can hold. */
    public static final int MAX_LENGTH = 1024;

    private static final String ID_KEY = "NfcId";
    private static final String DATA_KEY = "NfcData";

    private NfcCardData() {
    }

    public static boolean isCard(ItemStack stack) {
        return stack.getItem() instanceof NfcCardItem;
    }

    @Nullable
    public static NfcColor colorOf(ItemStack stack) {
        return stack.getItem() instanceof NfcCardItem card ? card.getColor() : null;
    }

    /** The card's unique id, created the first time it is needed. */
    public static String ensureId(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        if (!nbt.contains(ID_KEY)) nbt.putString(ID_KEY, UUID.randomUUID().toString());
        return nbt.getString(ID_KEY);
    }

    /** The stored data, or null if nothing has been written to the card. */
    @Nullable
    public static String getData(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        return nbt != null && nbt.contains(DATA_KEY) ? nbt.getString(DATA_KEY) : null;
    }

    public static void setData(ItemStack stack, String data) {
        stack.getOrCreateNbt().putString(DATA_KEY, data);
    }

    public static void clearData(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null) nbt.remove(DATA_KEY);
    }
}
