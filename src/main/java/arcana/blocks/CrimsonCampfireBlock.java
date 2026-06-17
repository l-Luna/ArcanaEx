package arcana.blocks;

import arcana.ArcanaRegistry;
import arcana.blocks.be.CrimsonCampfireBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CrimsonCampfireBlock extends CampfireBlock{
	
	// CampfireBlock has non-covariant override
	private static final MapCodec<CampfireBlock> CODEC = createCodec(CrimsonCampfireBlock::new);
	
	public CrimsonCampfireBlock(Settings settings){
		super(false, 2, settings);
		setDefaultState(getDefaultState().with(LIT, false));
	}
	
	public MapCodec<CampfireBlock> getCodec(){
		return CODEC;
	}
	
	public BlockState getPlacementState(ItemPlacementContext ctx){
		return super.getPlacementState(ctx).with(LIT, false);
	}
	
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return new CrimsonCampfireBlockEntity(pos, state);
	}
	
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type){
		if(world.isClient)
			return state.get(LIT) ? validateTicker(type, ArcanaRegistry.CRIMSON_CAMPFIRE_BE, CampfireBlockEntity::clientTick) : null;
		else
			return state.get(LIT)
					? validateTicker(type, ArcanaRegistry.CRIMSON_CAMPFIRE_BE, CrimsonCampfireBlockEntity::litServerTick)
					: validateTicker(type, ArcanaRegistry.CRIMSON_CAMPFIRE_BE, CampfireBlockEntity::unlitServerTick);
	}
	
	public static boolean canBeLit(BlockState state){
		return state.isOf(ArcanaRegistry.CRIMSON_CAMPFIRE) && !state.get(WATERLOGGED) && !state.get(LIT);
	}
}