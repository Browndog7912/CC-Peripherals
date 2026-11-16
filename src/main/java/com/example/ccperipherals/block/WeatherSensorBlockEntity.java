package com.example.ccperipherals.block;

import com.example.ccperipherals.CCPeripherals;
import com.example.ccperipherals.peripheral.WeatherSensorPeripheral;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class WeatherSensorBlockEntity extends BlockEntity {
    private final WeatherSensorPeripheral peripheral = new WeatherSensorPeripheral(this);

    public WeatherSensorBlockEntity(BlockPos pos, BlockState state) {
        super(CCPeripherals.WEATHER_SENSOR_ENTITY, pos, state);
    }

    public WeatherSensorPeripheral getPeripheral() {
        return peripheral;
    }
}
