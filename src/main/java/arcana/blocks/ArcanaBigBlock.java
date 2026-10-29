package arcana.blocks;

import com.unascribed.lib39.weld.api.BigBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

// working around current issues in weld
public class ArcanaBigBlock extends BigBlock{
	
	public ArcanaBigBlock(IntProperty x, IntProperty y, IntProperty z, Settings settings){
		super(x, y, z, settings);
	}
	
	public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player){
		super.onBreak(world, pos, state, player);
		return state;
	}
}