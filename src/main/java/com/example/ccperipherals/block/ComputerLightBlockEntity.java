package com.example.ccperipherals.block;

import com.example.ccperipherals.CCPeripherals;
import com.example.ccperipherals.peripheral.LightPeripheral;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class ComputerLightBlockEntity extends BlockEntity {
    private final LightPeripheral peripheral = new LightPeripheral(this);

    public ComputerLightBlockEntity(BlockPos pos, BlockState state) {
        super(CCPeripherals.COMPUTER_LIGHT_ENTITY, pos, state);
    }

    public LightPeripheral getPeripheral() {
        return peripheral;
    }
}
