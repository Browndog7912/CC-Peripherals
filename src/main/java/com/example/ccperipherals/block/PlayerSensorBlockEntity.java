package com.example.ccperipherals.block;

import com.example.ccperipherals.CCPeripherals;
import com.example.ccperipherals.peripheral.Locator;
import com.example.ccperipherals.peripheral.PlayerSensorPeripheral;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class PlayerSensorBlockEntity extends BlockEntity {
    private final PlayerSensorPeripheral peripheral = new PlayerSensorPeripheral(Locator.ofBlockEntity(this));

    public PlayerSensorBlockEntity(BlockPos pos, BlockState state) {
        super(CCPeripherals.PLAYER_SENSOR_ENTITY, pos, state);
    }

    public PlayerSensorPeripheral getPeripheral() {
        return peripheral;
    }
}
