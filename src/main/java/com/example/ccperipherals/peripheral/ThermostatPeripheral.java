package com.example.ccperipherals.peripheral;

import com.example.ccperipherals.block.ThermostatBlockEntity;
import com.example.ccperipherals.util.Temperature;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Peripheral type {@code thermostat}. Read-only: it reports the real temperature of the biome it is in.
 *
 * <pre>
 * local thermostat = peripheral.find("thermostat")
 * print(thermostat.getTemperature() .. " C in " .. thermostat.getBiome())
 * </pre>
 */
public class ThermostatPeripheral implements IPeripheral {
    private final ThermostatBlockEntity blockEntity;

    public ThermostatPeripheral(ThermostatBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public String getType() {
        return "thermostat";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof ThermostatPeripheral peripheral && peripheral.blockEntity == blockEntity;
    }

    private World world() throws LuaException {
        World world = blockEntity.getWorld();
        if (world == null) throw new LuaException("Thermostat is not in a loaded world");
        return world;
    }

    /** @return The temperature here in degrees Celsius, to one decimal place. */
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
        return Math.round(Temperature.rawTemperature(world(), blockEntity.getPos()) * 100.0) / 100.0;
    }

    /** @return The biome id here, e.g. "minecraft:plains". */
    @LuaFunction(mainThread = true)
    public final String getBiome() throws LuaException {
        return world().getBiome(blockEntity.getPos()).getKey()
            .map(key -> key.getValue().toString())
            .orElse("unknown");
    }

    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
