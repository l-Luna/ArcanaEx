package arcana.blocks;

import arcana.ArcanaRegistry;
import arcana.aspects.AspectIo;
import arcana.aspects.AspectStack;
import arcana.blocks.be.ThaumicHaloBlockEntity;
import com.unascribed.lib39.weld.api.BigBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ThaumicHaloBlock extends BigBlock implements BlockEntityProvider, AspectIo{
	
	public static IntProperty Y = IntProperty.of("y", 0, 1);
	
	public ThaumicHaloBlock(Settings settings){
		super(null, Y, null, settings);
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
		super.appendProperties(builder);
		builder.add(Y);
	}
	
	@Nullable
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return state.get(Y) == 0 ? new ThaumicHaloBlockEntity(pos, state) : null;
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type){
		return type == ArcanaRegistry.THAUMIC_HALO_BE ? (w, p, s, h) -> ThaumicHaloBlockEntity.tick(w, p, s, (ThaumicHaloBlockEntity)h) : null;
	}
	
	public @Nullable AspectStack accept(AspectStack stack, World world, BlockPos pos, Direction from){
		return world.getBlockEntity(pos) instanceof ThaumicHaloBlockEntity be ? be.accept(stack, world, pos, from) : stack;
	}
	
	public @Nullable AspectStack draw(int max, World world, BlockPos pos, Direction from){
		return null;
	}
}