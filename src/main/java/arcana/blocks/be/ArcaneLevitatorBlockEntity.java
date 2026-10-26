package arcana.blocks.be;

import arcana.ArcanaRegistry;
import arcana.aura.AuraWorld;
import arcana.aura.FluxOrigin;
import arcana.duck.ArcanaPushableEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;

public class ArcaneLevitatorBlockEntity extends BlockEntity{
	
	private static final VoxelShape WIND_COLUMN = Block.createCuboidShape(7, 0, 7, 9, 16, 9);
	
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
		// limit height at solid block
		for(int y = 1; y < height; y++){
			BlockPos barrierPos = pos.up(y);
			BlockState barrierState = world.getBlockState(barrierPos);
			if(VoxelShapes.matchesAnywhere(barrierState.getCollisionShape(world, barrierPos), WIND_COLUMN, BooleanBiFunction.AND)){
				height = y - 1;
				break;
			}
		}
		// if this is powered by redstone, or otherwise would have 0 range, don't emit flux or do anything
		if(height == 0)
			return;
		if(world.getTime() % (20 * 1000) == 0)
			AuraWorld.from(world).incrementFlux(0.5f, FluxOrigin.ARCANE_LEVITATOR, pos);
		for(Entity entity : world.getOtherEntities(null, new Box(pos).stretch(0, height + 0.1f, 0))){
			if(entity instanceof ArcanaPushableEntity p
					&& !p.arcana$wasPushed(Direction.UP)
					&& !entity.hasNoGravity()
					&& !(entity instanceof PlayerEntity player && player.getAbilities().flying)){
				p.arcana$setWasPushed(Direction.UP, true);
				entity.fallDistance = 0;
				boolean sneaking = entity.isSneaking();
				float frac = (float)(entity.getY() - pos.getY() - 1) / (height);
				if(!sneaking)
					frac = 1 - frac;
				frac = frac < 0 ? frac : 1 - (1 - frac) * (1 - frac) * (1 - frac);
				float targetVelY = (sneaking ? -0.5f : 0.5f) * frac;
				double curVelY = entity.getVelocity().y;
				double g = entity.getFinalGravity();
				entity.addVelocity(0, (targetVelY - (curVelY + g)) / 4 + g, 0);
			}
		}
		// particles
		if(world.getTime() % 4 == 0){
			// upwind particles exist for up to 16 ticks
			int amnt = world.random.nextBetween(1, 3);
			for(int i = 0; i < amnt; i++)
				world.addParticle(ArcanaRegistry.UPWIND, pos.getX() + world.random.nextFloat(), pos.getY() + 1, pos.getZ() + world.random.nextFloat(), 0, (height*1.2f) / 16f * (0.9f + world.random.nextFloat() * 0.2f), 0);
		}
	}
}