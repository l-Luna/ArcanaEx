package arcana.items.foci.follow_me;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record BiomeDestination(RegistryEntryList<Biome> biome) implements Destination{
	
	public static final MapCodec<BiomeDestination> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			RegistryCodecs.entryList(RegistryKeys.BIOME).fieldOf("biome").forGetter(x -> x.biome)
	).apply(i, BiomeDestination::new));
	
	public MapCodec<? extends Destination> getCodec(){
		return CODEC;
	}
	
	public @Nullable Pair<BlockPos, Optional<Heightmap.Type>> getLocation(ItemStack input, ServerWorld world, BlockPos origin){
		var pair = world.locateBiome(biome::contains, origin, 1000, 32, 64);
		return pair == null ? null : new Pair<>(pair.getFirst(), Optional.empty());
	}
	
	public boolean isDynamic(){
		return false;
	}
}