package arcana.blocks.be;

import arcana.ArcanaRegistry;
import arcana.aura.AuraWorld;
import arcana.aura.FluxOrigin;
import arcana.blocks.ArcaneLevitatorBlock;
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

import java.util.EnumMap;
import java.util.Map;

public class ArcaneLevitatorBlockEntity extends BlockEntity{
	
	private static final EnumMap<Direction.Axis, VoxelShape> WIND_COLUMN_CHECKS = new EnumMap<>(Map.of(
			Direction.Axis.Y, Block.createCuboidShape(7, 0, 7, 9, 16, 9),
			Direction.Axis.X, Block.createCuboidShape(0, 7, 7, 16, 9, 9),
			Direction.Axis.Z, Block.createCuboidShape(7, 7, 0, 9, 9, 16)
	));
	
	public ArcaneLevitatorBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.ARCANE_LEVITATOR_BE, pos, state);
	}
	
	public static void tick(World world, BlockPos pos, BlockState state, ArcaneLevitatorBlockEntity levitator){
		Direction direction = state.get(ArcaneLevitatorBlock.LEVITATOR_FACING);
		int offX = direction.getOffsetX(), offZ = direction.getOffsetZ();
		int abOffX = Math.abs(offX), abOffZ = Math.abs(offZ);
		int dist = 0;
		for(int i = 0; i < 5; i++){ // includes this block
			BlockPos local = pos.offset(direction, -i);
			if(world.getBlockEntity(local) instanceof ArcaneLevitatorBlockEntity
					&& !world.isReceivingRedstonePower(local)
					&& world.getBlockState(local).isOf(ArcanaRegistry.ARCANE_LEVITATOR)
					&& world.getBlockState(local).get(ArcaneLevitatorBlock.LEVITATOR_FACING).equals(direction))
				dist += 10;
			else
				break;
		}
		// limit height at solid block
		for(int y = 1; y < dist; y++){
			BlockPos barrierPos = pos.offset(direction, y);
			BlockState barrierState = world.getBlockState(barrierPos);
			if(VoxelShapes.matchesAnywhere(barrierState.getCollisionShape(world, barrierPos), WIND_COLUMN_CHECKS.get(direction.getAxis()), BooleanBiFunction.AND)){
				dist = y - 1;
				break;
			}
		}
		// if this is powered by redstone, or otherwise would have 0 range, don't emit flux or do anything
		if(dist == 0)
			return;
		// add working flux
		if(world.getTime() % (20 * 1000) == 0)
			AuraWorld.from(world).incrementFlux(0.5f, FluxOrigin.ARCANE_LEVITATOR, pos);
		// special behaviour for vertical levitators
		if(direction == Direction.UP){
			for(Entity entity : world.getOtherEntities(null, new Box(pos).stretch(0, dist + 0.1f, 0))){
				if(entity instanceof ArcanaPushableEntity p
						&& !p.arcana$wasPushed(Direction.UP)
						&& !entity.hasNoGravity()
						&& !(entity instanceof PlayerEntity player && player.getAbilities().flying)){
					p.arcana$setWasPushed(Direction.UP, true);
					entity.fallDistance = 0;
					boolean sneaking = entity.isSneaking();
					float frac = (float)(entity.getY() - pos.getY() - 1) / (dist);
					if(!sneaking)
						frac = 1 - frac;
					frac = frac < 0 ? frac : 1 - (1 - frac) * (1 - frac) * (1 - frac);
					float targetVelY = (sneaking ? -0.5f : 0.5f) * frac;
					double curVelY = entity.getVelocity().y;
					double g = entity.getFinalGravity();
					entity.addVelocity(0, (targetVelY - (curVelY + g)) / 4 + g, 0);
				}
			}
		}else{
			// but similar logic elsewhere
			for(Entity entity : world.getOtherEntities(null, new Box(pos).stretch(offX * (dist + 0.1f), 0, offZ * (dist + 0.1f)))){
				if(entity instanceof ArcanaPushableEntity p
						&& !p.arcana$wasPushed(direction)
						&& !(entity instanceof PlayerEntity player && player.getAbilities().flying)){
					p.arcana$setWasPushed(direction, true);
					double diff = 0.04f;
					entity.addVelocity(diff * offX, 0, diff * offZ);
				}
			}
		}
		// particles
		if(world.getTime() % 4 == 0){
			// upwind particles exist for up to 16 ticks
			int amnt = world.random.nextBetween(1, 3);
			for(int i = 0; i < amnt; i++){
				float vel = (dist * 1.2f) / 16f * (0.9f + world.random.nextFloat() * 0.2f);
				if(direction == Direction.UP)
					world.addParticle(ArcanaRegistry.UPWIND, pos.getX() + world.random.nextFloat(), pos.getY() + 1, pos.getZ() + world.random.nextFloat(), 0, vel, 0);
				else
					world.addParticle(ArcanaRegistry.WIND, pos.getX() + world.random.nextFloat() * abOffZ + Math.max(0, offX), pos.getY() + world.random.nextFloat(), pos.getZ() + world.random.nextFloat() * abOffX + Math.max(0, offZ), vel * offX, 0, vel * offZ);
			}
		}
	}
}