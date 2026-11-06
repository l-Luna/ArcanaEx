package arcana.blocks;

import arcana.ArcanaDamageSources;
import arcana.ArcanaRegistry;
import arcana.aura.AuraChunk;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class SilverleafSaplingBlock extends Block{
	
	public static float GROWTH_FLUX_CONSUMPTION = 2.2f,
						GROWTH_FLUX_MAX = 20f;
	
	public static final IntProperty AGE = Properties.AGE_2;
	
	public SilverleafSaplingBlock(Settings settings){
		super(settings);
		setDefaultState(getDefaultState().with(AGE, 0));
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
		super.appendProperties(builder);
		builder.add(AGE);
	}
	
	protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos){
		return !state.canPlaceAt(world, pos)
				? Blocks.AIR.getDefaultState()
				: super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
	}
	
	protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos){
		BlockState floor = world.getBlockState(pos.down());
		return floor.isIn(BlockTags.DIRT) || floor.isOf(Blocks.FARMLAND);
	}
	
	protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity){
		if(entity instanceof LivingEntity){
			entity.slowMovement(state, new Vec3d(0.8F, 0.75, 0.8F));
			if(!world.isClient && state.get(AGE) > 0
					&& (entity.lastRenderX != entity.getX() || entity.lastRenderZ != entity.getZ())
					&& Math.max(Math.abs(entity.getX() - entity.lastRenderX), Math.abs(entity.getZ() - entity.lastRenderZ)) >= 0.003f)
				entity.damage(ArcanaDamageSources.silverleaf(world), 3);
		}
	}
	
	protected boolean hasRandomTicks(BlockState state){
		return true; // this block is always not finished growing
	}
	
	protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random){
		if(random.nextInt(10) != 0 || !checkLight(world, pos))
			return;
		// small chance to grow in block light, or consume flux if present
		if(SilverleafSaplingBlock.checkFlux(world, pos))
			return;
		int curAge = state.get(AGE);
		if(curAge != 2)
			world.setBlockState(pos, state.with(AGE, curAge + 1));
		else{
			// check if there's clearance...
			for(int x = -1; x <= 1; x++)
				for(int y = 0; y <= 1; y++)
					for(int z = -1; z <= 1; z++){
						if(x == 0 && y == 0 && z == 0)
							continue;
						if(!world.isAir(pos.add(x, y, z)))
							return; // growth blocked by nearby blocks
					}
			// then grow into full bush
			for(int x = -1; x <= 1; x++)
				for(int y = 0; y <= 1; y++)
					for(int z = -1; z <= 1; z++)
						world.setBlockState(pos.add(x, y, z), ArcanaRegistry.SILVERLEAF_BUSH.set(ArcanaRegistry.SILVERLEAF_BUSH.getDefaultState(), x + 1, y, z + 1), Block.NOTIFY_ALL_AND_REDRAW);
		}
	}
	
	public static boolean checkLight(ServerWorld world, BlockPos pos){
		return world.getLightLevel(LightType.SKY, pos) == 0 && world.getLightLevel(LightType.BLOCK, pos) >= 13;
	}
	
	public static boolean checkFlux(ServerWorld world, BlockPos pos){
		AuraChunk localAura = AuraChunk.from(world, pos);
		float flux = localAura.flux();
		if(flux > 0){
			float growthChance = Math.clamp(1 - flux / GROWTH_FLUX_MAX, 0, 1);
			if(world.random.nextFloat() > growthChance){
				// filter rather than growing
				localAura.setFlux(Math.max(0, flux - GROWTH_FLUX_CONSUMPTION));
				return true;
			}
		}
		return false;
	}
}