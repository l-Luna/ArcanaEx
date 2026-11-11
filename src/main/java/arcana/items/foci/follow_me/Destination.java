package arcana.items.foci.follow_me;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public interface Destination{
	
	BiMap<String, MapCodec<? extends Destination>> TYPES = HashBiMap.create();
	Codec<Destination> TYPE_CODEC = Codec.STRING
			.<MapCodec<? extends Destination>>xmap(key -> Objects.requireNonNull(TYPES.get(key)), Objects.requireNonNull(TYPES.inverse()::get))
			.dispatch(Destination::getCodec, Function.identity());
	
	MapCodec<? extends Destination> getCodec();
	
	@Nullable Pair<BlockPos, Optional<Heightmap.Type>> getLocation(ItemStack input, ServerWorld world, BlockPos origin);
	
	boolean isDynamic();
}