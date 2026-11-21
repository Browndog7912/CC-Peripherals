package com.example.ccperipherals.peripheral;

import com.example.ccperipherals.block.NfcReaderBlockEntity;
import com.example.ccperipherals.nfc.NfcCardData;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.util.ItemScatterer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Peripheral type {@code nfc_reader}.
 *
 * <p>Events: {@code nfc_card_inserted, name, color, id} and {@code nfc_card_removed, name, color, id}.
 *
 * <pre>
 * local reader = peripheral.find("nfc_reader")
 * if reader.hasCard() then
 *   reader.write("hello")
 *   print(reader.read())
 * end
 * </pre>
 */
public class NfcReaderPeripheral implements IPeripheral {
    private final NfcReaderBlockEntity reader;
    private final Set<IComputerAccess> computers = ConcurrentHashMap.newKeySet();

    public NfcReaderPeripheral(NfcReaderBlockEntity reader) {
        this.reader = reader;
    }

    @Override
    public String getType() {
        return "nfc_reader";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof NfcReaderPeripheral peripheral && peripheral.reader == reader;
    }

    @Override
    public void attach(IComputerAccess computer) {
        computers.add(computer);
    }

    @Override
    public void detach(IComputerAccess computer) {
        computers.remove(computer);
    }

    /** Queue an event on every attached computer: event, peripheral name, card colour, card id. */
    public void fireEvent(String event, ItemStack card) {
        var color = NfcCardData.colorOf(card);
        String colorName = color == null ? "unknown" : color.id();
        String id = NfcCardData.ensureId(card);
        for (IComputerAccess computer : computers) {
            computer.queueEvent(event, computer.getAttachmentName(), colorName, id);
        }
    }

    // ---- Lua API ----

    /** @return true if a card is in the reader. */
    @LuaFunction(mainThread = true)
    public final boolean hasCard() {
        return reader.hasCard();
    }

    /** @return The card's colour ("red", "green", "yellow", "purple", "blue" or "white"), or nil if no card. */
    @LuaFunction(mainThread = true)
    public final @Nullable String getCardColor() {
        var color = NfcCardData.colorOf(reader.getCard());
        return color == null ? null : color.id();
    }

    /** @return The card's unique id, or nil if no card. */
    @LuaFunction(mainThread = true)
    public final @Nullable String getCardId() {
        return reader.hasCard() ? NfcCardData.ensureId(reader.getCard()) : null;
    }

    /** @return The most characters a card can hold. */
    @LuaFunction
    public final int getMaxLength() {
        return NfcCardData.MAX_LENGTH;
    }

    /**
     * Reads the data stored on the card.
     *
     * @return The data string (nil if the card is blank), or nil and a reason if there is no card.
     */
    @LuaFunction(mainThread = true)
    public final MethodResult read() {
        if (!reader.hasCard()) return MethodResult.of(null, "No card in reader");
        return MethodResult.of(NfcCardData.getData(reader.getCard()));
    }

    /**
     * Writes a string to the card, replacing what was there. To store a table, serialise it first,
     * e.g. with textutils.serialiseJSON.
     *
     * @return true, or false and a reason (no card, or data too long).
     */
    @LuaFunction(mainThread = true)
    public final MethodResult write(String data) {
        if (!reader.hasCard()) return MethodResult.of(false, "No card in reader");
        if (data.length() > NfcCardData.MAX_LENGTH) {
            return MethodResult.of(false, "Data too long (max " + NfcCardData.MAX_LENGTH + " characters)");
        }

        NfcCardData.setData(reader.getCard(), data);
        reader.cardChanged();
        return MethodResult.of(true);
    }

    /** Erases the data on the card (the card keeps its id). @return true, or false and a reason. */
    @LuaFunction(mainThread = true)
    public final MethodResult clear() {
        if (!reader.hasCard()) return MethodResult.of(false, "No card in reader");

        NfcCardData.clearData(reader.getCard());
        reader.cardChanged();
        return MethodResult.of(true);
    }

    /** Pops the card out of the reader above the block. @return true, or false and a reason. */
    @LuaFunction(mainThread = true)
    public final MethodResult eject() {
        if (!reader.hasCard()) return MethodResult.of(false, "No card in reader");

        ItemStack card = reader.removeCard();
        BlockPos pos = reader.getPos();
        if (reader.getWorld() != null) {
            ItemScatterer.spawn(reader.getWorld(), pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, card);
        }
        return MethodResult.of(true);
    }
}
