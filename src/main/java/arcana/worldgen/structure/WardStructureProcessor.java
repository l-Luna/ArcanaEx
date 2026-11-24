package arcana.worldgen.structure;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;

import java.util.Optional;

public class WardStructureProcessor extends StructureProcessor{
	
	public static final MapCodec<WardStructureProcessor> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			RegistryCodecs.entryList(RegistryKeys.BLOCK).optionalFieldOf("whitelist").forGetter(it -> !it.isBlacklist ? Optional.of(it.filter) : Optional.empty()),
			RegistryCodecs.entryList(RegistryKeys.BLOCK).optionalFieldOf("blacklist").forGetter(it -> it.isBlacklist ? Optional.of(it.filter) : Optional.empty())
	).apply(i, (whitelist, blacklist) -> {
		if(whitelist.isPresent())
			return new WardStructureProcessor(whitelist.get(), false);
		if(blacklist.isPresent())
			return new WardStructureProcessor(blacklist.get(), true);
		return new WardStructureProcessor(RegistryEntryList.empty(), true);
	}));
	public static final StructureProcessorType<WardStructureProcessor> TYPE = () -> CODEC;
	
	private final RegistryEntryList<Block> filter;
	private final boolean isBlacklist;
	
	public WardStructureProcessor(RegistryEntryList<Block> filter, boolean isBlacklist){
		this.filter = filter;
		this.isBlacklist = isBlacklist;
	}
	
	public boolean check(BlockState state){
		return filter.contains(state.getBlock().getRegistryEntry()) ^ isBlacklist;
	}
	
	protected StructureProcessorType<?> getType(){
		return TYPE;
	}
}