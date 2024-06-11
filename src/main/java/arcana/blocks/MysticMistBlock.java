package arcana.blocks;

import arcana.ArcanaRegistry;
import arcana.aspects.AspectIo;
import arcana.aspects.AspectStack;
import arcana.blocks.be.MysticMistBlockEntity;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class MysticMistBlock extends BlockWithEntity implements AspectIo{
	
	public MysticMistBlock(Settings settings){
		super(settings);
	}
	
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return new MysticMistBlockEntity(pos, state);
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type){
		return checkType(type, ArcanaRegistry.MYSTIC_MIST_BE, MysticMistBlockEntity::tick);
	}
	
	public BlockRenderType getRenderType(BlockState state){
		return BlockRenderType.MODEL;
	}
	
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random){
		super.randomDisplayTick(state, world, pos, random);
		
	}
	
	public @Nullable AspectStack accept(AspectStack stack, World world, BlockPos pos, Direction from){
		return world.getBlockEntity(pos) instanceof MysticMistBlockEntity be ? be.accept(stack, world, pos, from) : stack;
	}
	
	public @Nullable AspectStack draw(int max, World world, BlockPos pos, Direction from){
		return null; // sorry! no returns
	}
}