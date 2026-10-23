package arcana.blocks;

import arcana.ArcanaRegistry;
import arcana.blocks.be.AwareCrucibleBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AwareCrucibleBlock extends CrucibleBlock{
	
	private static final MapCodec<AwareCrucibleBlock> CODEC = createCodec(AwareCrucibleBlock::new);
	
	public AwareCrucibleBlock(Settings settings){
		super(settings);
	}
	
	protected MapCodec<? extends BlockWithEntity> getCodec(){
		return CODEC;
	}
	
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit){
		if(!player.isSneaking()){
			if(world.isClient)
				return ActionResult.SUCCESS;
			if(world.getBlockEntity(pos) instanceof AwareCrucibleBlockEntity be)
				player.openHandledScreen(be);
			return ActionResult.CONSUME;
		}
		return super.onUse(state, world, pos, player, hit);
	}
	
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved){
		if(!state.isOf(newState.getBlock()) && world.getBlockEntity(pos) instanceof AwareCrucibleBlockEntity be)
			ItemScatterer.spawn(world, pos, be.bookInventory);
		
		super.onStateReplaced(state, world, pos, newState, moved);
	}
	
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return new AwareCrucibleBlockEntity(pos, state);
	}
	
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World _world, BlockState _state, BlockEntityType<T> type){
		return validateTicker(type, ArcanaRegistry.AWARE_CRUCIBLE_BE, (world, pos, state, entity) -> entity.tick());
	}
	
	protected boolean hasComparatorOutput(BlockState state){
		return true;
	}
	
	protected int getComparatorOutput(BlockState state, World world, BlockPos pos){
		return world.getBlockEntity(pos) instanceof AwareCrucibleBlockEntity be && be.getSignalTicks() > 0 ? 15 : 0;
	}
}