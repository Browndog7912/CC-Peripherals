package com.example.ccperipherals.nfc;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NfcCardItem extends Item {
    private final NfcColor color;

    public NfcCardItem(NfcColor color) {
        super(new FabricItemSettings().maxCount(1));
        this.color = color;
    }

    public NfcColor getColor() {
        return color;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        String data = NfcCardData.getData(stack);
        tooltip.add(Text.literal(data == null ? "Blank" : "Holds " + data.length() + " characters")
            .formatted(Formatting.GRAY));
    }
}
