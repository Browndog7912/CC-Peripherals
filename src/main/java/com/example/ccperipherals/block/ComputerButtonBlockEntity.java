package com.example.ccperipherals.block;

import com.example.ccperipherals.CCPeripherals;
import com.example.ccperipherals.peripheral.ButtonPeripheral;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class ComputerButtonBlockEntity extends BlockEntity {
    private final ButtonPeripheral peripheral = new ButtonPeripheral(this);

    public ComputerButtonBlockEntity(BlockPos pos, BlockState state) {
        super(CCPeripherals.COMPUTER_BUTTON_ENTITY, pos, state);
    }

    public ButtonPeripheral getPeripheral() {
        return peripheral;
    }
}
