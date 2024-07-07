package arcana.blocks;

import com.unascribed.lib39.weld.api.BigBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;

public class ThaumicHaloBlock extends BigBlock{
	
	public static IntProperty Y = IntProperty.of("y", 0, 1);
	
	public ThaumicHaloBlock(Settings settings){
		super(null, Y, null, settings);
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
		super.appendProperties(builder);
		builder.add(Y);
	}
}