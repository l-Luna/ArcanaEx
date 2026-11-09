package arcana.items.foci.follow_me;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

public class BiomeDestination implements Destination{
	
	public static final MapCodec<BiomeDestination> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			RegistryCodecs.entryList(RegistryKeys.BIOME).fieldOf("biome").forGetter(x -> x.biomes)
	).apply(i, BiomeDestination::new));
	
	private final RegistryEntryList<Biome> biomes;
	
	public BiomeDestination(RegistryEntryList<Biome> biomes){
		this.biomes = biomes;
	}
	
	public RegistryEntryList<Biome> getBiomes(){
		return biomes;
	}
	
	public MapCodec<? extends Destination> getCodec(){
		return CODEC;
	}
	
	public @Nullable BlockPos getLocation(ItemStack input, ServerWorld world, BlockPos origin){
		Pair<BlockPos, RegistryEntry<Biome>> pair = world.locateBiome(biomes::contains, origin, 100, 32, 64);
		return pair == null ? null : pair.getFirst();
	}
	
	public boolean isDynamic(){
		return false;
	}
}