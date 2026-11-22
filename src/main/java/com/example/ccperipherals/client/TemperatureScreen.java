package com.example.ccperipherals.client;

import com.example.ccperipherals.util.Temperature;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector3f;

/** Draws a temperature reading as text on the screen on the front of a block. Shared by the block renderers. */
public final class TemperatureScreen {
    /** Width of the screen in blocks, so long readings like "-17°C" are shrunk to fit. */
    private static final float SCREEN_WIDTH = 0.66f;
    private static final float MAX_SCALE = 0.02f;

    private TemperatureScreen() {
    }

    public static void draw(TextRenderer textRenderer, float celsius, Direction facing,
                            MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        Text text = Text.literal(Math.round(celsius) + "°C");

        Vector3f tint = Temperature.colorFor(celsius);
        int color = 0xFF000000
            | (Math.min(255, (int) (tint.x * 255)) << 16)
            | (Math.min(255, (int) (tint.y * 255)) << 8)
            | Math.min(255, (int) (tint.z * 255));

        float width = textRenderer.getWidth(text);
        float scale = Math.min(MAX_SCALE, SCREEN_WIDTH / width);

        matrices.push();
        // In this frame the front of the block is +Z, like a wall sign.
        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-facing.asRotation()));
        // The screen is the upper part of the front face; sit just in front of the texture.
        matrices.translate(0.0, 0.17, 0.502);
        matrices.scale(scale, -scale, scale);

        textRenderer.draw(text, -width / 2.0f, -4.0f, color, false, matrices.peek().getPositionMatrix(),
            vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
        matrices.pop();
    }
}
