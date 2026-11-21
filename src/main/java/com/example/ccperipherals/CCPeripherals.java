package com.example.ccperipherals;

import com.example.ccperipherals.block.ChatBoxBlock;
import com.example.ccperipherals.block.ChatBoxBlockEntity;
import com.example.ccperipherals.block.EnvironmentSensorBlock;
import com.example.ccperipherals.block.EnvironmentSensorBlockEntity;
import com.example.ccperipherals.block.ComputerButtonBlock;
import com.example.ccperipherals.block.ComputerButtonBlockEntity;
import com.example.ccperipherals.block.ComputerLightBlock;
import com.example.ccperipherals.block.ComputerLightBlockEntity;
import com.example.ccperipherals.block.NfcReaderBlock;
import com.example.ccperipherals.block.NfcReaderBlockEntity;
import com.example.ccperipherals.block.NfcScannerBlock;
import com.example.ccperipherals.block.NfcScannerBlockEntity;
import com.example.ccperipherals.block.PlayerSensorBlock;
import com.example.ccperipherals.block.PlayerSensorBlockEntity;
import com.example.ccperipherals.block.WeatherSensorBlock;
import com.example.ccperipherals.block.WeatherSensorBlockEntity;
import com.example.ccperipherals.nfc.NfcCardItem;
import com.example.ccperipherals.nfc.NfcColor;
import com.example.ccperipherals.peripheral.ChatBoxPeripheral;
import com.example.ccperipherals.peripheral.Locator;
import com.example.ccperipherals.peripheral.PlayerSensorPeripheral;
import com.example.ccperipherals.peripheral.WeatherSensorPeripheral;
import com.example.ccperipherals.pocket.PeripheralPocketUpgrade;
import dan200.computercraft.api.peripheral.PeripheralLookup;
import dan200.computercraft.api.pocket.IPocketUpgrade;
import dan200.computercraft.api.upgrades.UpgradeSerialiser;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.sound.BlockSoundGroup;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.EnumMap;
import java.util.Map;

public class CCPeripherals implements ModInitializer {
    public static final String MOD_ID = "ccperipherals";

    public static final Block CHAT_BOX = new ChatBoxBlock();
    public static final Block WEATHER_SENSOR = new WeatherSensorBlock();
    public static final Block ENVIRONMENT_SENSOR = new EnvironmentSensorBlock();
    public static final Block PLAYER_SENSOR = new PlayerSensorBlock();
    public static final Block NFC_READER = new NfcReaderBlock();
    public static final Block NFC_SCANNER = new NfcScannerBlock();
    public static final Block COMPUTER_LIGHT = new ComputerLightBlock();
    public static final Block COMPUTER_BUTTON = new ComputerButtonBlock();

    /** One NFC card item per colour. */
    public static final Map<NfcColor, Item> NFC_CARDS = new EnumMap<>(NfcColor.class);
    static {
        for (NfcColor color : NfcColor.values()) NFC_CARDS.put(color, new NfcCardItem(color));
    }

    /** Decorative block with no behaviour, for building computer rooms. */
    public static final Block COMPUTER_CASING = new Block(FabricBlockSettings.create()
        .mapColor(MapColor.IRON_GRAY)
        .strength(2.0f, 6.0f)
        .sounds(BlockSoundGroup.METAL));

    /** Decorative block with no behaviour. Looks like metal but sounds like stone. */
    public static final Block COMPUTER_METAL = new Block(FabricBlockSettings.create()
        .mapColor(MapColor.LIGHT_GRAY)
        .strength(2.0f, 6.0f)
        .sounds(BlockSoundGroup.STONE));

    public static BlockEntityType<ChatBoxBlockEntity> CHAT_BOX_ENTITY;
    public static BlockEntityType<WeatherSensorBlockEntity> WEATHER_SENSOR_ENTITY;
    public static BlockEntityType<EnvironmentSensorBlockEntity> ENVIRONMENT_SENSOR_ENTITY;
    public static BlockEntityType<PlayerSensorBlockEntity> PLAYER_SENSOR_ENTITY;
    public static BlockEntityType<NfcReaderBlockEntity> NFC_READER_ENTITY;
    public static BlockEntityType<NfcScannerBlockEntity> NFC_SCANNER_ENTITY;
    public static BlockEntityType<ComputerLightBlockEntity> COMPUTER_LIGHT_ENTITY;
    public static BlockEntityType<ComputerButtonBlockEntity> COMPUTER_BUTTON_ENTITY;

