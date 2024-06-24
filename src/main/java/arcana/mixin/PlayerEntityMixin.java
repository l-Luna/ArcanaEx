package arcana.mixin;

import arcana.blocks.WardedCampfireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin{
	
	@Shadow public abstract HungerManager getHungerManager();
	
	@Inject(method = "eatFood", at = @At("HEAD"))
	private void applyWardedCampfireFoodBonus(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> cir){
		if(stack.isFood() && WardedCampfireBlock.isProtected((Entity)(Object)this)){
			FoodComponent food = stack.getItem().getFoodComponent();
			getHungerManager().add(food.getHunger() / 2, food.getSaturationModifier() / 2);
		}
	}
}