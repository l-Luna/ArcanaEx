package arcana.mixin.recipes;

import arcana.ArcanaRegistry;
import arcana.ArcanaTags;
import arcana.enchantments.ArcanaEnchantmentComponents;
import arcana.enchantments.DynamicMaxLevelsEffect;
import arcana.recipes.crafting.VoidPuttyRepairRecipe;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler{
	
	@Shadow @Final private Property levelCost;
	
	@Shadow private int repairItemUsage;
	
	public AnvilScreenHandlerMixin(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context){
		super(type, syncId, playerInventory, context);
		throw new IllegalStateException();
	}
	
	@Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
	void addAnvilRecipes(CallbackInfo ci){
		ItemStack tool = input.getStack(0);
		ItemStack material = input.getStack(1);
		if(VoidPuttyRepairRecipe.isRepairable(tool) && material.isOf(ArcanaRegistry.VOID_PUTTY)){
			levelCost.set(0);
			repairItemUsage = 1;
			ItemStack newOutput = tool.copy();
			newOutput.setDamage(0);
			output.setStack(0, newOutput);
			ci.cancel();
		}
	}
	
	@WrapOperation(method = "updateResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/Enchantment;getMaxLevel()I"))
	int capRunicShieldingLevel(Enchantment enchantment, Operation<Integer> original, @Local(ordinal = 0) RegistryEntry<Enchantment> self){
		if(self.isIn(ArcanaTags.CANT_ANVIL_COMBINE))
			return 1;
		ItemStack stack = input.getStack(0);
		int lvl = original.call(enchantment);
		if(stack.isOf(Items.ENCHANTED_BOOK))
			return lvl;
		DynamicMaxLevelsEffect levels = enchantment.effects().getOrDefault(ArcanaEnchantmentComponents.DYNAMIC_MAX_LEVELS, null);
		if(levels != null){
			int possible = levels.maxLevelTags().size();
			// start at the proposed level, walk down until a valid level is reached
			for(lvl = Math.min(lvl, possible); lvl > 0; lvl--)
				if(stack.isIn(levels.maxLevelTags().get(lvl - 1)))
					break;
		}
		return lvl;
	}
	
	@Inject(method = "canTakeOutput", at = @At("HEAD"), cancellable = true)
	void canTakeOutput(PlayerEntity player, boolean present, CallbackInfoReturnable<Boolean> cir){
		ItemStack tool = input.getStack(0);
		ItemStack material = input.getStack(1);
		if(VoidPuttyRepairRecipe.isRepairable(tool) && material.isOf(ArcanaRegistry.VOID_PUTTY))
			cir.setReturnValue(true);
	}
}