package com.example.ccperipherals.block;

import com.example.ccperipherals.CCPeripherals;
import com.example.ccperipherals.peripheral.AirConditionerPeripheral;
import com.example.ccperipherals.util.Temperature;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class AirConditionerBlockEntity extends BlockEntity {
    /** The fan clip is 2 seconds long, so it is replayed every 40 ticks to make a continuous hum. */
    private static final int FAN_CLIP_TICKS = 40;
    private static final float FAN_VOLUME = 0.3f;
    private static final int PARTICLE_INTERVAL = 3;

    private final AirConditionerPeripheral peripheral = new AirConditionerPeripheral(this);

    /** A temperature a computer has set for the screen and particles, in degrees C. Null means use the biome's. */
    @Nullable
    private Float setTemperature;

    public AirConditionerBlockEntity(BlockPos pos, BlockState state) {
        super(CCPeripherals.AIR_CONDITIONER_ENTITY, pos, state);
    }

    public AirConditionerPeripheral getPeripheral() {
        return peripheral;
    }

    // ---- Displayed temperature ----

    /** The temperature the screen shows and the particles are coloured by: the one set by a computer, or the biome's. */
    public float getDisplayedCelsius() {
        if (setTemperature != null) return setTemperature;
        return world == null ? 0.0f : Temperature.celsius(world, pos);
    }

    @Nullable
    public Float getSetTemperature() {
        return setTemperature;
    }

    /** Set (or with null, clear) the displayed temperature, and send it to nearby players. */
    public void setSetTemperature(@Nullable Float celsius) {
        if (Objects.equals(setTemperature, celsius)) return;

        setTemperature = celsius;
        markDirty();
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (setTemperature != null) nbt.putFloat("SetTemperature", setTemperature);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        setTemperature = nbt.contains("SetTemperature") ? nbt.getFloat("SetTemperature") : null;
    }

    // Send the set temperature to clients, because the screen is drawn on the client.
    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    // ---- Effects (server side; players nearby receive the particles and sound) ----

    public static void serverTick(World world, BlockPos pos, BlockState state, AirConditionerBlockEntity entity) {
        if (!(world instanceof ServerWorld serverWorld) || !state.get(AirConditionerBlock.ON)) return;

        long time = world.getTime();

        if (state.get(AirConditionerBlock.SOUND) && Math.floorMod(time + pos.hashCode(), FAN_CLIP_TICKS) == 0) {
            world.playSound(null, pos, CCPeripherals.AIR_CONDITIONER_FAN, SoundCategory.BLOCKS, FAN_VOLUME, 1.0f);
        }

        if (state.get(AirConditionerBlock.PARTICLES) && time % PARTICLE_INTERVAL == 0) {
            spawnAir(serverWorld, pos, state.get(AirConditionerBlock.FACING), entity.getDisplayedCelsius());
        }
    }

    /** Puffs of coloured air blowing out of the vent on the front, coloured by the temperature. */
    private static void spawnAir(ServerWorld world, BlockPos pos, Direction facing, float celsius) {
        DustParticleEffect air = new DustParticleEffect(Temperature.colorFor(celsius), 1.4f);
        var random = world.getRandom();

        // The vent is the lower part of the front face. "Along" runs across the face.
        double alongX = facing.getAxis() == Direction.Axis.Z ? 1 : 0;
        double alongZ = facing.getAxis() == Direction.Axis.X ? 1 : 0;

        for (int i = 0; i < 2; i++) {
            double spread = (random.nextDouble() - 0.5) * 0.6;
            double x = pos.getX() + 0.5 + facing.getOffsetX() * 0.55 + alongX * spread;
            double y = pos.getY() + 0.15 + random.nextDouble() * 0.2;
            double z = pos.getZ() + 0.5 + facing.getOffsetZ() * 0.55 + alongZ * spread;
            // count = 0 makes the last three numbers a direction: blow outwards and slightly down.
            world.spawnParticles(air, x, y, z, 0, facing.getOffsetX(), -0.05, facing.getOffsetZ(), 0.12);
        }
    }
}
