package arcana.blocks;

import arcana.ArcanaRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class ArcaneLevitatorBlockEntity extends BlockEntity{
	
	public ArcaneLevitatorBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.ARCANE_LEVITATOR_BE, pos, state);
	}
	
	public static void tick(World world, BlockPos pos, BlockState state, ArcaneLevitatorBlockEntity levitator){
		int height = 0;
		for(int i = 0; i < 5; i++){ // includes this block
			var local = pos.down(i);
			if(world.getBlockEntity(local) instanceof ArcaneLevitatorBlockEntity && !world.isReceivingRedstonePower(local))
				height += 10;
			else
				break;
		}
		// limit height at solid block
		for(int y = 1; y < height; y++)
			if(world.getBlockState(pos.up(y)).isSolidBlock(world, pos.up(y))){
				height = y - 1;
				break;
			}
		for(Entity entity : world.getOtherEntities(null, new Box(pos).stretch(0, height, 0))){
			if(!entity.hasNoGravity() && !(entity instanceof PlayerEntity player && player.getAbilities().flying)){
				entity.fallDistance = 0;
				float targetY = entity.isSneaking() ? -.3f : .6f;
				if(entity.getVelocity().y < targetY)
					entity.addVelocity(0, .09, 0);
			}
		}
	}
}