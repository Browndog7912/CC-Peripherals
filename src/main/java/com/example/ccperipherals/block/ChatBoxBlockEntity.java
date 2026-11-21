package com.example.ccperipherals.block;

import com.example.ccperipherals.CCPeripherals;
import com.example.ccperipherals.peripheral.ChatBoxPeripheral;
import com.example.ccperipherals.peripheral.Locator;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class ChatBoxBlockEntity extends BlockEntity {
    private final ChatBoxPeripheral peripheral = new ChatBoxPeripheral(Locator.ofBlockEntity(this));

    public ChatBoxBlockEntity(BlockPos pos, BlockState state) {
        super(CCPeripherals.CHAT_BOX_ENTITY, pos, state);
    }

    public ChatBoxPeripheral getPeripheral() {
        return peripheral;
    }
}
