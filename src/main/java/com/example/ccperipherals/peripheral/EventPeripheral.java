package com.example.ccperipherals.peripheral;

import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base for peripherals that queue events on the computers they are attached to. Events follow the
 * usual CC convention: the first argument after the event name is the peripheral's name.
 */
public abstract class EventPeripheral implements IPeripheral {
    private final Set<IComputerAccess> computers = ConcurrentHashMap.newKeySet();

    @Override
    public void attach(IComputerAccess computer) {
        computers.add(computer);
    }

    @Override
    public void detach(IComputerAccess computer) {
        computers.remove(computer);
    }

    /** Queue {@code event} on every attached computer as: event, peripheral name, args... */
    protected void sendEvent(String event, Object... args) {
        for (IComputerAccess computer : computers) {
            Object[] full = new Object[args.length + 1];
            full[0] = computer.getAttachmentName();
            System.arraycopy(args, 0, full, 1, args.length);
            computer.queueEvent(event, full);
        }
    }
}
