package io.github.apace100.pockets.mixin;

import io.github.apace100.pockets.PocketUtil;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BundleContentsComponent.class)
public abstract class BundleContentsComponentMixin {

    @Inject(method = "getOccupancy(Lnet/minecraft/item/ItemStack;)Lorg/apache/commons/lang3/math/Fraction;",
            at = @At("HEAD"), cancellable = true)
    private static void pockets$includeNestedPocketContents(ItemStack stack,
                                                             CallbackInfoReturnable<Fraction> cir) {
        if (PocketUtil.hasPockets(stack)) {
            int itemWeight = Math.max(1, 64 / stack.getMaxCount());
            int totalWeight = itemWeight + PocketUtil.getPocketOccupancy(stack);
            cir.setReturnValue(Fraction.getFraction(totalWeight, 64));
        }
    }
}
