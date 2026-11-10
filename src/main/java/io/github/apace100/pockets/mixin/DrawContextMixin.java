package io.github.apace100.pockets.mixin;

import io.github.apace100.pockets.PocketUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {

    @Shadow public abstract void fill(int x1, int y1, int x2, int y2, int color);

    @Inject(method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
            at = @At("TAIL"))
    private void pockets$renderPocketBar(TextRenderer textRenderer, ItemStack stack, int x, int y,
                                         String countOverride, CallbackInfo ci) {
        if (!PocketUtil.hasPockets(stack)) {
            return;
        }

        int occupancy = Math.min(64, PocketUtil.getPocketOccupancy(stack));
        if (occupancy <= 0) {
            return;
        }

        int width = Math.min(1 + 12 * occupancy / 64, 13);
        int color = MathHelper.packRgb(0.4f, 0.4f, 1.0f);
        int drawX = x + 2;
        int drawY = y + (stack.isItemBarVisible() ? 11 : 13);
        this.fill(drawX, drawY, drawX + 13, drawY + 2, 0xFF000000);
        this.fill(drawX, drawY, drawX + width, drawY + 1, color | 0xFF000000);
    }
}
