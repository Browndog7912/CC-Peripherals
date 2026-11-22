package com.example.ccperipherals.client;

import com.example.ccperipherals.block.AirConditionerBlock;
import com.example.ccperipherals.block.AirConditionerBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

/** Shows the air conditioner's temperature (the one a computer set, or else the biome's) on its screen. */
public class AirConditionerRenderer implements BlockEntityRenderer<AirConditionerBlockEntity> {
    private final TextRenderer textRenderer;

    public AirConditionerRenderer(BlockEntityRendererFactory.Context context) {
        this.textRenderer = context.getTextRenderer();
    }

    @Override
    public void render(AirConditionerBlockEntity entity, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity.getWorld() == null) return;

        BlockState state = entity.getCachedState();
        if (!state.contains(AirConditionerBlock.ON) || !state.get(AirConditionerBlock.ON)) return;

        TemperatureScreen.draw(textRenderer, entity.getDisplayedCelsius(), state.get(AirConditionerBlock.FACING),
            matrices, vertexConsumers);
    }
}
