package com.example.ccperipherals.block;

import com.example.ccperipherals.CCPeripherals;
import com.example.ccperipherals.peripheral.EnvironmentSensorPeripheral;
import com.example.ccperipherals.peripheral.Locator;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class EnvironmentSensorBlockEntity extends BlockEntity {
    private final EnvironmentSensorPeripheral peripheral = new EnvironmentSensorPeripheral(Locator.ofBlockEntity(this));

    public EnvironmentSensorBlockEntity(BlockPos pos, BlockState state) {
        super(CCPeripherals.ENVIRONMENT_SENSOR_ENTITY, pos, state);
    }

    public EnvironmentSensorPeripheral getPeripheral() {
        return peripheral;
    }
}
