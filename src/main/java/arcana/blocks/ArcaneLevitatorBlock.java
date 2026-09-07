package arcana.blocks;

import arcana.ArcanaRegistry;
import arcana.blocks.be.ArcaneLevitatorBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ArcaneLevitatorBlock extends BlockWithEntity{
	
	private static final MapCodec<ArcaneLevitatorBlock> CODEC = createCodec(ArcaneLevitatorBlock::new);
	
	public ArcaneLevitatorBlock(Settings settings){
		super(settings);
	}
	
	protected MapCodec<? extends BlockWithEntity> getCodec(){
		return CODEC;
	}
	
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return new ArcaneLevitatorBlockEntity(pos, state);
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type){
		return validateTicker(type, ArcanaRegistry.ARCANE_LEVITATOR_BE, ArcaneLevitatorBlockEntity::tick);
	}
	
	public BlockRenderType getRenderType(BlockState state){
		return BlockRenderType.MODEL;
	}
	
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random){
		//
	}
}