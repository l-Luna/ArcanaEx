package arcana.items;

import arcana.ArcanaRegistry;
import arcana.components.AuraWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.concurrent.atomic.AtomicReference;

public class ScribbledNotesItem extends Item{
	
	public ScribbledNotesItem(Settings settings){
		super(settings);
	}
	
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
		var stack = user.getStackInHand(hand);
		var ret = new AtomicReference<>(TypedActionResult.pass(stack));
		world.getComponent(AuraWorld.KEY).raycast(user.getEyePos(), 4.5, false, user).ifPresent(node -> {
			var newStack = new ItemStack(ArcanaRegistry.ARCANUM);
			System.out.println(stack);
			user.setStackInHand(hand, newStack);
			ret.set(TypedActionResult.success(newStack));
		});
		return ret.get();
	}
}