    public static final ItemGroup CREATIVE_TAB = FabricItemGroup.builder()
        .icon(() -> new ItemStack(CHAT_BOX))
        .displayName(Text.translatable("itemGroup." + MOD_ID + ".main"))
        .entries((context, entries) -> {
            entries.add(CHAT_BOX);
            entries.add(WEATHER_SENSOR);
            entries.add(ENVIRONMENT_SENSOR);
            entries.add(PLAYER_SENSOR);
            entries.add(NFC_READER);
            entries.add(NFC_SCANNER);
            entries.add(COMPUTER_LIGHT);
            entries.add(COMPUTER_BUTTON);
            NFC_CARDS.values().forEach(entries::add);
            entries.add(COMPUTER_CASING);
            entries.add(COMPUTER_METAL);
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
        registerBlock("environment_sensor", ENVIRONMENT_SENSOR);
        registerBlock("player_sensor", PLAYER_SENSOR);
        registerBlock("nfc_reader", NFC_READER);
        registerBlock("nfc_scanner", NFC_SCANNER);
        registerBlock("computer_light", COMPUTER_LIGHT);
        registerBlock("computer_button", COMPUTER_BUTTON);
        NFC_CARDS.forEach((color, item) -> Registry.register(Registries.ITEM, id("nfc_card_" + color.id()), item));
        registerBlock("computer_casing", COMPUTER_CASING);
        registerBlock("computer_metal", COMPUTER_METAL);

        // Block entities
        CHAT_BOX_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("chat_box"),
            FabricBlockEntityTypeBuilder.create(ChatBoxBlockEntity::new, CHAT_BOX).build());
        WEATHER_SENSOR_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("weather_sensor"),
            FabricBlockEntityTypeBuilder.create(WeatherSensorBlockEntity::new, WEATHER_SENSOR).build());
        ENVIRONMENT_SENSOR_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("environment_sensor"),
            FabricBlockEntityTypeBuilder.create(EnvironmentSensorBlockEntity::new, ENVIRONMENT_SENSOR).build());
        PLAYER_SENSOR_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("player_sensor"),
            FabricBlockEntityTypeBuilder.create(PlayerSensorBlockEntity::new, PLAYER_SENSOR).build());
        NFC_READER_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("nfc_reader"),
            FabricBlockEntityTypeBuilder.create(NfcReaderBlockEntity::new, NFC_READER).build());
        NFC_SCANNER_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("nfc_scanner"),
            FabricBlockEntityTypeBuilder.create(NfcScannerBlockEntity::new, NFC_SCANNER).build());
        COMPUTER_LIGHT_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("computer_light"),
            FabricBlockEntityTypeBuilder.create(ComputerLightBlockEntity::new, COMPUTER_LIGHT).build());
        COMPUTER_BUTTON_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("computer_button"),
            FabricBlockEntityTypeBuilder.create(ComputerButtonBlockEntity::new, COMPUTER_BUTTON).build());

        // Creative tab
        Registry.register(Registries.ITEM_GROUP, id("main"), CREATIVE_TAB);

        // Expose the block entities to CC: Tweaked computers as peripherals.
        PeripheralLookup.get().registerForBlockEntity((be, side) -> be.getPeripheral(), CHAT_BOX_ENTITY);
        PeripheralLookup.get().registerForBlockEntity((be, side) -> be.getPeripheral(), WEATHER_SENSOR_ENTITY);
        PeripheralLookup.get().registerForBlockEntity((be, side) -> be.getPeripheral(), ENVIRONMENT_SENSOR_ENTITY);
        PeripheralLookup.get().registerForBlockEntity((be, side) -> be.getPeripheral(), PLAYER_SENSOR_ENTITY);
        PeripheralLookup.get().registerForBlockEntity((be, side) -> be.getPeripheral(), NFC_READER_ENTITY);
        PeripheralLookup.get().registerForBlockEntity((be, side) -> be.getPeripheral(), NFC_SCANNER_ENTITY);
        PeripheralLookup.get().registerForBlockEntity((be, side) -> be.getPeripheral(), COMPUTER_LIGHT_ENTITY);
        PeripheralLookup.get().registerForBlockEntity((be, side) -> be.getPeripheral(), COMPUTER_BUTTON_ENTITY);

        // Player Sensor join/leave events go to every sensor that has a computer attached.
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
            PlayerSensorPeripheral.broadcast("player_join", handler.getPlayer()));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
            PlayerSensorPeripheral.broadcast("player_leave", handler.getPlayer()));

        registerPocketUpgrades();
    }

    /**
     * Pocket computer upgrades. Craft a pocket computer with the Chat Box or Weather Sensor item to
     * get an upgraded pocket computer; the peripheral then appears on its "back" side.
     * The upgrades themselves are defined in data/ccperipherals/computercraft/pocket_upgrades/.
     */
    private static void registerPocketUpgrades() {
        Registry<UpgradeSerialiser<? extends IPocketUpgrade>> registry = pocketUpgradeSerialisers();

        Registry.register(registry, id("chat_box"),
            UpgradeSerialiser.<PeripheralPocketUpgrade>simpleWithCustomItem((upgradeId, stack) ->
                new PeripheralPocketUpgrade(upgradeId, stack, access -> new ChatBoxPeripheral(Locator.ofPocket(access)))));

        Registry.register(registry, id("weather_sensor"),
            UpgradeSerialiser.<PeripheralPocketUpgrade>simpleWithCustomItem((upgradeId, stack) ->
                new PeripheralPocketUpgrade(upgradeId, stack, access -> new WeatherSensorPeripheral(Locator.ofPocket(access)))));
    }

    /** CC: Tweaked's registry of pocket upgrade serialisers, which it creates before this mod loads. */
    @SuppressWarnings("unchecked")
    private static Registry<UpgradeSerialiser<? extends IPocketUpgrade>> pocketUpgradeSerialisers() {
        Identifier registryId = IPocketUpgrade.serialiserRegistryKey().getValue();
        Object registry = Registries.REGISTRIES.get(registryId);
        if (registry == null) {
            throw new IllegalStateException("CC: Tweaked's pocket upgrade serialiser registry (" + registryId
                + ") was not found. Is CC: Tweaked installed?");
        }
        return (Registry<UpgradeSerialiser<? extends IPocketUpgrade>>) registry;
    }

    private static void registerBlock(String name, Block block) {
        Registry.register(Registries.BLOCK, id(name), block);
        Registry.register(Registries.ITEM, id(name), new BlockItem(block, new FabricItemSettings()));
    }
}
