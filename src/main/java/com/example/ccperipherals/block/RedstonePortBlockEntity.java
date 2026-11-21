package com.example.ccperipherals.block;

import com.example.ccperipherals.CCPeripherals;
import com.example.ccperipherals.peripheral.RedstonePortPeripheral;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Arrays;

public class RedstonePortBlockEntity extends BlockEntity {
    /** Output power per side (0 to 15), indexed by {@link Direction#getId()}. */
    private final int[] outputs = new int[6];
    /** The inputs seen last time, so changes can be reported. -1 means not known yet. */
    private final int[] lastInputs = new int[6];
    private final RedstonePortPeripheral peripheral = new RedstonePortPeripheral(this);

    public RedstonePortBlockEntity(BlockPos pos, BlockState state) {
        super(CCPeripherals.REDSTONE_PORT_ENTITY, pos, state);
        Arrays.fill(lastInputs, -1);
    }

    public RedstonePortPeripheral getPeripheral() {
        return peripheral;
    }

    public int getOutput(Direction side) {
        return outputs[side.getId()];
    }

    /** The redstone power arriving at the given side of this block from outside, 0 to 15. */
    public int getInput(Direction side) {
        return world == null ? 0 : world.getEmittedRedstonePower(pos.offset(side), side);
    }

    /** Set the power sent out of one side and tell neighbouring blocks. */
    public void setOutput(Direction side, int power) {
        if (outputs[side.getId()] == power) return;
        outputs[side.getId()] = power;
        markDirty();
        refreshWorld();
    }

    /** Checks all six inputs and reports any that changed to attached computers. */
    public void onNeighborChanged() {
        for (Direction side : Direction.values()) {
            int power = getInput(side);
            int last = lastInputs[side.getId()];
            lastInputs[side.getId()] = power;
            if (last != -1 && last != power) peripheral.fireInput(side, power);
        }
    }

    private void refreshWorld() {
        if (world == null || world.isClient) return;

        boolean anyOutput = Arrays.stream(outputs).anyMatch(power -> power > 0);
        BlockState state = world.getBlockState(pos);
        if (state.isOf(CCPeripherals.REDSTONE_PORT) && state.get(RedstonePortBlock.POWERED) != anyOutput) {
            world.setBlockState(pos, state.with(RedstonePortBlock.POWERED, anyOutput), Block.NOTIFY_ALL);
        }
        world.updateNeighborsAlways(pos, CCPeripherals.REDSTONE_PORT);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putIntArray("Outputs", outputs.clone());
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        int[] saved = nbt.getIntArray("Outputs");
        for (int i = 0; i < outputs.length; i++) outputs[i] = i < saved.length ? Math.max(0, Math.min(15, saved[i])) : 0;
    }
}
