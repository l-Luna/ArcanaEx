package arcana.blocks;

import arcana.blocks.be.MnemonicChestBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class MnemonicChestBlock extends BlockWithEntity implements InventoryProvider{
	
	private static final MapCodec<MnemonicChestBlock> CODEC = createCodec(MnemonicChestBlock::new);
	private static final VoxelShape SHAPE = Block.createCuboidShape(1, 0, 1, 15, 14, 15);
	
	public MnemonicChestBlock(Settings settings){
		super(settings);
	}
	
	protected MapCodec<? extends BlockWithEntity> getCodec(){
		return CODEC;
	}
	
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit){
		if(world.isClient)
			return ActionResult.SUCCESS;
		if(world.getBlockEntity(pos) instanceof MnemonicChestBlockEntity be)
			player.openHandledScreen(be);
		return ActionResult.CONSUME;
	}
	
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return new MnemonicChestBlockEntity(pos, state);
	}
	
	public BlockRenderType getRenderType(BlockState state){
		return BlockRenderType.MODEL;
	}
	
	public SidedInventory getInventory(BlockState state, WorldAccess world, BlockPos pos){
		return world.getBlockEntity(pos) instanceof MnemonicChestBlockEntity be ? be.inventory : null;
	}
	
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context){
		return SHAPE;
	}
	
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved){
		if(!state.isOf(newState.getBlock()) && world.getBlockEntity(pos) instanceof MnemonicChestBlockEntity be)
			ItemScatterer.spawn(world, pos, be.inventory);
		
		super.onStateReplaced(state, world, pos, newState, moved);
	}
}