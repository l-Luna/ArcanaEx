package arcana.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

// applies to entities or blocks
public interface ScalpelSlashable{
	
	void onScalpelSlash(World world, PlayerEntity user, BlockPos pos);
}