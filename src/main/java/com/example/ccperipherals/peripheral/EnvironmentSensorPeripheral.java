package com.example.ccperipherals.peripheral;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Random;

/**
 * Peripheral type {@code environment_sensor}.
 *
 * <pre>
 * local env = peripheral.find("environment_sensor")
 * print(env.getDimension(), env.getBiome())
 * local pos = env.getPosition()
 * print(pos.x, pos.y, pos.z, env.isSlimeChunk())
 * </pre>
 */
public class EnvironmentSensorPeripheral implements IPeripheral {
    /** Vanilla's scrambler for slime chunk selection. */
    private static final long SLIME_SCRAMBLER = 987234911L;

    private final Locator locator;

    public EnvironmentSensorPeripheral(Locator locator) {
        this.locator = locator;
    }

    @Override
    public String getType() {
        return "environment_sensor";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof EnvironmentSensorPeripheral peripheral
            && peripheral.locator.owner() == locator.owner();
    }

    private World world() throws LuaException {
        World world = locator.world();
        if (world == null) throw new LuaException("Environment sensor is not in a loaded world");
        return world;
    }

    /** @return The dimension id, e.g. "minecraft:overworld", "minecraft:the_nether", "minecraft:the_end". */
    @LuaFunction(mainThread = true)
    public final String getDimension() throws LuaException {
        return world().getRegistryKey().getValue().toString();
    }

    /** @return The biome id at the sensor, e.g. "minecraft:plains". */
    @LuaFunction(mainThread = true)
    public final String getBiome() throws LuaException {
        return world().getBiome(locator.pos()).getKey()
            .map(key -> key.getValue().toString())
            .orElse("unknown");
    }

    /** @return A table {x=, y=, z=} with the sensor's block position. */
    @LuaFunction(mainThread = true)
    public final Map<String, Integer> getPosition() throws LuaException {
        world();
        BlockPos pos = locator.pos();
        return Map.of("x", pos.getX(), "y", pos.getY(), "z", pos.getZ());
    }

    /** @return The sensor's X coordinate. */
    @LuaFunction(mainThread = true)
    public final int getX() throws LuaException {
        world();
        return locator.pos().getX();
    }

    /** @return The sensor's Y coordinate. */
    @LuaFunction(mainThread = true)
    public final int getY() throws LuaException {
        world();
        return locator.pos().getY();
    }

    /** @return The sensor's Z coordinate. */
    @LuaFunction(mainThread = true)
    public final int getZ() throws LuaException {
        world();
        return locator.pos().getZ();
    }

    /** @return A table {x=, z=} with the chunk coordinates the sensor is in. */
    @LuaFunction(mainThread = true)
    public final Map<String, Integer> getChunkPosition() throws LuaException {
        world();
        ChunkPos chunk = new ChunkPos(locator.pos());
        return Map.of("x", chunk.x, "z", chunk.z);
    }

    /**
     * Whether the sensor is in a slime chunk. Slime chunks only exist in the Overworld, so this is
     * always false in other dimensions. (Slimes can also spawn in swamps regardless of chunk.)
     */
    @LuaFunction(mainThread = true)
    public final boolean isSlimeChunk() throws LuaException {
        World world = world();
        if (!(world instanceof ServerWorld serverWorld) || world.getRegistryKey() != World.OVERWORLD) {
            return false;
        }

        ChunkPos chunk = new ChunkPos(locator.pos());
        return isSlimeChunk(serverWorld.getSeed(), chunk.x, chunk.z);
    }

    /**
     * Vanilla's slime chunk formula. The int arithmetic (and its overflow) is intentional: it must
     * match Minecraft exactly, since the multiplications happen in 32-bit ints before widening.
     */
    static boolean isSlimeChunk(long worldSeed, int chunkX, int chunkZ) {
        long seed = worldSeed
            + (long) (chunkX * chunkX * 4987142)
            + (long) (chunkX * 5947611)
            + (long) (chunkZ * chunkZ) * 4392871L
            + (long) (chunkZ * 389711)
            ^ SLIME_SCRAMBLER;
        return new Random(seed).nextInt(10) == 0;
    }
}
