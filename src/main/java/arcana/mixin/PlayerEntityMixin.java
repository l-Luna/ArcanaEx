package arcana.mixin;

import arcana.blocks.WardedCampfireBlock;
import arcana.items.BootsOfTheTravellerItem;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity{
	
	protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world){
		super(entityType, world);
	}
	
	@Shadow public abstract HungerManager getHungerManager();
	
	@Shadow public abstract ItemStack getEquippedStack(EquipmentSlot slot);
	
	@Inject(method = "eatFood", at = @At("HEAD"))
	private void applyWardedCampfireFoodBonus(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> cir){
		if(stack.isFood() && WardedCampfireBlock.isProtected(this)){
			FoodComponent food = stack.getItem().getFoodComponent();
			getHungerManager().add(food.getHunger() / 2, food.getSaturationModifier() / 2);
		}
	}
	
	// also see: ClientPlayerEntityMixin
	@WrapMethod(method = "damage")
	private boolean applyDamage(DamageSource source, float amount, Operation<Boolean> original){
		if(source.isFromFalling()){
			if(amount > 0 && amount <= effectiveFallDamageReduction())
				return false;
			return original.call(source, amount - effectiveFallDamageReduction());
		}
		return original.call(source, amount);
	}
	
	@Unique
	private int effectiveFallDamageReduction(){
		boolean boostFromBoots = getEquippedStack(EquipmentSlot.FEET).getItem() instanceof BootsOfTheTravellerItem;
		return boostFromBoots ? 3 : 0;
	}
}