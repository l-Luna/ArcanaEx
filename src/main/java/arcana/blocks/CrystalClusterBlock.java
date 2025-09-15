package arcana.blocks;

import arcana.aspects.Aspect;
import arcana.aspects.AspectMap;
import arcana.aspects.Aspects;
import arcana.aura.AuraWorld;
import arcana.aura.Node;
import arcana.network.Networking;
import arcana.network.PkShakeNode;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@SuppressWarnings("deprecation")
public class CrystalClusterBlock extends WaterloggableBlock{
	
	public static final Property<Direction> facing = Properties.FACING;
	public static final IntProperty size = IntProperty.of("size", 0, 3);
	
	private static final Map<Direction, VoxelShape[]> shapes = genShapes();
	
	private final Aspect aspect;
	
	public CrystalClusterBlock(Settings settings, Aspect aspect){
		super(settings);
		this.aspect = aspect;
	}
	
	@Nullable
	public BlockState getPlacementState(ItemPlacementContext ctx){
		var world = ctx.getWorld();
		var pos = ctx.getBlockPos().offset(ctx.getSide().getOpposite());
		if(!world.getBlockState(pos).isOpaqueFullCube(world, pos))
			return null;
		return super.getPlacementState(ctx).with(facing, ctx.getSide()).with(size, 3);
	}
	
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos){
		BlockPos support = pos.offset(state.get(facing).getOpposite());
		return world.getBlockState(support).isOpaqueFullCube(world, support);
	}
	
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos){
		if(!state.canPlaceAt(world, pos))
			world.createAndScheduleBlockTick(pos, this, 1);
		
		return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
	}
	
	public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random){
		if(!state.canPlaceAt(world, pos))
			world.breakBlock(pos, true);
	}
	
	public boolean hasComparatorOutput(BlockState state){
		return true;
	}
	
	public int getComparatorOutput(BlockState state, World world, BlockPos pos){
		return state.get(size) == 3 ? 15 : 0;
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
		super.appendProperties(builder);
		builder.add(facing, size);
	}
	
	public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random){
		super.randomTick(state, world, pos, random);
		// drain our aspect from nodes to grow
		if(state.get(size) != 3){
			AuraWorld view = AuraWorld.from((World)world);
			for(Node node : view.getNodesInBounds(new Box(pos.down(4).south(4).west(4), pos.up(4).north(4).east(4)))){
				var toDrain = getAspect();
				boolean isAuram = toDrain == Aspects.AURA;
				AspectMap nodeAspects = node.getAspects();
				if(isAuram && !nodeAspects.isEmpty())
					toDrain = nodeAspects.aspectByIndex(random.nextInt(nodeAspects.size()));
				if(nodeAspects.contains(toDrain)){
					int amount = isAuram ? world.random.nextInt(5) + 9 : world.random.nextInt(3) + 2;
					if(nodeAspects.get(toDrain) >= amount){
						nodeAspects.take(toDrain, amount);
						node.markDirty();
						world.setBlockState(pos, state.with(size, state.get(size) + 1));
						break;
					}else if(isAuram){
						node.damage(random.nextInt(12) == 0, random);
						Networking.sendToNearbyPlayers(world, new PkShakeNode(node, 12), Vec3d.ofCenter(pos));
					}
				}
			}
		}
	}
	
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context){
		return shapes.get(state.get(facing))[state.get(size)];
	}
	
	public Aspect getAspect(){
		return aspect;
	}
	
	private static Map<Direction, VoxelShape[]> genShapes(){
		return Map.of(
				Direction.UP, new VoxelShape[]{
						createCuboidShape(4, 0, 4, 12, 8, 12),
						createCuboidShape(3, 0, 3, 13, 11, 13),
						createCuboidShape(3, 0, 3, 13, 13, 13),
						createCuboidShape(2, 0, 2, 14, 15, 14)
				},
				Direction.DOWN, new VoxelShape[]{
						createCuboidShape(4, 8, 4, 12, 16, 12),
						createCuboidShape(3, 5, 3, 13, 16, 13),
						createCuboidShape(3, 3, 3, 13, 16, 13),
						createCuboidShape(2, 1, 2, 14, 16, 14)
				},
				Direction.NORTH, new VoxelShape[]{
						createCuboidShape(4, 4, 8, 12, 12, 16),
						createCuboidShape(3, 4, 5, 13, 13, 16),
						createCuboidShape(3, 4, 3, 13, 13, 16),
						createCuboidShape(2, 4, 1, 14, 14, 16)
				},
				Direction.SOUTH, new VoxelShape[]{
						createCuboidShape(4, 4, 0, 12, 12, 8),
						createCuboidShape(3, 3, 0, 13, 13, 11),
						createCuboidShape(3, 3, 0, 13, 13, 13),
						createCuboidShape(2, 2, 0, 14, 14, 14)
				},
				Direction.EAST, new VoxelShape[]{
						createCuboidShape(0, 4, 4, 8, 12, 12),
						createCuboidShape(0, 3, 3, 11, 13, 13),
						createCuboidShape(0, 3, 3, 13, 13, 13),
						createCuboidShape(0, 2, 2, 15, 14, 14)
				},
				Direction.WEST, new VoxelShape[]{
						createCuboidShape(8, 4, 4, 16, 12, 12),
						createCuboidShape(5, 4, 4, 16, 12, 12),
						createCuboidShape(3, 4, 4, 16, 12, 12),
						createCuboidShape(1, 4, 4, 16, 12, 12)
				}
		);
	}
}