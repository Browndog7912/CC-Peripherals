package com.example.ccperipherals.peripheral;

import com.example.ccperipherals.CCPeripherals;
import com.example.ccperipherals.block.AirConditionerBlock;
import com.example.ccperipherals.block.AirConditionerBlockEntity;
import com.example.ccperipherals.util.Temperature;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Peripheral type {@code air_conditioner}.
 *
 * <pre>
 * local ac = peripheral.find("air_conditioner")
 * print(ac.getTemperature())      -- degrees Celsius
 * ac.setSoundEnabled(false)       -- keep the screen and particles, silence the fan
 * ac.setOn(false)                 -- switch everything off
 * </pre>
 */
public class AirConditionerPeripheral implements IPeripheral {
    private final AirConditionerBlockEntity blockEntity;

    public AirConditionerPeripheral(AirConditionerBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public String getType() {
        return "air_conditioner";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof AirConditionerPeripheral peripheral && peripheral.blockEntity == blockEntity;
    }

    private World world() throws LuaException {
        World world = blockEntity.getWorld();
        if (world == null) throw new LuaException("Air conditioner is not in a loaded world");
        return world;
    }

    private BlockState state() throws LuaException {
        BlockState state = world().getBlockState(blockEntity.getPos());
        if (!state.isOf(CCPeripherals.AIR_CONDITIONER)) throw new LuaException("Air conditioner is no longer there");
        return state;
    }

    private boolean get(BooleanProperty property) throws LuaException {
        return state().get(property);
    }

    private void set(BooleanProperty property, boolean value) throws LuaException {
        BlockState state = state();
        if (state.get(property) != value) {
            world().setBlockState(blockEntity.getPos(), state.with(property, value), Block.NOTIFY_ALL);
        }
    }

    // ---- Power ----

    /** Turns the air conditioner on or off. Off means the screen is dark and there are no particles or sound. */
    @LuaFunction(mainThread = true)
    public final void setOn(boolean on) throws LuaException {
        set(AirConditionerBlock.ON, on);
    }

    /** @return true if the air conditioner is on. */
    @LuaFunction(mainThread = true)
    public final boolean isOn() throws LuaException {
        return get(AirConditionerBlock.ON);
    }

    /** Switches it on if it is off and off if it is on. @return The new state (true = on). */
    @LuaFunction(mainThread = true)
    public final boolean toggle() throws LuaException {
        boolean on = !get(AirConditionerBlock.ON);
        set(AirConditionerBlock.ON, on);
        return on;
    }

    // ---- Effects ----

    /** Turns the air particles on or off. They only show while the air conditioner is on. */
    @LuaFunction(mainThread = true)
    public final void setParticlesEnabled(boolean enabled) throws LuaException {
        set(AirConditionerBlock.PARTICLES, enabled);
    }

    /** @return true if the air particles are enabled. */
    @LuaFunction(mainThread = true)
    public final boolean areParticlesEnabled() throws LuaException {
        return get(AirConditionerBlock.PARTICLES);
    }

    /** Turns the fan noise on or off. It only plays while the air conditioner is on. */
    @LuaFunction(mainThread = true)
    public final void setSoundEnabled(boolean enabled) throws LuaException {
        set(AirConditionerBlock.SOUND, enabled);
    }

    /** @return true if the fan noise is enabled. */
    @LuaFunction(mainThread = true)
    public final boolean isSoundEnabled() throws LuaException {
        return get(AirConditionerBlock.SOUND);
    }

    // ---- Temperature (works whether or not the air conditioner is on) ----

    /** @return The real temperature of the biome here in degrees Celsius, to one decimal place. */
    @LuaFunction(mainThread = true)
    public final double getTemperature() throws LuaException {
        return round1(Temperature.celsius(world(), blockEntity.getPos()));
    }

    /** @return The temperature here in degrees Fahrenheit, to one decimal place. */
    @LuaFunction(mainThread = true)
    public final double getTemperatureFahrenheit() throws LuaException {
        return round1(Temperature.toFahrenheit(Temperature.celsius(world(), blockEntity.getPos())));
    }

    /** @return Minecraft's own temperature value for here (about 0.0 snowy to 2.0 desert). */
    @LuaFunction(mainThread = true)
    public final double getRawTemperature() throws LuaException {
        return round1(Temperature.rawTemperature(world(), blockEntity.getPos()) * 100.0) / 100.0;
    }

    // ---- Display temperature (mostly decoration: it changes what the screen and particles show) ----

    /**
     * Sets the temperature shown on the screen and used to colour the particles, in degrees Celsius.
     * This is for decoration only: it does not change the real temperature, which getTemperature() still reports.
     */
    @LuaFunction(mainThread = true)
    public final void setTemperature(double celsius) throws LuaException {
        if (Double.isNaN(celsius) || Math.abs(celsius) > 999) {
            throw new LuaException("Temperature must be a number between -999 and 999");
        }
        blockEntity.setSetTemperature((float) celsius);
    }

    /** @return The temperature a computer has set for the display, or nil if it is showing the biome's. */
    @LuaFunction(mainThread = true)
    public final @Nullable Double getSetTemperature() {
        Float set = blockEntity.getSetTemperature();
        return set == null ? null : round1(set);
    }

    /** Goes back to showing the real temperature of the biome. */
    @LuaFunction(mainThread = true)
    public final void resetTemperature() {
        blockEntity.setSetTemperature(null);
    }

    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
