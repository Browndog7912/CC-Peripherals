package com.example.ccperipherals;

import com.example.ccperipherals.block.ChatBoxBlock;
import com.example.ccperipherals.block.ChatBoxBlockEntity;
import com.example.ccperipherals.block.WeatherSensorBlock;
import com.example.ccperipherals.block.WeatherSensorBlockEntity;
import dan200.computercraft.api.peripheral.PeripheralLookup;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class CCPeripherals implements ModInitializer {
    public static final String MOD_ID = "ccperipherals";

    public static final Block CHAT_BOX = new ChatBoxBlock();
    public static final Block WEATHER_SENSOR = new WeatherSensorBlock();

    public static BlockEntityType<ChatBoxBlockEntity> CHAT_BOX_ENTITY;
    public static BlockEntityType<WeatherSensorBlockEntity> WEATHER_SENSOR_ENTITY;

    public static final ItemGroup CREATIVE_TAB = FabricItemGroup.builder()
        .icon(() -> new ItemStack(CHAT_BOX))
        .displayName(Text.translatable("itemGroup." + MOD_ID + ".main"))
        .entries((context, entries) -> {
            entries.add(CHAT_BOX);
            entries.add(WEATHER_SENSOR);
        })
        .build();

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        // Blocks + their items
        registerBlock("chat_box", CHAT_BOX);
        registerBlock("weather_sensor", WEATHER_SENSOR);

        // Block entities
        CHAT_BOX_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("chat_box"),
            FabricBlockEntityTypeBuilder.create(ChatBoxBlockEntity::new, CHAT_BOX).build());
        WEATHER_SENSOR_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("weather_sensor"),
            FabricBlockEntityTypeBuilder.create(WeatherSensorBlockEntity::new, WEATHER_SENSOR).build());

        // Creative tab
        Registry.register(Registries.ITEM_GROUP, id("main"), CREATIVE_TAB);

        // Expose the block entities to CC: Tweaked computers as peripherals.
        PeripheralLookup.get().registerForBlockEntity((be, side) -> be.getPeripheral(), CHAT_BOX_ENTITY);
        PeripheralLookup.get().registerForBlockEntity((be, side) -> be.getPeripheral(), WEATHER_SENSOR_ENTITY);
    }

    private static void registerBlock(String name, Block block) {
        Registry.register(Registries.BLOCK, id(name), block);
        Registry.register(Registries.ITEM, id(name), new BlockItem(block, new FabricItemSettings()));
    }
}
