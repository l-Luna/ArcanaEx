package arcana.blocks.be;

import arcana.ArcanaRegistry;
import arcana.blocks.MagicMirrorBlock;
import arcana.components.MagicMirrorQueue;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class MagicMirrorBlockEntity extends BlockEntity{
	
	public MagicMirrorBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.MAGIC_MIRROR_BE, pos, state);
	}
	
	public void tick(World world, BlockPos pos, BlockState state){
		if(world.isClient)
			return;
		ItemStack next = MagicMirrorQueue.from(world).pull(pos);
		if(next == null)
			return;
		
		Direction facing = state.get(MagicMirrorBlock.FACING);
		ItemEntity entity = new ItemEntity(world, pos.getX() + .5 - facing.getOffsetX()*0.4, pos.getY() + .5, pos.getZ() + .5 - facing.getOffsetZ()*0.4, next.copy());
		entity.setVelocity(facing.getOffsetX() * 0.2, 0, facing.getOffsetZ() * 0.2);
		world.spawnEntity(entity);
	}
}