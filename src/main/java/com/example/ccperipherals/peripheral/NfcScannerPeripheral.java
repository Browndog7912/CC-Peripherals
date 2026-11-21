package com.example.ccperipherals.peripheral;

import com.example.ccperipherals.block.NfcScannerBlockEntity;
import com.example.ccperipherals.nfc.NfcCardData;
import com.example.ccperipherals.nfc.NfcColor;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Peripheral type {@code nfc_scanner}. A keycard-style reader: players right-click it holding an NFC card,
 * the card stays in their inventory, and attached computers get an event. The scanner can only read.
 *
 * <p>Event: {@code nfc_scan, name, color, id, data, username, uuid}. {@code data} is nil if the card is blank.
 *
 * <pre>
 * local _, name, color, id, data, user = os.pullEvent("nfc_scan")
 * if color == "green" and data == "door:open" then redstone.setOutput("back", true) end
 * </pre>
 */
public class NfcScannerPeripheral extends EventPeripheral {
    private record Scan(String color, String id, @Nullable String data, String username, String uuid) {
    }

    private final NfcScannerBlockEntity blockEntity;
    @Nullable
    private Scan lastScan;

    public NfcScannerPeripheral(NfcScannerBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public String getType() {
        return "nfc_scanner";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof NfcScannerPeripheral peripheral && peripheral.blockEntity == blockEntity;
    }

    /** A player scanned {@code card}. The stack is only read, never changed or removed (apart from giving a new card its id). */
    public void onScan(ServerPlayerEntity player, ItemStack card) {
        NfcColor color = NfcCardData.colorOf(card);
        if (color == null) return;

        Scan scan = new Scan(color.id(), NfcCardData.ensureId(card), NfcCardData.getData(card),
            player.getGameProfile().getName(), player.getUuidAsString());
        lastScan = scan;
        sendEvent("nfc_scan", scan.color(), scan.id(), scan.data(), scan.username(), scan.uuid());
    }

    /**
     * @return A table {color, id, data, username, uuid} for the most recent scan (data is missing if the
     * card was blank), or nil if nothing has been scanned since the world loaded. Useful if your program
     * missed the event.
     */
    @LuaFunction(mainThread = true)
    public final @Nullable Map<String, Object> getLastScan() {
        Scan scan = lastScan;
        if (scan == null) return null;

        Map<String, Object> result = new HashMap<>();
        result.put("color", scan.color());
        result.put("id", scan.id());
        if (scan.data() != null) result.put("data", scan.data());
        result.put("username", scan.username());
        result.put("uuid", scan.uuid());
        return result;
    }
}
