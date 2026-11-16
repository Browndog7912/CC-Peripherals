package com.example.ccperipherals.peripheral;

import com.example.ccperipherals.block.WeatherSensorBlockEntity;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.level.ServerWorldProperties;
import org.jetbrains.annotations.Nullable;

/**
 * Peripheral type {@code weather_sensor}.
 *
 * <pre>
 * local sensor = peripheral.find("weather_sensor")
 * print(sensor.getWeather())  -- "clear", "rain" or "thunder"
 * </pre>
 */
public class WeatherSensorPeripheral implements IPeripheral {
    private final WeatherSensorBlockEntity blockEntity;

    public WeatherSensorPeripheral(WeatherSensorBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public String getType() {
        return "weather_sensor";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof WeatherSensorPeripheral peripheral && peripheral.blockEntity == blockEntity;
    }

    private World world() throws LuaException {
        World world = blockEntity.getWorld();
        if (world == null) throw new LuaException("Weather sensor is not in a loaded world");
        return world;
    }

    /** @return "clear", "rain" or "thunder", the world's current weather state. */
    @LuaFunction(mainThread = true)
    public final String getWeather() throws LuaException {
        World world = world();
        if (world.isThundering()) return "thunder";
        if (world.isRaining()) return "rain";
        return "clear";
    }

    /** @return true if it is raining (this includes thunderstorms). */
    @LuaFunction(mainThread = true)
    public final boolean isRaining() throws LuaException {
        return world().isRaining();
    }

    /** @return true if there is a thunderstorm. */
    @LuaFunction(mainThread = true)
    public final boolean isThundering() throws LuaException {
        return world().isThundering();
    }

    /** @return true if the weather is clear (no rain, no storm). */
    @LuaFunction(mainThread = true)
    public final boolean isClear() throws LuaException {
        return !world().isRaining();
    }

    /**
     * Whether rain is actually falling on the sensor: it must be raining, the sensor must have open
     * sky above it, and the biome must be warm enough for rain rather than snow or no precipitation.
     */
    @LuaFunction(mainThread = true)
    public final boolean isRainingHere() throws LuaException {
        return world().hasRain(blockEntity.getPos().up());
    }

    /** @return How strongly it is raining, from 0.0 (none) to 1.0 (full). Fades in and out. */
    @LuaFunction(mainThread = true)
    public final double getRainLevel() throws LuaException {
        return world().getRainGradient(1.0f);
    }

    /** @return How strong the thunderstorm is, from 0.0 (none) to 1.0 (full). Fades in and out. */
    @LuaFunction(mainThread = true)
    public final double getThunderLevel() throws LuaException {
        return world().getThunderGradient(1.0f);
    }

    /** @return false in dimensions with no weather (the Nether and the End). */
    @LuaFunction(mainThread = true)
    public final boolean hasWeather() throws LuaException {
        var dimension = world().getDimension();
        return dimension.hasSkyLight() && !dimension.hasCeiling();
    }

    /** @return Ticks until rain next starts or stops (20 ticks = 1 second). */
    @LuaFunction(mainThread = true)
    public final int getRainTime() throws LuaException {
        return weatherProperties().getRainTime();
    }

    /** @return Ticks until thunder next starts or stops (20 ticks = 1 second). */
    @LuaFunction(mainThread = true)
    public final int getThunderTime() throws LuaException {
        return weatherProperties().getThunderTime();
    }

    /** @return Ticks of forced clear weather remaining (for example after /weather clear). */
    @LuaFunction(mainThread = true)
    public final int getClearTime() throws LuaException {
        return weatherProperties().getClearWeatherTime();
    }

    /** Weather timers are stored in the main (overworld) properties and shared by all dimensions. */
    private ServerWorldProperties weatherProperties() throws LuaException {
        return serverWorld().getServer().getSaveProperties().getMainWorldProperties();
    }

    private ServerWorld serverWorld() throws LuaException {
        if (world() instanceof ServerWorld serverWorld) return serverWorld;
        throw new LuaException("Weather timers are only available on the server");
    }
}
