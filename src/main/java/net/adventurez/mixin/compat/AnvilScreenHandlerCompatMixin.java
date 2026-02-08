package net.adventurez.mixin.compat;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.adventurez.init.ItemInit;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;

@Mixin(value = AnvilScreenHandler.class, priority = 1500)
public abstract class AnvilScreenHandlerCompatMixin extends ForgingScreenHandler {

    @Unique
    private int adventurez$repairedAmount;

    @Unique
    private boolean adventurez$isPrimeEyeRepair;

    @Shadow
    @Mutable
    @Final
    private Property levelCost;

    @Unique
    @Final
    protected Inventory input;

    @Unique
    @Final
    protected Inventory output;

    public AnvilScreenHandlerCompatMixin(ScreenHandlerType<?> type, int syncId,
                                         PlayerInventory playerInventory,
                                         ScreenHandlerContext context) {
        super(type, syncId, playerInventory, context);
    }

    /**
     * CLAVE: Usar @At("RETURN") en lugar de @At("HEAD")
     * CLAVE: NO capturar variables locales (sin parámetros extras en el método)
     * CLAVE: require = 0 para que sea opcional
     */
    @Inject(
            method = "updateResult",
            at = @At("RETURN"),
            require = 0
    )
    private void adventurez$handlePrimeEyeRepair(CallbackInfo ci) {
        adventurez$isPrimeEyeRepair = false;

        ItemStack leftInput = this.input.getStack(0);
        ItemStack rightInput = this.input.getStack(1);

        if (leftInput.getItem() == ItemInit.PRIME_EYE &&
                leftInput.getDamage() > 0 &&
                rightInput.getItem() == Items.ENDER_PEARL) {

            ItemStack result = new ItemStack(ItemInit.PRIME_EYE);
            int damage = leftInput.getDamage();
            int pearlCount = rightInput.getCount();

            adventurez$repairedAmount = Math.min(pearlCount, damage);
            result.setDamage(damage - adventurez$repairedAmount);

            this.output.setStack(0, result);
            this.levelCost.set(0);

            adventurez$isPrimeEyeRepair = true;
        }
    }

    @Inject(
            method = "canTakeOutput",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void adventurez$allowPrimeEyeTake(PlayerEntity player, boolean present,
                                              CallbackInfoReturnable<Boolean> cir) {
        if (adventurez$isPrimeEyeRepair) {
            cir.setReturnValue(true);
        }
    }

    @Inject(
            method = "onTakeOutput",
            at = @At("HEAD"),
            require = 0
    )
    private void adventurez$consumePearls(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if (adventurez$isPrimeEyeRepair && adventurez$repairedAmount > 0) {
            ItemStack rightInput = this.input.getStack(1);
            if (rightInput.getItem() == Items.ENDER_PEARL) {
                rightInput.decrement(adventurez$repairedAmount);
            }
            adventurez$repairedAmount = 0;
            adventurez$isPrimeEyeRepair = false;
        }
    }
}
