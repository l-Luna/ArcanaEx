package arcana.items;

import arcana.aura.AuraWorld;
import arcana.network.PkShakeNode;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvironmentInterface;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

@EnvironmentInterface(value = EnvType.CLIENT, itf = PosableItem.class)
public class ScalpelItem extends Item implements PosableItem{
	
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
		return 30;
	}
	
	@Environment(EnvType.CLIENT)
	public void applyPose(MatrixStack matrices, PlayerEntity player, ItemStack stack, float tickDelta, Hand hand, Arm arm){
		matrices.multiply(Vec3f.POSITIVE_Y.getRadialQuaternion(0.2f));
		float x = player.getItemUseTime() / (float)getMaxUseTime(stack);
		float of = x < 0.7 ? -x/3f
				: x <= 0.8 ? 9f*(x - 0.7f) - (0.7f/3)
				: -4*(x - 0.8f) + 0.66f;
		matrices.translate(0, 0, -of);
	}
}