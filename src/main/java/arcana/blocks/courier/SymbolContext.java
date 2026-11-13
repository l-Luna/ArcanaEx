package arcana.blocks.courier;

import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.Optional;

public record SymbolContext(World world, Direction symbolFacing, BlockBox volume){
	
	public Optional<BlockPos> getSingleBlock(){
		if(!volume.getDimensions().equals(new Vec3i(1, 1, 1)))
			return Optional.empty();
		BlockPos origin = new BlockPos(volume.getMinX(), volume.getMinY(), volume.getMinZ());
		return Optional.of(origin.offset(symbolFacing));
	}
}