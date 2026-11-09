package arcana.items.foci.follow_me;

import arcana.mixin.accessor.JigsawStructureAccessor;
import arcana.util.InertRegistryCodecs;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.JigsawStructure;
import net.minecraft.world.gen.structure.Structure;
import org.jetbrains.annotations.Nullable;

public record StructureDestination(RegistryEntryList<Structure> structure) implements Destination{
	
	public static final MapCodec<StructureDestination> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			InertRegistryCodecs.create(RegistryKeys.STRUCTURE).fieldOf("structure").forGetter(x -> x.structure)
	).apply(i, StructureDestination::new));
	
	public MapCodec<? extends Destination> getCodec(){
		return CODEC;
	}
	
	public @Nullable BlockPos getLocation(ItemStack input, ServerWorld world, BlockPos origin){
		ChunkGenerator gen = world.getChunkManager().getChunkGenerator();
		Pair<BlockPos, RegistryEntry<Structure>> located = gen.locateStructure(world, structure, origin, 1000, false);
		if(located == null)
			return null;
		int yOffset = 0;
		Structure structureType = located.getSecond().value();
		if(structureType instanceof JigsawStructure js)
			yOffset = ((JigsawStructureAccessor)(Object)js).arcana$getStartHeight().get(createChunkRandom(world, located.getFirst()), new HeightContext(gen, world));
		return located.getFirst().add(0, yOffset, 0);
	}
	
	public boolean isDynamic(){
		return false;
	}
	
	private static ChunkRandom createChunkRandom(ServerWorld world, BlockPos pos){
		ChunkPos chunkPos = new ChunkPos(pos);
		ChunkRandom chunkRandom = new ChunkRandom(new CheckedRandom(0));
		chunkRandom.setCarverSeed(world.getSeed(), chunkPos.x, chunkPos.z);
		return chunkRandom;
	}
}