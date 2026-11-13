package arcana.items.foci.follow_me;

import com.mojang.serialization.MapCodec;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapDecorationsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ExplorerMapTargetDestination implements Destination{
	
	public static final MapCodec<ExplorerMapTargetDestination> CODEC = MapCodec.unit(new ExplorerMapTargetDestination());
	
	public MapCodec<? extends Destination> getCodec(){
		return CODEC;
	}
	
	public @Nullable Pair<BlockPos, Optional<Heightmap.Type>> getLocation(ItemStack input, ServerWorld world, BlockPos origin, PlayerEntity caster){
		if(input.getItem() == Items.FILLED_MAP){
			MapDecorationsComponent decorations = input.getOrDefault(DataComponentTypes.MAP_DECORATIONS, MapDecorationsComponent.DEFAULT);
			// exploration maps given by trades and loot use this ID
			MapDecorationsComponent.Decoration targetDecoration = decorations.decorations().get("+");
			if(targetDecoration != null){
				// most structures with exploration maps are on the surface
				return new Pair<>(new BlockPos((int)targetDecoration.x(), world.getSeaLevel(), (int)targetDecoration.z()), Optional.of(Heightmap.Type.WORLD_SURFACE));
			}
		}
		return null;
	}
}