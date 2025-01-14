package arcana.mixin;

import arcana.components.CaArrow;
import arcana.items.CrimsonLongbowItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(BowItem.class)
public class BowItemMixin{
	
	@ModifyArgs(method = "onStoppedUsing",
	            at = @At(value = "INVOKE",
	                    target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
	void onStoppedUsing(Args args, ItemStack stack, World _1, LivingEntity _2, int _3){
		if(args.get(0) instanceof ArrowEntity ae && stack.getItem() instanceof CrimsonLongbowItem){
			CaArrow.setProjected(ae, true);
		}
	}
}