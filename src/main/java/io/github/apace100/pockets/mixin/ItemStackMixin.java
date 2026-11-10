package io.github.apace100.pockets.mixin;

import io.github.apace100.pockets.PocketUtil;
import io.github.apace100.pockets.SoundUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "damage(ILnet/minecraft/server/world/ServerWorld;Lnet/minecraft/server/network/ServerPlayerEntity;Ljava/util/function/Consumer;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;"))
    private void pockets$dropContentsOnBreak(int amount, ServerWorld world, ServerPlayerEntity player,
                                             Consumer<Item> breakCallback, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        if (PocketUtil.hasPockets(stack) && player != null) {
            PocketUtil.migrateLegacyContents(stack, world);
            if (PocketUtil.dropAllPocketedItems(stack, player)) {
                SoundUtil.playDropContentsSound(player);
            }
        }
    }
}
