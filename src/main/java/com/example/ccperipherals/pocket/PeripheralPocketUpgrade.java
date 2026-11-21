package com.example.ccperipherals.pocket;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.pocket.AbstractPocketUpgrade;
import dan200.computercraft.api.pocket.IPocketAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/** A pocket computer upgrade that just attaches a peripheral, like the wireless modem upgrade does. */
public class PeripheralPocketUpgrade extends AbstractPocketUpgrade {
    private final Function<IPocketAccess, IPeripheral> factory;

    public PeripheralPocketUpgrade(Identifier id, ItemStack craftingItem, Function<IPocketAccess, IPeripheral> factory) {
        super(id, craftingItem);
        this.factory = factory;
    }

    @Nullable
    @Override
    public IPeripheral createPeripheral(IPocketAccess access) {
        return factory.apply(access);
    }
}
