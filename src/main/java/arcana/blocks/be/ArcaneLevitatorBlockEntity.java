package arcana.blocks.be;

import arcana.ArcanaRegistry;
import arcana.aura.AuraWorld;
import arcana.aura.FluxOrigin;
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
			BlockPos local = pos.down(i);
			if(world.getBlockEntity(local) instanceof ArcaneLevitatorBlockEntity && !world.isReceivingRedstonePower(local))
				height += 10;
			else
				break;
		}
		// if this is powered by redstone, or otherwise would have 0 range, don't emit flux or do anything
		if(height == 0)
			return;
		if(world.getTime() % (20 * 1000) == 0)
			AuraWorld.from(world).incrementFlux(0.5f, FluxOrigin.ARCANE_LEVITATOR, pos);
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
		// particles
		if(world.getTime() % 5 == 0){
			// upwind particles exist for up to 16 ticks
			int amnt = world.random.nextBetween(1, 3);
			for(int i = 0; i < amnt; i++)
				world.addParticle(ArcanaRegistry.UPWIND, pos.getX() + world.random.nextFloat(), pos.getY() + 1, pos.getZ() + world.random.nextFloat(), 0, height / 16f * (0.9f + world.random.nextFloat() * 0.2f), 0);
		}
	}
}