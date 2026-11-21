package com.example.ccperipherals.peripheral;

import com.example.ccperipherals.CCPeripherals;
import com.example.ccperipherals.block.ComputerButtonBlock;
import com.example.ccperipherals.block.ComputerButtonBlockEntity;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Peripheral type {@code button}.
 *
 * <p>Event: {@code button_click, name, username, uuid} when a player clicks the button.
 *
 * <pre>
 * local _, name, user = os.pullEvent("button_click")
 * print(user .. " pressed " .. name)
 * </pre>
 */
public class ButtonPeripheral extends EventPeripheral {
    private final ComputerButtonBlockEntity blockEntity;

    public ButtonPeripheral(ComputerButtonBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public String getType() {
        return "button";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof ButtonPeripheral peripheral && peripheral.blockEntity == blockEntity;
    }

    /** A player clicked the button. */
    public void onClick(ServerPlayerEntity player) {
        sendEvent("button_click", player.getGameProfile().getName(), player.getUuidAsString());
    }

    /** @return true while the button is physically pressed in (a short moment after each click). */
    @LuaFunction(mainThread = true)
    public final boolean isPressed() {
        if (blockEntity.getWorld() == null) return false;
        BlockState state = blockEntity.getWorld().getBlockState(blockEntity.getPos());
        return state.isOf(CCPeripherals.COMPUTER_BUTTON) && state.get(ComputerButtonBlock.PRESSED);
    }
}
