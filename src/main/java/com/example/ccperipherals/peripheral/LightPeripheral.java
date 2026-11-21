package com.example.ccperipherals.peripheral;

import com.example.ccperipherals.CCPeripherals;
import com.example.ccperipherals.block.ComputerLightBlock;
import com.example.ccperipherals.block.ComputerLightBlockEntity;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Peripheral type {@code light}.
 *
 * <pre>
 * local lamp = peripheral.find("light")
 * lamp.setLit(true)
 * lamp.setBrightness(8)
 * </pre>
 */
public class LightPeripheral implements IPeripheral {
    private final ComputerLightBlockEntity blockEntity;

    public LightPeripheral(ComputerLightBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public String getType() {
        return "light";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof LightPeripheral peripheral && peripheral.blockEntity == blockEntity;
    }

    private World world() throws LuaException {
        World world = blockEntity.getWorld();
        if (world == null) throw new LuaException("Light is not in a loaded world");
        return world;
    }

    private int level() throws LuaException {
        BlockState state = world().getBlockState(blockEntity.getPos());
        return state.isOf(CCPeripherals.COMPUTER_LIGHT) ? state.get(ComputerLightBlock.LIGHT_LEVEL) : 0;
    }

    private void setLevel(int level) throws LuaException {
        World world = world();
        BlockState state = world.getBlockState(blockEntity.getPos());
        if (state.isOf(CCPeripherals.COMPUTER_LIGHT) && state.get(ComputerLightBlock.LIGHT_LEVEL) != level) {
            world.setBlockState(blockEntity.getPos(), state.with(ComputerLightBlock.LIGHT_LEVEL, level), Block.NOTIFY_ALL);
        }
    }

    /** Turns the light on (full brightness) or off. */
    @LuaFunction(mainThread = true)
    public final void setLit(boolean lit) throws LuaException {
        setLevel(lit ? 15 : 0);
    }

    /** @return true if the light is on at any brightness. */
    @LuaFunction(mainThread = true)
    public final boolean isLit() throws LuaException {
        return level() > 0;
    }

    /** Switches the light on if it is off, and off if it is on. @return The new state (true = on). */
    @LuaFunction(mainThread = true)
    public final boolean toggle() throws LuaException {
        boolean lit = level() == 0;
        setLevel(lit ? 15 : 0);
        return lit;
    }

    /** Sets the brightness from 0 (off) to 15 (full). */
    @LuaFunction(mainThread = true)
    public final void setBrightness(int level) throws LuaException {
        if (level < 0 || level > 15) throw new LuaException("Brightness must be between 0 and 15");
        setLevel(level);
    }

    /** @return The brightness, from 0 (off) to 15 (full). */
    @LuaFunction(mainThread = true)
    public final int getBrightness() throws LuaException {
        return level();
    }
}
