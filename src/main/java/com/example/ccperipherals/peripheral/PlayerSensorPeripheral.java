package com.example.ccperipherals.peripheral;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Peripheral type {@code player_sensor}.
 *
 * <p>Events queued on attached computers (the first argument is the peripheral's name, as with
 * {@code monitor_touch}):
 * <ul>
 *   <li>{@code player_click, name, username, uuid, face}: a player right-clicked the block.</li>
 *   <li>{@code player_touch, name, username, uuid}: a player touched the block. Fires once per contact.</li>
 *   <li>{@code player_join, name, username, uuid}: a player joined the world.</li>
 *   <li>{@code player_leave, name, username, uuid}: a player left the world.</li>
 * </ul>
 *
 * <pre>
 * local _, name, user = os.pullEvent("player_click")
 * </pre>
 */
public class PlayerSensorPeripheral implements IPeripheral {
    /** A touch counts as a new contact if the player hasn't touched the block for this many ticks. */
    private static final int TOUCH_GAP_TICKS = 5;

    /** Sensors that currently have at least one computer attached. Used to deliver join/leave events. */
    private static final Set<PlayerSensorPeripheral> ACTIVE = ConcurrentHashMap.newKeySet();

    private final Locator locator;
    private final Set<IComputerAccess> computers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Long> lastTouch = new HashMap<>();

    public PlayerSensorPeripheral(Locator locator) {
        this.locator = locator;
    }

    @Override
    public String getType() {
        return "player_sensor";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof PlayerSensorPeripheral peripheral && peripheral.locator.owner() == locator.owner();
    }

    @Override
    public void attach(IComputerAccess computer) {
        computers.add(computer);
        ACTIVE.add(this);
    }

    @Override
    public void detach(IComputerAccess computer) {
        computers.remove(computer);
        if (computers.isEmpty()) ACTIVE.remove(this);
    }

    // ---- Events (called from the game, on the server thread) ----

    /** A player right-clicked the sensor block. */
    public void onClick(ServerPlayerEntity player, String face) {
        sendEvent("player_click", player.getGameProfile().getName(), player.getUuidAsString(), face);
    }

    /** A player is touching the sensor block. Only the start of each contact fires an event. */
    public void onTouch(ServerPlayerEntity player, long gameTime) {
        Long last = lastTouch.put(player.getUuid(), gameTime);
        if (last == null || gameTime - last > TOUCH_GAP_TICKS) {
            sendEvent("player_touch", player.getGameProfile().getName(), player.getUuidAsString());
        }

        // Forget players who haven't touched for a while so the map can't grow forever.
        if (lastTouch.size() > 64) lastTouch.values().removeIf(t -> gameTime - t > 200);
    }

    /** Sends {@code event} (with the player's name and UUID) to every active sensor. */
    public static void broadcast(String event, ServerPlayerEntity player) {
        String name = player.getGameProfile().getName();
        String uuid = player.getUuidAsString();
        for (PlayerSensorPeripheral sensor : ACTIVE) sensor.sendEvent(event, name, uuid);
    }

    private void sendEvent(String event, Object... args) {
        for (IComputerAccess computer : computers) {
            Object[] full = new Object[args.length + 1];
            full[0] = computer.getAttachmentName();
            System.arraycopy(args, 0, full, 1, args.length);
            computer.queueEvent(event, full);
        }
    }

    // ---- Lua API ----

    /** @return The usernames of all players currently online. */
    @LuaFunction(mainThread = true)
    public final String[] getPlayers() throws LuaException {
        return serverWorld().getServer().getPlayerManager().getPlayerList().stream()
            .map(p -> p.getGameProfile().getName())
            .toArray(String[]::new);
    }

    /** @return true if a player with this username is online. */
    @LuaFunction(mainThread = true)
    public final boolean isPlayerOnline(String username) throws LuaException {
        return serverWorld().getServer().getPlayerManager().getPlayer(username) != null;
    }

    private ServerWorld serverWorld() throws LuaException {
        if (locator.world() instanceof ServerWorld world) return world;
        throw new LuaException("Player sensor is not in a loaded world");
    }
}
