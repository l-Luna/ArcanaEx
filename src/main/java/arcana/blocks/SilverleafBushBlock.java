package arcana.blocks;

import arcana.ArcanaDamageSources;
import arcana.ArcanaRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

import static arcana.blocks.SilverleafSaplingBlock.checkFlux;
import static arcana.blocks.SilverleafSaplingBlock.checkLight;

public class SilverleafBushBlock extends ArcanaBigBlock{
	
	public static final IntProperty X = IntProperty.of("x", 0, 2),
			Y = IntProperty.of("y", 0, 1),
			Z = IntProperty.of("z", 0, 2),
			AGE = Properties.AGE_1;
	
	public SilverleafBushBlock(Settings settings){
		super(X, Y, Z, settings);
		setDefaultState(getDefaultState().with(AGE, 0));
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
		super.appendProperties(builder);
		builder.add(X, Y, Z, AGE);
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
	
	public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state){
		return new ItemStack(ArcanaRegistry.SILVERLEAF_SEED);
	}
	
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos){
		return !state.canPlaceAt(world, pos)
				? Blocks.AIR.getDefaultState()
				: super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
	}
	
	protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos){
		BlockState floor = world.getBlockState(pos.down());
		return !isRoot(state) || floor.isIn(BlockTags.DIRT) || floor.isOf(Blocks.FARMLAND);
	}
	
	protected BlockState copyState(BlockState us, BlockState neighbor){
		return super.copyState(us, neighbor).with(AGE, neighbor.get(AGE));
	}
	
	protected boolean hasRandomTicks(BlockState state){
		return state.get(AGE) != 1 && isRoot(state);
	}
	
	protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random){
		// small chance to grow in block light, or consume flux if present
		if(random.nextInt(10) != 0 || !checkLight(world, pos))
			return;
		if(checkFlux(world, pos))
			return;
		// age up
		for(int x = -1; x <= 1; x++)
			for(int y = 0; y <= 1; y++)
				for(int z = -1; z <= 1; z++)
					world.setBlockState(pos.add(x, y, z), set(state, x + 1, y, z + 1).with(AGE, state.get(AGE) + 1), Block.NOTIFY_ALL_AND_REDRAW);
	}
	
	private boolean isRoot(BlockState state){
		return getX(state) == 1 && getY(state) == 0 && getZ(state) == 1;
	}
}