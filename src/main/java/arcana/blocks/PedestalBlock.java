package arcana.blocks;

import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class PedestalBlock extends WaterloggableBlock implements BlockEntityProvider{
	
	private static final VoxelShape shape = VoxelShapes.union(
			createCuboidShape(1, 0, 1, 15, 4, 15),
			createCuboidShape(3, 12, 3, 13, 16, 13),
			createCuboidShape(6, 4, 6, 10, 12, 10)
	);
	
	public PedestalBlock(Settings settings){
		super(settings);
	}
	
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit){
		BlockEntity at = world.getBlockEntity(pos);
		if(at instanceof PedestalBlockEntity pedestal){
			ItemStack held = player.getStackInHand(hand);
			if(pedestal.getStack().isEmpty() && !held.isEmpty()){
				pedestal.setStack(held.split(1));
				return ActionResult.SUCCESS;
			}else if(!pedestal.getStack().isEmpty())
				if(held.isEmpty()){
					player.setStackInHand(hand, pedestal.getStack());
					pedestal.setStack(ItemStack.EMPTY);
					return ActionResult.SUCCESS;
				}else if(ItemStack.canCombine(held, pedestal.getStack()) && held.getCount() < held.getMaxCount()){
					held.increment(1);
					pedestal.setStack(ItemStack.EMPTY);
					return ActionResult.SUCCESS;
				}
		}
		return super.onUse(state, world, pos, player, hand, hit);
	}
	
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved){
		if(!state.isOf(newState.getBlock())){
			BlockEntity be = world.getBlockEntity(pos);
			if(be instanceof PedestalBlockEntity pedestal)
				ItemScatterer.spawn(world, pos, DefaultedList.copyOf(ItemStack.EMPTY, pedestal.getStack()));
		}
		super.onStateReplaced(state, world, pos, newState, moved);
	}
	
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context){
		return shape;
	}
	
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return new PedestalBlockEntity(pos, state);
	}
	
	public boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data){
		super.onSyncedBlockEvent(state, world, pos, type, data);
		BlockEntity be = world.getBlockEntity(pos);
		return be != null && be.onSyncedBlockEvent(type, data);
	}
}