package com.example.ccperipherals.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.joml.Vector3f;

/** Temperature readings shared by the Thermostat and the Air Conditioner. */
public final class Temperature {
    /** Minecraft biome temperatures are about 0.0 (snowy) to 2.0 (desert, nether). This maps them to degrees C. */
    public static final float DEGREES_PER_UNIT = 25.0f;

    private Temperature() {
    }

    /** The raw Minecraft temperature at a position: the biome's, lowered a little above y=80 as vanilla does. */
    public static float rawTemperature(World world, BlockPos pos) {
        float temperature = world.getBiome(pos).value().getTemperature();
        if (pos.getY() > 80) temperature -= (pos.getY() - 80) * 0.05f / 40.0f;
        return temperature;
    }

    /** The temperature at a position in degrees Celsius. */
    public static float celsius(World world, BlockPos pos) {
        return rawTemperature(world, pos) * DEGREES_PER_UNIT;
    }

    public static double toFahrenheit(double celsius) {
        return celsius * 9.0 / 5.0 + 32.0;
    }

    /** Blue when cold, near white when mild, red when hot. */
    public static Vector3f colorFor(float celsius) {
        Vector3f cold = new Vector3f(0.25f, 0.55f, 1.0f);
        Vector3f mild = new Vector3f(0.95f, 0.97f, 1.0f);
        Vector3f hot = new Vector3f(1.0f, 0.2f, 0.12f);

        if (celsius <= 0) return cold;
        if (celsius >= 45) return hot;
        if (celsius < 20) return lerp(cold, mild, celsius / 20.0f);
        return lerp(mild, hot, (celsius - 20.0f) / 25.0f);
    }

    private static Vector3f lerp(Vector3f a, Vector3f b, float t) {
        return new Vector3f(MathHelper.lerp(t, a.x, b.x), MathHelper.lerp(t, a.y, b.y), MathHelper.lerp(t, a.z, b.z));
    }
}
