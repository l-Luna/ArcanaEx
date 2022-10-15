package arcana.items;

import arcana.entities.ThrownAlumentumEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class AlumentumItem extends Item{
	
	public AlumentumItem(Settings settings){
		super(settings);
	}
	
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
		if(user != null){
			if(!user.getAbilities().creativeMode)
				user.getStackInHand(hand).decrement(1);
			ThrownAlumentumEntity entity = new ThrownAlumentumEntity(world);
			entity.updatePosition(user.getX(), user.getEyeY(), user.getZ());
			
			float f = -MathHelper.sin(user.headYaw * .017453292f) * MathHelper.cos(user.getPitch() * 0.017453292F);
			float g = -MathHelper.sin(user.getPitch() * .017453292f);
			float h = MathHelper.cos(user.headYaw * .017453292f) * MathHelper.cos(user.getPitch() * 0.017453292F);
			
			entity.addVelocity(f * 2, g * 2, h * 2);
			world.spawnEntity(entity);
			return TypedActionResult.success(user.getStackInHand(hand));
		}
		return super.use(world, null, hand);
	}
}