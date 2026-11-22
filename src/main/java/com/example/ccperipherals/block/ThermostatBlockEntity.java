package com.example.ccperipherals.block;

import com.example.ccperipherals.CCPeripherals;
import com.example.ccperipherals.peripheral.ThermostatPeripheral;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class ThermostatBlockEntity extends BlockEntity {
    private final ThermostatPeripheral peripheral = new ThermostatPeripheral(this);

    public ThermostatBlockEntity(BlockPos pos, BlockState state) {
        super(CCPeripherals.THERMOSTAT_ENTITY, pos, state);
    }

    public ThermostatPeripheral getPeripheral() {
        return peripheral;
    }
}
