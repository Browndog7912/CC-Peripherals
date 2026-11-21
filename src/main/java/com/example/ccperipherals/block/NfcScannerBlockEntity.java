package com.example.ccperipherals.block;

import com.example.ccperipherals.CCPeripherals;
import com.example.ccperipherals.peripheral.NfcScannerPeripheral;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class NfcScannerBlockEntity extends BlockEntity {
    private final NfcScannerPeripheral peripheral = new NfcScannerPeripheral(this);

    public NfcScannerBlockEntity(BlockPos pos, BlockState state) {
        super(CCPeripherals.NFC_SCANNER_ENTITY, pos, state);
    }

    public NfcScannerPeripheral getPeripheral() {
        return peripheral;
    }
}
