package com.example.ccperipherals.peripheral;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Peripheral type {@code chat_box}.
 *
 * <pre>
 * local chat = peripheral.find("chat_box")
 * chat.sendMessage("Hello world!")
 * chat.sendMessageToPlayer("Psst", "Steve", "Secret")
 * </pre>
 */
public class ChatBoxPeripheral implements IPeripheral {
    /** Minimum ticks between messages (20 ticks = 1 second). Stops computers spamming chat. */
    private static final int COOLDOWN_TICKS = 10;
    private static final int MAX_MESSAGE_LENGTH = 256;
    private static final int MAX_PREFIX_LENGTH = 32;
    private static final String DEFAULT_PREFIX = "ChatBox";

    private final Locator locator;
    private long lastSentTick = Long.MIN_VALUE;

    public ChatBoxPeripheral(Locator locator) {
        this.locator = locator;
    }

    @Override
    public String getType() {
        return "chat_box";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof ChatBoxPeripheral peripheral && peripheral.locator.owner() == locator.owner();
    }

    /**
     * Sends a message to every player on the server.
     *
     * @param message The message to send.
     * @param prefix  Optional label shown in front of the message. Defaults to "ChatBox". Pass "" for none.
     * @return true if sent, or false and a reason (e.g. when on cooldown).
     */
    @LuaFunction(mainThread = true)
    public final MethodResult sendMessage(String message, Optional<String> prefix) throws LuaException {
        ServerWorld world = serverWorld();
        MethodResult check = checkSend(world, message);
        if (check != null) return check;

        MinecraftServer server = world.getServer();
        server.getPlayerManager().broadcast(format(message, prefix.orElse(DEFAULT_PREFIX)), false);
        return MethodResult.of(true);
    }

    /**
     * Sends a message to one player only.
     *
     * @param message The message to send.
     * @param player  Name of the (online) player.
     * @param prefix  Optional label shown in front of the message. Defaults to "ChatBox". Pass "" for none.
     * @return true if sent, or false and a reason (cooldown, player not online).
     */
    @LuaFunction(mainThread = true)
    public final MethodResult sendMessageToPlayer(String message, String player, Optional<String> prefix) throws LuaException {
        ServerWorld world = serverWorld();
        MethodResult check = checkSend(world, message);
        if (check != null) return check;

        ServerPlayerEntity target = world.getServer().getPlayerManager().getPlayer(player);
        if (target == null) {
            // Not counted against the cooldown because nothing was sent.
            lastSentTick = Long.MIN_VALUE;
            return MethodResult.of(false, "Player not online");
        }

        target.sendMessage(format(message, prefix.orElse(DEFAULT_PREFIX)), false);
        return MethodResult.of(true);
    }

    /** @return The names of all players currently online. */
    @LuaFunction(mainThread = true)
    public final String[] getPlayers() throws LuaException {
        return serverWorld().getServer().getPlayerManager().getPlayerList().stream()
            .map(p -> p.getName().getString())
            .toArray(String[]::new);
    }

    private ServerWorld serverWorld() throws LuaException {
        if (locator.world() instanceof ServerWorld world) return world;
        throw new LuaException("Chat box is not in a loaded world");
    }

    /** Validates length and cooldown. Returns a failure result, or null when the message may be sent. */
    @Nullable
    private MethodResult checkSend(ServerWorld world, String message) {
        if (message.isEmpty()) return MethodResult.of(false, "Message is empty");
        if (message.length() > MAX_MESSAGE_LENGTH) {
            return MethodResult.of(false, "Message too long (max " + MAX_MESSAGE_LENGTH + " characters)");
        }

        long now = world.getTime();
        if (lastSentTick != Long.MIN_VALUE && now - lastSentTick < COOLDOWN_TICKS) {
            return MethodResult.of(false, "On cooldown");
        }
        lastSentTick = now;
        return null;
    }

    private static Text format(String message, String prefix) {
        if (prefix.length() > MAX_PREFIX_LENGTH) prefix = prefix.substring(0, MAX_PREFIX_LENGTH);

        MutableText text = Text.empty();
        if (!prefix.isEmpty()) {
            text.append(Text.literal("[" + prefix + "] ").formatted(Formatting.AQUA));
        }
        return text.append(Text.literal(message));
    }
}
