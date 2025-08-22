package arcana.items;

import arcana.aura.AuraWorld;
import arcana.network.PkShakeNode;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class ScalpelItem extends Item{
	
	public enum ScalpelType{
		ROSE,
		SILVER,
		BLACK
	}
	
	public final ScalpelType type;
	
	public ScalpelItem(Settings settings, ScalpelType type){
		super(settings.maxDamageIfAbsent(100));
		this.type = type;
	}
	
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
		user.setCurrentHand(hand);
		return TypedActionResult.consume(user.getStackInHand(hand));
	}
	
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user){
		if(!world.isClient)
			AuraWorld.from(world).raycastNodes(user, false).ifPresent(node -> {
				if(type == ScalpelType.BLACK){
					node.destroy(true); // TODO
				}else{
					Random rng = world.random;
					new PkShakeNode(node, 40).sendToAllWatching(user);
					boolean degrade = type == ScalpelType.ROSE || rng.nextInt(4) == 0;
					node.damage(degrade, rng);
				}
				stack.damage(1, user, e -> e.sendToolBreakStatus(e.getActiveHand()));
			});
		return stack;
	}
	
	public int getMaxUseTime(ItemStack stack){
		return 2 * 20;
	}
	
	public UseAction getUseAction(ItemStack stack){
		// TODO: custom pose for scalpels
		return UseAction.SPEAR;
	}
}