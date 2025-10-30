package arcana.items.creative;

import arcana.aura.AuraChunk;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FluxSpongeItem extends Item{
	
	public FluxSpongeItem(Settings settings){
		super(settings);
	}
	
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
		BlockPos here = user.getBlockPos();
		for(int x = -2; x < 3; x++)
			for(int z = -2; z < 3; z++){
				AuraChunk ac = AuraChunk.from(world, here.add(x * 16, 0, z * 16));
				if(ac != null)
					ac.setFlux(0);
			}
		return TypedActionResult.success(user.getStackInHand(hand));
	}
}