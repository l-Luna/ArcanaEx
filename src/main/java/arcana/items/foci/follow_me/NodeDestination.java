package arcana.items.foci.follow_me;

import arcana.aura.AuraWorld;
import arcana.aura.Node;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.Heightmap;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public record NodeDestination(boolean onlyNewNodes) implements Destination{
	
	public static final MapCodec<NodeDestination> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.BOOL.fieldOf("only_new_nodes").forGetter(NodeDestination::onlyNewNodes)
	).apply(i, NodeDestination::new));
	
	public MapCodec<? extends Destination> getCodec(){
		return CODEC;
	}
	
	public @Nullable Pair<BlockPos, Optional<Heightmap.Type>> getLocation(ItemStack input, ServerWorld world, BlockPos origin, PlayerEntity caster){
		Box bounds = new Box(origin.getX(), world.getBottomY(), origin.getZ(), origin.getX() + 1, world.getTopY(), origin.getZ() + 1).expand(200, 0, 200);
		Stream<Node> nodes = AuraWorld.from(world).getNodesInBounds(bounds).stream();
		if(onlyNewNodes)
			nodes = nodes.filter(x -> !x.getInteractedPlayerIds().contains(caster.getUuid()));
		Optional<Node> nearest = nodes.min(Comparator.comparingDouble(it -> it.asVec3d().squaredDistanceTo(origin.toCenterPos())));
		return nearest.isPresent() ? new Pair<>(nearest.get().asBlockPos().down(2), Optional.empty()) : null;
	}
	
	private static void iterateSquare(int radius, BiConsumer<Integer, Integer> cons){
		for(int step = 0; step <= radius; step++)
			for(int x = -step; x <= step; x++)
				for(int z = -step; z <= step; z++)
					if(x == step || x == -step || z == step || z == -step)
						cons.accept(x, z);
	}
}