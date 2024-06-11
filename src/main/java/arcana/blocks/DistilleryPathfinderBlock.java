package arcana.blocks;

import arcana.ArcanaRegistry;
import arcana.blocks.be.DistilleryPathfinderBlockEntity;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class DistilleryPathfinderBlock extends BlockWithEntity{
	
	protected static final VoxelShape SHAPE = VoxelShapes.union(
					createCuboidShape(0, 0, 0, 16, 10, 16),
					createCuboidShape(3.5, 9, 3.5, 12.5, 14, 12.5)
	);
	
	public DistilleryPathfinderBlock(Settings settings){
		super(settings);
	}
	
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return new DistilleryPathfinderBlockEntity(pos, state);
	}
	
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World _world, BlockState _state, BlockEntityType<T> type){
		return checkType(type, ArcanaRegistry.DISTILLERY_PATHFINDER_BE, (world, pos, state, blockEntity) -> blockEntity.tick(world, pos, state));
	}
	
	public BlockRenderType getRenderType(BlockState state){
		return BlockRenderType.MODEL;
	}
	
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit){
		if(world.isClient)
			return ActionResult.SUCCESS;
		if(world.getBlockEntity(pos) instanceof DistilleryPathfinderBlockEntity pathfinder){
			player.openHandledScreen(pathfinder);
			player.incrementStat(Stats.INTERACT_WITH_FURNACE);
		}
		return ActionResult.CONSUME;
	}
	
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved){
		if(!state.isOf(newState.getBlock()))
			if(world.getBlockEntity(pos) instanceof DistilleryPathfinderBlockEntity be){
				ItemScatterer.spawn(world, pos, be.material);
				ItemScatterer.spawn(world, pos, be.fuel);
			}
		
		super.onStateReplaced(state, world, pos, newState, moved);
	}
	
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context){
		return SHAPE;
	}
}