package com.example.ccperipherals.client;

import com.example.ccperipherals.CCPeripherals;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class CCPeripheralsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRendererFactories.register(CCPeripherals.AIR_CONDITIONER_ENTITY, AirConditionerRenderer::new);
        BlockEntityRendererFactories.register(CCPeripherals.THERMOSTAT_ENTITY, ThermostatRenderer::new);
    }
}
