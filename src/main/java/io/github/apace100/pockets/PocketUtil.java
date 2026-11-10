package io.github.apace100.pockets;

import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.component.type.NbtComponent;
import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class PocketUtil {

    private PocketUtil() {
    }

    public static boolean isAllowedInPockets(ItemStack stack) {
        return !stack.isIn(Pockets.BLACKLIST_TAG);
    }

    public static boolean hasPockets(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem && stack.isIn(Pockets.HAS_POCKET_TAG);
    }

    /**
     * Migrates the old 1.20.1 item-tag storage without discarding the legacy data
     * when any nested stack cannot be decoded in the current registry context.
     */
    public static void migrateLegacyContents(ItemStack stack, World world) {
        if (!hasPockets(stack) || !(world instanceof ServerWorld serverWorld)) {
            return;
        }
        NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null) {
            return;
        }

        NbtCompound legacyData = customData.copyNbt();
        if (!(legacyData.get("Items") instanceof NbtList legacyItems)) {
            return;
        }

        List<ItemStack> decodedStacks = new ArrayList<>(legacyItems.size());
        for (NbtElement element : legacyItems) {
            if (!(element instanceof NbtCompound itemNbt)) {
                return;
            }

            NbtCompound updatedItemNbt;
            try {
                // Pockets 1.20.1 stored nested stacks using the legacy item NBT shape.
                Object fixedValue = serverWorld.getServer().getDataFixer()
                        .update(TypeReferences.ITEM_STACK, new Dynamic<>(NbtOps.INSTANCE, itemNbt), 3465,
                                SharedConstants.getGameVersion().getSaveVersion().getId())
                        .getValue();
                if (!(fixedValue instanceof NbtCompound fixedItemNbt)) {
                    return;
                }
                updatedItemNbt = fixedItemNbt;
            } catch (RuntimeException exception) {
                return;
            }

            Optional<ItemStack> decoded = ItemStack.fromNbt(serverWorld.getRegistryManager(), updatedItemNbt);
            if (decoded.isEmpty() || decoded.get().isEmpty()) {
                return;
            }
            decodedStacks.add(decoded.get());
        }

        List<ItemStack> allStacks = new ArrayList<>(getPocketedStacks(stack).toList());
        allStacks.addAll(decodedStacks);
        setPocketedStacks(stack, allStacks);

        legacyData.remove("Items");
        if (legacyData.isEmpty()) {
            stack.remove(DataComponentTypes.CUSTOM_DATA);
        } else {
            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(legacyData));
        }
    }

    public static int addToPockets(ItemStack pocketItem, ItemStack stack) {
        if (stack.isEmpty() || !stack.getItem().canBeNested()) {
            return 0;
        }

        int occupied = getPocketOccupancy(pocketItem);
        int itemOccupancy = getItemOccupancy(stack);
        if (itemOccupancy <= 0 || occupied >= 64) {
            return 0;
        }

        int amount = Math.min(stack.getCount(), (64 - occupied) / itemOccupancy);
        if (amount <= 0) {
            return 0;
        }

        List<ItemStack> items = new ArrayList<>(getPocketedStacks(pocketItem).toList());
        Optional<Integer> mergeIndex = findMergeIndex(stack, items);
        if (mergeIndex.isPresent()) {
            ItemStack merged = items.remove((int) mergeIndex.get());
            merged.increment(amount);
            items.add(0, merged);
        } else {
            items.add(0, stack.copyWithCount(amount));
        }

        setPocketedStacks(pocketItem, items);
        return amount;
    }

    private static Optional<Integer> findMergeIndex(ItemStack stack, List<ItemStack> items) {
        if (stack.isOf(Items.BUNDLE)) {
            return Optional.empty();
        }

        for (int i = 0; i < items.size(); i++) {
            if (ItemStack.areItemsAndComponentsEqual(items.get(i), stack)) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    private static int getItemOccupancy(ItemStack stack) {
        if (hasPockets(stack)) {
            return Math.max(1, 64 / stack.getMaxCount()) + getPocketOccupancy(stack);
        }
        if (stack.isOf(Items.BUNDLE)) {
            return 4 + getPocketOccupancy(stack);
        }
        if ((stack.isOf(Items.BEEHIVE) || stack.isOf(Items.BEE_NEST))
                && stack.contains(DataComponentTypes.BEES)
                && !stack.get(DataComponentTypes.BEES).isEmpty()) {
            return 64;
        }

        return Math.max(1, 64 / stack.getMaxCount());
    }

    public static int getPocketOccupancy(ItemStack stack) {
        return getContainedStacks(stack)
                .mapToInt(itemStack -> getItemOccupancy(itemStack) * itemStack.getCount())
                .sum();
    }

    private static Stream<ItemStack> getContainedStacks(ItemStack stack) {
        if (hasPockets(stack)) {
            return getPocketedStacks(stack);
        }
        if (stack.isOf(Items.BUNDLE)) {
            return stack.getOrDefault(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT).stream();
        }
        return Stream.empty();
    }

    public static BundleContentsComponent getPocketTooltipData(ItemStack stack) {
        return stack.getOrDefault(Pockets.POCKET_CONTENTS, BundleContentsComponent.DEFAULT);
    }

    public static Stream<ItemStack> getPocketedStacks(ItemStack stack) {
        BundleContentsComponent contents = stack.get(Pockets.POCKET_CONTENTS);
        if (contents == null) {
            return Stream.empty();
        }
        return contents.stream();
    }

    private static void setPocketedStacks(ItemStack stack, List<ItemStack> items) {
        if (items.isEmpty()) {
            stack.remove(Pockets.POCKET_CONTENTS);
            return;
        }

        List<ItemStack> copies = items.stream().map(ItemStack::copy).toList();
        stack.set(Pockets.POCKET_CONTENTS, new BundleContentsComponent(copies));
    }

    public static Optional<ItemStack> removeFirstStack(ItemStack stack) {
        List<ItemStack> items = new ArrayList<>(getPocketedStacks(stack).toList());
        if (items.isEmpty()) {
            return Optional.empty();
        }

        ItemStack removed = items.remove(0);
        setPocketedStacks(stack, items);
        return Optional.of(removed);
    }

    public static boolean dropAllPocketedItems(ItemStack stack, PlayerEntity player) {
        List<ItemStack> items = getPocketedStacks(stack).toList();
        if (items.isEmpty()) {
            return false;
        }

        if (player instanceof ServerPlayerEntity) {
            for (ItemStack itemStack : items) {
                player.dropItem(itemStack, true, true);
            }
        }
        stack.remove(Pockets.POCKET_CONTENTS);
        return true;
    }

}
