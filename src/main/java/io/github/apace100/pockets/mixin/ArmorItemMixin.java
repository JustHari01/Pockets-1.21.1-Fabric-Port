package io.github.apace100.pockets.mixin;

import io.github.apace100.pockets.PocketUtil;
import io.github.apace100.pockets.SoundUtil;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;
import java.util.Optional;

@Mixin(ArmorItem.class)
public abstract class ArmorItemMixin extends Item {

    public ArmorItemMixin(Settings settings) {
        super(settings);
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType,
                             PlayerEntity player, StackReference cursorStackReference) {
        if (clickType != ClickType.RIGHT || !slot.canTakePartial(player)) {
            return false;
        }

        PocketUtil.migrateLegacyContents(stack, player.getWorld());
        boolean pocketable = PocketUtil.hasPockets(stack) && PocketUtil.isAllowedInPockets(otherStack);
        if (otherStack.isEmpty()) {
            PocketUtil.removeFirstStack(stack).ifPresent(itemStack -> {
                SoundUtil.playRemoveOneSound(player);
                cursorStackReference.set(itemStack);
            });
        } else if (pocketable) {
            PocketUtil.migrateLegacyContents(otherStack, player.getWorld());
            int amount = PocketUtil.addToPockets(stack, otherStack);
            if (amount > 0) {
                SoundUtil.playInsertSound(player);
                otherStack.decrement(amount);
            }
        }
        return pocketable;
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        if (!PocketUtil.hasPockets(stack)) {
            return Optional.empty();
        }
        return Optional.of(PocketUtil.getPocketTooltipData(stack));
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (PocketUtil.hasPockets(stack)) {
            tooltip.add(Text.translatable("item.minecraft.bundle.fullness", PocketUtil.getPocketOccupancy(stack), 64)
                    .formatted(Formatting.GRAY));
        }
    }

    @Override
    public void onItemEntityDestroyed(ItemEntity entity) {
        ItemStack stack = entity.getStack();
        if (!PocketUtil.hasPockets(stack)) {
            return;
        }

        PocketUtil.migrateLegacyContents(stack, entity.getWorld());
        SoundUtil.playDropContentsSound(entity);
        ItemUsage.spawnItemContents(entity, PocketUtil.getPocketedStacks(stack).toList());
        stack.remove(io.github.apace100.pockets.Pockets.POCKET_CONTENTS);
    }
}
