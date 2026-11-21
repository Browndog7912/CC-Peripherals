package com.example.ccperipherals.peripheral;

import dan200.computercraft.api.pocket.IPocketAccess;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Where a peripheral currently is. This lets the same peripheral code run from a placed block
 * (a block entity) or from a pocket computer carried by a player (an entity).
 */
public interface Locator {
    /** The world the peripheral is in, or null if it is not available (e.g. pocket computer in a chest). */
    @Nullable
    World world();

    /** The block position of the peripheral. */
    BlockPos pos();

    /** Identity used for peripheral equality: two peripherals are equal if they share an owner. */
    Object owner();

    static Locator ofBlockEntity(BlockEntity blockEntity) {
        return new Locator() {
            @Override public @Nullable World world() { return blockEntity.getWorld(); }
            @Override public BlockPos pos() { return blockEntity.getPos(); }
            @Override public Object owner() { return blockEntity; }
        };
    }

    static Locator ofPocket(IPocketAccess access) {
        return new Locator() {
            @Override
            public @Nullable World world() {
                Entity entity = access.getEntity();
                return entity == null ? null : entity.getWorld();
            }

            @Override
            public BlockPos pos() {
                Entity entity = access.getEntity();
                return entity == null ? BlockPos.ORIGIN : entity.getBlockPos();
            }

            @Override public Object owner() { return access; }
        };
    }
}
