package com.example.ccperipherals.client;

import com.example.ccperipherals.block.ThermostatBlock;
import com.example.ccperipherals.block.ThermostatBlockEntity;
import com.example.ccperipherals.util.Temperature;
import net.minecraft.block.BlockState;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

/** Shows the biome's temperature on the thermostat's screen. */
public class ThermostatRenderer implements BlockEntityRenderer<ThermostatBlockEntity> {
    private final TextRenderer textRenderer;

    public ThermostatRenderer(BlockEntityRendererFactory.Context context) {
        this.textRenderer = context.getTextRenderer();
    }

    @Override
    public void render(ThermostatBlockEntity entity, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        var world = entity.getWorld();
        if (world == null) return;

        BlockState state = entity.getCachedState();
        if (!state.contains(ThermostatBlock.FACING)) return;

        TemperatureScreen.draw(textRenderer, Temperature.celsius(world, entity.getPos()), state.get(ThermostatBlock.FACING),
            matrices, vertexConsumers);
    }
}
