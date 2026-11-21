package com.example.ccperipherals.peripheral;

import com.example.ccperipherals.block.RedstonePortBlockEntity;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * Peripheral type {@code redstone_port}. Sides are absolute directions, not relative to a facing:
 * "top", "bottom", "north", "south", "east" and "west" ("up" and "down" also work).
 *
 * <p>Event: {@code redstone_input, name, side, power} when the signal coming in on a side changes.
 *
 * <pre>
 * local port = peripheral.find("redstone_port")
 * port.setOutput("north", true)
 * print(port.getAnalogInput("top"))
 * </pre>
 */
public class RedstonePortPeripheral extends EventPeripheral {
    private static final String[] SIDES = {"top", "bottom", "north", "south", "east", "west"};

    private final RedstonePortBlockEntity blockEntity;

    public RedstonePortPeripheral(RedstonePortBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public String getType() {
        return "redstone_port";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof RedstonePortPeripheral peripheral && peripheral.blockEntity == blockEntity;
    }

    /** An input changed. Called from the block entity. */
    public void fireInput(Direction side, int power) {
        sendEvent("redstone_input", sideName(side), power);
    }

    private static String sideName(Direction side) {
        return switch (side) {
            case UP -> "top";
            case DOWN -> "bottom";
            default -> side.getName();
        };
    }

    private static Direction side(String name) throws LuaException {
        return switch (name.toLowerCase()) {
            case "top", "up" -> Direction.UP;
            case "bottom", "down" -> Direction.DOWN;
            case "north" -> Direction.NORTH;
            case "south" -> Direction.SOUTH;
            case "east" -> Direction.EAST;
            case "west" -> Direction.WEST;
            default -> throw new LuaException("Invalid side '" + name + "' (use top, bottom, north, south, east or west)");
        };
    }

    /** @return The six side names: top, bottom, north, south, east, west. */
    @LuaFunction
    public final String[] getSides() {
        return SIDES.clone();
    }

    /** Turns the signal out of a side fully on (15) or off (0). */
    @LuaFunction(mainThread = true)
    public final void setOutput(String side, boolean on) throws LuaException {
        blockEntity.setOutput(side(side), on ? 15 : 0);
    }

    /** Sets the signal strength out of a side, from 0 to 15. */
    @LuaFunction(mainThread = true)
    public final void setAnalogOutput(String side, int power) throws LuaException {
        if (power < 0 || power > 15) throw new LuaException("Power must be between 0 and 15");
        blockEntity.setOutput(side(side), power);
    }

    /** @return true if this side is sending any signal out. */
    @LuaFunction(mainThread = true)
    public final boolean getOutput(String side) throws LuaException {
        return blockEntity.getOutput(side(side)) > 0;
    }

    /** @return The strength of the signal this side is sending out, 0 to 15. */
    @LuaFunction(mainThread = true)
    public final int getAnalogOutput(String side) throws LuaException {
        return blockEntity.getOutput(side(side));
    }

    /** @return true if a signal is coming in on this side. */
    @LuaFunction(mainThread = true)
    public final boolean getInput(String side) throws LuaException {
        return blockEntity.getInput(side(side)) > 0;
    }

    /** @return The strength of the signal coming in on this side, 0 to 15. */
    @LuaFunction(mainThread = true)
    public final int getAnalogInput(String side) throws LuaException {
        return blockEntity.getInput(side(side));
    }
}
