package arcana.duck;

import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class ProjectedBlockHitResult extends BlockHitResult{
	
	public ProjectedBlockHitResult(BlockHitResult from){
		this(from.getPos(), from.getSide(), from.getBlockPos(), from.isInsideBlock());
	}
	
	public ProjectedBlockHitResult(Vec3d pos, Direction side, BlockPos blockPos, boolean insideBlock){
		super(pos, side, blockPos, insideBlock);
	}
}